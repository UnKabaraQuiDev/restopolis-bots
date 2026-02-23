package lu.kbra.restopolis_bots;

import java.io.IOException;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import lu.kbra.pclib.datastructure.pair.Pair;
import lu.kbra.pclib.datastructure.pair.Pairs;

@Component
@Profile("!noRestopolis")
public class RestopolisFetcher {

	public static final String DEBUG_PROPERTY = "restopolis.debug";
	public static boolean DEBUG = Boolean.getBoolean(DEBUG_PROPERTY);

	private static final Logger LOGGER = Logger.getLogger(RestopolisFetcher.class.getName());

	@Autowired
	private RestTemplate restTemplate;

	@Value("${restopolis.url}")
	private String targetUrl;
	@Value("${restopolis.cookies}")
	private String cookies;

	private List<String> targets;

	@Scheduled(cron = "0 0 8 * * 1-5")
	public void runDailyCheck() {
		// this should only fetch and place in a db table

		final List<Pair<String, Map<String, String>>> maps = new ArrayList<>();
		for (final String key : targets) {
			try {
				final String cookie = cookies.replace("%TARGET%", key);
				if (DEBUG) {
					LOGGER.info("Fetching for: " + targetUrl + " with cookies: " + cookie);
				}
				final String html = fetchHtmlWithCookies(targetUrl, cookie);
				maps.add(parseAndProcess(html));
			} catch (IOException e) {
				LOGGER.warning("Error fetching or parsing HTML: " + e.getMessage());
			}
		}

		sendMessage(maps);
	}

	public void sendMessage(List<Pair<String, Map<String, String>>> maps) {
		final StringBuilder sb = new StringBuilder();
		final String[] wantedList = new String[0];

		final Instant now = Instant.now();
		final long unixSeconds = now.getEpochSecond();
		final String discordTime = "<t:" + unixSeconds + ":D>";
		sb.append("# " + discordTime + "\n");

		for (Pair<String, Map<String, String>> pairVal : maps) {
			sb.append("## " + pairVal.getKey() + "\n"); // restaurant name

			final Map<String, String> map = pairVal.getValue();
			for (String w : wantedList) {
				if (map.containsKey(w)) {
					sb.append("__**" + w + "**__\n"); // course title
					sb.append(map.get(w) + "\n");
				}
			}
		}
	}

	private String fetchHtmlWithCookies(String url, String cookieHeader) throws IOException {
		final HttpHeaders headers = new HttpHeaders();
		headers.set(HttpHeaders.COOKIE, cookieHeader);

		final HttpEntity<String> request = new HttpEntity<>(headers);

		final ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, request, String.class);

		return response.getBody();
	}

	private Pair<String, Map<String, String>> parseAndProcess(String html) {
		final Document doc = Jsoup.parse(html);

		final String restName = doc.getElementsByClass("restaurant-selector-value-inner").get(0).text();

		final Map<String, List<String>> map = new HashMap<>();

		final LocalDate today = LocalDate.now();
		final DayOfWeek dow = today.getDayOfWeek();

		final int day = dow.getValue();

		// somehow matching too much ?
		final Element container = doc.select(".menu-overview > .menu-slider > div > .formulaeContainer").get(day - 1);
		final Elements courseDivs = container.select("div.course-name");

		for (final Element courseDiv : courseDivs) {
			final String courseName = courseDiv.text();
			Element sibling = courseDiv.nextElementSibling();

			while (sibling != null && !sibling.hasClass("course-name")) {

				if (sibling.hasClass("product-name")) {
					map.putIfAbsent(courseName, new ArrayList<>());
					map.get(courseName).add(sibling.text());
				}

				sibling = sibling.nextElementSibling();
			}
		}

		return Pairs
				.readOnly(restName,
						map
								.entrySet()
								.parallelStream()
								.collect(Collectors
										.toMap(e -> e.getKey(),
												e -> e.getValue().stream().map(c -> "* " + c).collect(Collectors.joining("\n")))));
	}

}
