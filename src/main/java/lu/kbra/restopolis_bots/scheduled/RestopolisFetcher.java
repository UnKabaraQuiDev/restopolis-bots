package lu.kbra.restopolis_bots.scheduled;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import lu.kbra.pclib.datastructure.pair.Pair;
import lu.kbra.pclib.datastructure.pair.Pairs;
import lu.kbra.restopolis_bots.db.data.MealData;
import lu.kbra.restopolis_bots.db.data.MealSectionData;
import lu.kbra.restopolis_bots.db.data.RestaurantData;
import lu.kbra.restopolis_bots.db.data.RestaurantSectionData;
import lu.kbra.restopolis_bots.db.data.RestaurantSiteData;
import lu.kbra.restopolis_bots.db.table.MealSectionTable;
import lu.kbra.restopolis_bots.db.table.MealTable;
import lu.kbra.restopolis_bots.db.table.RestaurantSectionTable;
import lu.kbra.restopolis_bots.db.table.RestaurantSiteTable;
import lu.kbra.restopolis_bots.db.table.RestaurantTable;

@Component
public class RestopolisFetcher {

	public static final String DEBUG_PROPERTY = "restopolis.debug";
	public static boolean DEBUG = Boolean.getBoolean(DEBUG_PROPERTY);

	private static final Logger LOGGER = Logger.getLogger(RestopolisFetcher.class.getName());

	private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd.MM.yyyy");

	@Autowired
	private RestTemplate restTemplate;

	@Value("${restopolis.url}")
	private String targetUrl;
	@Value("${restopolis.cookies}")
	private String cookies;

	@Autowired
	private RestaurantSiteTable restaurantSiteTable;
	@Autowired
	private RestaurantTable restaurantTable;
	@Autowired
	private RestaurantSectionTable restaurantSectionTable;
	@Autowired
	private MealTable mealTable;
	@Autowired
	private MealSectionTable mealSectionTable;

	@Scheduled(cron = "0 0 8 * * *")
	public void runListFetch() throws IOException {
		final Document doc = Jsoup.parse(fetchHtmlWithCookies(targetUrl, null));
		doc.select("div.restaurant-selector-list > h3.site").forEach(c -> {
			final String dataSite = c.attr("data-site");
			final String name = c.text();
			if (dataSite == null || dataSite.isBlank() || name == null || name.isBlank()) {
				return;
			}
			restaurantSiteTable.loadIfExistsElseInsert(new RestaurantSiteData(Integer.parseInt(dataSite), name));
		});
		doc.select("div.restaurant-selector-list > a.restaurant-name").forEach(c -> {
			final String dataSite = c.attr("data-site");
			final String name = c.text();
			final String absHref = c.attr("href");
			if (dataSite == null || dataSite.isBlank() || name == null || name.isBlank() || absHref == null || absHref.isBlank()) {
				return;
			}
			final URI href;
			try {
				href = new URI(absHref);
			} catch (URISyntaxException e) {
				e.printStackTrace();
				return;
			}
			final String hrefQuery = href.getQuery();
			final String restaurantId = Arrays.stream(hrefQuery.split("&")).map(q -> {
				final String[] split = q.split("=");
				return Pairs.readOnly(split[0], split[1]);
			}).filter(p -> "pRestaurantSelection".equalsIgnoreCase(p.getKey())).findFirst().map(Pair::getValue).orElse(null);
			// /eRestauration/CustomerServices/Menu/BtnChangeRestaurant?pRestaurantSelection=1198
			if (restaurantId == null || restaurantId.isBlank()) {
				return;
			}
			final int restaurantIndex = Integer.parseInt(restaurantId);
			final int dataSiteId = Integer.parseInt(dataSite);

			restaurantTable.loadIfExistsElseInsert(new RestaurantData(restaurantIndex, name, dataSiteId));
		});
	}

	public Pair<LocalDate, Map<String, List<String>>> fetchForRestaurant(long restaurantId) {
		try {
			// https://ssl.education.lu/eRestauration/CustomerServices/Menu/BtnChangeRestaurant?pRestaurantSelection=1198
			final Document doc = Jsoup.parse(fetchHtmlWithCookies(targetUrl, cookies.replace("%TARGET%", Long.toString(restaurantId))));

			final String dateStr = doc.select("a.day.active").attr("data-date");
			final LocalDate date = LocalDate.parse(dateStr, DATE_FMT);
			final Elements divContainers = doc.select("div.formulaeContainer");
			if (divContainers.isEmpty()) {
				return null;
			}
			final Element divContainer = divContainers.get(date.getDayOfWeek().getValue() - 1);
			final Elements courseDivs = divContainer.select("div.course-name");

			final Map<String, List<String>> map = new HashMap<>();

			for (final Element courseDiv : courseDivs) {
				final String courseName = courseDiv.text();
				Element sibling = courseDiv.nextElementSibling();

				while (sibling != null && !sibling.hasClass("course-name")) {

					if (sibling.hasClass("product-name")) {
						map.computeIfAbsent(courseName, k -> new ArrayList<>());
						map.get(courseName).add(sibling.text());
					}

					sibling = sibling.nextElementSibling();
				}
			}

			return Pairs.readOnly(date, map);
		} catch (Exception e) {
			System.err.println("Error on fetch restaurant " + restaurantId + ": " + e.getMessage() + " (" + e.getClass().getName() + ")");
			return null;
		}
	}

	@Scheduled(cron = "0 30 8 * * *")
	public void runMenuFetch() {
		restaurantTable.all().forEachRemaining(restaurant -> {
			final Pair<LocalDate, Map<String, List<String>>> pair = fetchForRestaurant(restaurant.getId());
			if (pair == null) {
				LOGGER.warning("Menu fetch failed for: " + restaurant.getId() + " (" + restaurant.getName() + ")");
				return;
			} else {
				LOGGER.info("Menu fetch OK: " + restaurant.getId() + " (" + restaurant.getName() + ")");
			}

			final MealData mealData = mealTable.loadUniqueIfExistsElseInsert(new MealData(restaurant.getId(), pair.getKey()));

			pair.getValue().forEach((k, v) -> {
				final RestaurantSectionData restaurantSection = restaurantSectionTable
						.loadUniqueIfExistsElseInsert(new RestaurantSectionData(restaurant.getId(), k));

				final MealSectionData mealSection = mealSectionTable
						.loadIfExistsElseInsert(new MealSectionData(mealData.getId(), restaurantSection.getId(), v));
			});
		});
	}

	private String fetchHtmlWithCookies(String url, String cookieHeader) throws IOException {
		final HttpHeaders headers = new HttpHeaders();
		if (cookieHeader != null)
			headers.set(HttpHeaders.COOKIE, cookieHeader);

		final HttpEntity<String> request = new HttpEntity<>(headers);

		final ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, request, String.class);

		return response.getBody();
	}

}
