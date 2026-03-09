package lu.kbra.restopolis_bots.whatsapp;

import java.time.DayOfWeek;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;

import lu.kbra.pclib.PCUtils;
import lu.kbra.restopolis_bots.data.TargetPlatform;
import lu.kbra.restopolis_bots.db.data.RestaurantData;
import lu.kbra.restopolis_bots.db.data.RestaurantSectionData;
import lu.kbra.restopolis_bots.db.data.TargetData;
import lu.kbra.restopolis_bots.db.data.TargetRestaurantSectionData;
import lu.kbra.restopolis_bots.db.data.whatsapp.WhatsappPlatformData;
import lu.kbra.restopolis_bots.db.table.RestaurantSectionTable;
import lu.kbra.restopolis_bots.db.table.RestaurantTable;
import lu.kbra.restopolis_bots.db.table.TargetRestaurantSectionTable;
import lu.kbra.restopolis_bots.db.table.TargetTable;
import lu.kbra.restopolis_bots.db.table.whatsapp.WhatsappPlatformTable;

@Service
@Profile("!noWhatsapp")
public class WhatsappService {

	private static final String HELP_TEXT = """
			*Commands:*

			* */search [restaurant name]*: List of all the restaurants matching the given name.
			* */list [restaurant id]*: List all the sections from the given restaurant.
			* */subscribe [section id], ...*: Subscribe to the sections with the provided id.
			* */subscribe all [restaurant id]*: Subscribe to all the sections provided by the given restaurant.
			* */unsubscribe [section id], ...*: Unsubscribe from the sections with the provided id.
			* */unsubscribe all [restaurant id], ...*: Unsubscribe from all the sections provided by the given restaurant.
			* */unsubscribe all*: Unsubscribe from all the sections.
			* */schedule [day id], ...*: Select the days of the week you wish to receive updates.
			* */show*: Shows the current config for this chat.""";

	@Autowired
	private WahaHttpClient wahaHttpClient;

	@Autowired
	private RestaurantTable restaurantTable;
	@Autowired
	private RestaurantSectionTable restaurantSectionTable;

	@Autowired
	private TargetTable targetTable;
	@Autowired
	private TargetRestaurantSectionTable targetRestaurantSectionTable;
	@Autowired
	private WhatsappPlatformTable whatsappPlatformTable;

	public void incomingMessage(final JsonNode payload) {
//		System.err.println(payload.toPrettyString());

		final String chatId = payload.at("/payload/from").asText();
		final String participant = payload.at("/payload/participant").asText() == null
				|| payload.at("/payload/participant").asText().isBlank() ? chatId : payload.at("/payload/participant").asText();
		final String content = payload.at("/payload/body").asText();

//		System.err.println(participant + ":" + chatId + ":" + content);

		if (participant == null || participant.isBlank() || chatId == null || chatId.isBlank() || content == null || content.isBlank()) {
			return;
		}

		if (!content.startsWith("/") || (chatId.contains("@g.us") && !this.wahaHttpClient.isSenderAdmin(chatId, participant))) {
			return;
		}

		final String[] split = content.split("\s+");
		if (split.length == 0) {
			return;
		}

		final String command = split[0].substring(1);
		switch (command.toLowerCase()) {
		case "search" -> {
			if (split.length == 1) {
				this.wahaHttpClient.sendText(chatId, "Usage: _/search [restaurant name]_\nExample: _/search lam_");
				return;
			}

			final String name = IntStream.range(1, split.length).mapToObj(c -> split[c]).collect(Collectors.joining(" "));

			final List<RestaurantData> list = this.restaurantTable.likeName(name, 25);
			if (list.size() == 1) {
				this.sendSectionQuery(chatId, list.get(0));
			} else {
				this.sendRestaurantQuery(chatId, list);
			}
		}
		case "list" -> {
			if (split.length == 1) {
				this.wahaHttpClient.sendText(chatId, "Usage: _/list [restaurant id]_\nExample: _/list 52_");
				return;
			}

			final String name = IntStream.range(1, split.length).mapToObj(c -> split[c]).collect(Collectors.joining(" "));

			if (split.length == 2 && name.matches("\\d+")) {
				final int id = Integer.parseInt(name);
				final Optional<RestaurantData> rd = this.restaurantTable.optById(id);
				rd.ifPresentOrElse(value -> this.sendSectionQuery(chatId, value),
						() -> this.wahaHttpClient.sendText(chatId,
								"Unknown restaurant id: ```" + id
										+ "```.\nUse _/search [name]_ to look up restaurants.\nExample _/search lam_"));
			} else {
				this.wahaHttpClient.sendText(chatId, "Usage: _/list [restaurant id]_\nExample: _/list 52_");
			}
		}
		case "subscribe" -> {
			if (split.length == 1) {
				this.wahaHttpClient.sendText(chatId,
						"Usage: _/subscribe all [restaurant id]_ or _/subscribe [section id], ..._\nExample: _/subscribe all 52_ or _/subscribe 210,211_");
				return;
			}

			final WhatsappPlatformData whatsappPlatformData = this.whatsappPlatformTable.byChat(chatId).orElseGet(() -> {
				final TargetData td = this.targetTable.insertAndReload(new TargetData(TargetPlatform.WHATSAPP, TargetData.allDays()));
				return this.whatsappPlatformTable.insertAndReload(new WhatsappPlatformData(td.getId(), chatId));
			});

			if ("all".equalsIgnoreCase(split[1])) {
				if (split.length >= 3 && split[2].matches("\\d+")) {
					final int restaurantId = Integer.parseInt(split[2]);
					final Optional<RestaurantData> restaurant = this.restaurantTable.optById(restaurantId);
					restaurant.ifPresentOrElse(
							r -> this.restaurantSectionTable.byRestaurant(restaurantId)
									.forEach(restaurantSectionData -> this.targetRestaurantSectionTable.loadIfExistsElseInsert(
											new TargetRestaurantSectionData(whatsappPlatformData.getId(), restaurantSectionData.getId()))),
							() -> this.wahaHttpClient.sendText(chatId,
									"Unknown restaurant id: ```" + restaurantId
											+ "```.\nUse _/search [name]_ to look up restaurants.\nExample _/search lam_"));
				} else {
					this.wahaHttpClient.sendText(chatId,
							"Usage: _/subscribe all [restaurant id]_ or _/subscribe [section id], ..._\nExample: _/subscribe all 52_ or _/subscribe 210,211_");
				}
			} else {
				final String allIds = IntStream.range(1, split.length).mapToObj(c -> split[c]).collect(Collectors.joining(" "));
				Arrays.stream(allIds.split(","))
						.map(String::trim)
						.filter(c -> c.matches("\\d+"))
						.mapToInt(Integer::parseInt)
						.filter(this.restaurantSectionTable::exists)
						.forEach(id -> this.targetRestaurantSectionTable
								.loadIfExistsElseInsert(new TargetRestaurantSectionData(whatsappPlatformData.getId(), id)));
			}

			this.sendCurrentSections(chatId);
		}
		case "unsubscribe" -> {
			if (split.length == 1) {
				this.wahaHttpClient.sendText(chatId,
						"Usage: _/unsubscribe all_, _/unsubscribe all [restaurant id]_ or _/unsubscribe [section id], ..._\nExample: _/unsubscribe all_, _/unsubscribe all 52_ or _/unsubscribe 210,211_");
				return;
			}

			final WhatsappPlatformData whatsappPlatformData = this.whatsappPlatformTable.byChat(chatId).orElseGet(() -> {
				final TargetData td = this.targetTable.insertAndReload(new TargetData(TargetPlatform.WHATSAPP, TargetData.allDays()));
				return this.whatsappPlatformTable.insertAndReload(new WhatsappPlatformData(td.getId(), chatId));
			});

			if ("all".equalsIgnoreCase(split[1])) {
				if (split.length == 2) {
					this.targetRestaurantSectionTable.byTarget(whatsappPlatformData.getId())
							.forEach(this.targetRestaurantSectionTable::deleteIfExists);

					this.wahaHttpClient.sendText(chatId, "Deleted all your selections.");
				} else if (split[2].matches("\\d+")) {
					final int restaurantId = Integer.parseInt(split[2]);
					final Optional<RestaurantData> restaurant = this.restaurantTable.optById(restaurantId);
					restaurant.ifPresentOrElse(
							r -> this.restaurantSectionTable.byRestaurant(restaurantId)
									.forEach(restaurantSectionData -> this.targetRestaurantSectionTable.deleteIfExists(
											new TargetRestaurantSectionData(whatsappPlatformData.getId(), restaurantSectionData.getId()))),
							() -> this.wahaHttpClient.sendText(chatId,
									"Unknown restaurant id: ```" + restaurantId
											+ "```.\nUse _/search [name]_ to look up restaurants.\nExample _/search lam_"));
				} else {
					this.wahaHttpClient.sendText(chatId,
							"Usage: _/unsubscribe all_, _/unsubscribe all [restaurant id]_ or _/unsubscribe [section id], ..._\nExample: _/unsubscribe all_, _/unsubscribe all 52_ or _/unsubscribe 210,211_");
				}
			} else {
				final String allIds = IntStream.range(1, split.length).mapToObj(c -> split[c]).collect(Collectors.joining(" "));
				Arrays.stream(allIds.split(","))
						.map(String::trim)
						.filter(c -> c.matches("\\d+"))
						.mapToInt(Integer::parseInt)
						.forEach(id -> this.targetRestaurantSectionTable
								.deleteIfExists(new TargetRestaurantSectionData(whatsappPlatformData.getId(), id)));
			}

			this.sendCurrentSections(chatId);
		}
		case "show" -> {
			this.sendCurrentSections(chatId);
			this.sendCurrentSchedule(chatId);
		}
		case "schedule" -> {
			if (split.length == 1) {
				this.sendCurrentSchedule(chatId);
				return;
			}

			final WhatsappPlatformData whatsappPlatformData = this.whatsappPlatformTable.byChat(chatId).orElseGet(() -> {
				final TargetData td = this.targetTable.insertAndReload(new TargetData(TargetPlatform.WHATSAPP, TargetData.allDays()));
				return this.whatsappPlatformTable.insertAndReload(new WhatsappPlatformData(td.getId(), chatId));
			});
			final TargetData targetData = this.targetTable.byId(whatsappPlatformData);
			targetData.setDays(
					Arrays.stream(IntStream.range(1, split.length).mapToObj(c -> split[c]).collect(Collectors.joining(" ")).split(","))
							.map(String::trim)
							.mapToInt(Integer::parseInt)
							.mapToObj(DayOfWeek::of)
							.toList());
			this.targetTable.updateAndReload(targetData);

			this.sendCurrentSchedule(chatId);
		}
		case "help" -> {
			this.sendHelp(chatId);
		}
		}

	}

	private void sendHelp(final String chatId) {
		this.wahaHttpClient.sendText(chatId, HELP_TEXT);
	}

	private void sendCurrentSchedule(final String chatId) {
		this.whatsappPlatformTable.byChat(chatId).ifPresentOrElse(whatsappPlatformData -> {
			final TargetData targetData = this.targetTable.byId(whatsappPlatformData.getId());

			final String msg = Arrays.stream(DayOfWeek.values())
					.map(c -> (targetData.getDays().contains(c) ? "✅" : "❌") + " ```"
							+ PCUtils.leftPadString(Long.toString(c.getValue()), " ", 5) + "```: *" + c.name() + "*")
					.collect(Collectors.joining("\n"));

			this.wahaHttpClient.sendText(chatId,
					"*Your schedule:*\n"
							+ "Use _/schedule [day id], ..._ to set the days you wish to receive updates\nExample: _/schedule 1,2,3,4,5_ selects Monday to Friday\n\n"
							+ msg);
		},
				() -> this.wahaHttpClient.sendText(chatId,
						"No data for this chat, use _/schedule [day id], ..._ to start !\nExample: _/schedule 1,2,3,4,5_ select monday to friday"));
	}

	private void sendCurrentSections(final String chatId) {
		this.whatsappPlatformTable.byChat(chatId).ifPresentOrElse(whatsappPlatformData -> {
			final List<RestaurantSectionData> restaurantSections = this.targetRestaurantSectionTable.byTarget(whatsappPlatformData.getId())
					.stream()
					.map(c -> this.restaurantSectionTable.byId(c.getRestaurantSectionId()))
					.sorted(Comparator.comparing(RestaurantSectionData::getRestaurantId))
					.toList();

			final String msg = restaurantSections.stream()
					.collect(Collectors.groupingBy(RestaurantSectionData::getRestaurantId))
					.entrySet()
					.stream()
					.map(entry -> {
						final long restaurantId = entry.getKey();
						final String restaurantName = this.restaurantTable.byId(restaurantId).getName();

						final String sections = entry.getValue()
								.stream()
								.map(section -> "* " + section.getName() + " (```" + section.getId() + "```)")
								.collect(Collectors.joining("\n"));

						return "*" + restaurantName + "* (```" + restaurantId + "```)\n" + sections;
					})
					.collect(Collectors.joining("\n\n"));

			this.wahaHttpClient.sendText(chatId,
					"*Your restaurants:*\n" + (msg == null || msg.isBlank()
							? "_No content_, use _/search [restaurant name]_ to start !\nExample: _/search lam_"
							: "Use _/search [restaurant name]_ to add a restaurant,\n _/list [restaurant id]_ to list the sections provided by a restaurant\nand _/subscribe (all) [section id], ..._ or _/unsubscribe (all) [section id], ..._ to add or remove sections\nExample: _/search lam_, _/list 52_, _/subscribe 220_, _/subscribe 220,221,223_ or _/subscribe all 52_\n\n"
									+ msg));
		},
				() -> this.wahaHttpClient.sendText(chatId,
						"No data for this chat, use _/search [restaurant name]_ to start !\nExample: _/search lam_"));
	}

	private void sendRestaurantQuery(final String from, final List<RestaurantData> list) {
		if (list.isEmpty()) {
			this.wahaHttpClient.sendText(from, "No matching restaurant found. :(");
		} else {
			this.wahaHttpClient.sendText(from,
					"Use: _/list [restaurant id]_ to see the list of sections provided by this restaurant.\nExample: _/list 52_\n\n*ID: Restaurant's name*\n"
							+ list.stream()
									.map(c -> "```" + PCUtils.leftPadString(Long.toString(c.getId()), " ", 5) + "```" + ": " + c.getName())
									.collect(Collectors.joining("\n")));
		}
	}

	private void sendSectionQuery(final String chatId, final RestaurantData restaurantData) {
		this.whatsappPlatformTable.byChat(chatId).ifPresentOrElse(whatsappPlatformData -> {
			final Set<Long> set = this.targetRestaurantSectionTable.byTarget(whatsappPlatformData.getId())
					.stream()
					.map(TargetRestaurantSectionData::getRestaurantSectionId)
					.collect(Collectors.toSet());
			this.wahaHttpClient.sendText(chatId,
					"Select sections for: *" + restaurantData.getName() + "* (```" + restaurantData.getId()
							+ "```)\nUse: _/subscribe (all) [section id], ..._ or _/unsubscribe (all) [section id], ..._\nExample: _/subscribe 220_, _/subscribe 220,227,223,230_ or _/subscribe all 52_\n\n*ID: Section Name*\n"
							+ this.restaurantSectionTable.byRestaurant(restaurantData.getId())
									.stream()
									.map(c -> (set.contains(c.getId()) ? "✅" : "❌") + " ```"
											+ PCUtils.leftPadString(Long.toString(c.getId()), " ", 5) + "```" + ": " + c.getName())
									.collect(Collectors.joining("\n")));
		},
				() -> this.wahaHttpClient.sendText(chatId,
						"Select sections for: *" + restaurantData.getName() + "* (```" + restaurantData.getId()
								+ "```)\nUse: _/subscribe (all) [section id], ..._ or _/unsubscribe (all) [section id], ...\nExample: _/subscribe 220_, _/subscribe 220,227,223,230_ or _/subscribe all 52_\n\n*ID: Section Name*\n"
								+ this.restaurantSectionTable.byRestaurant(restaurantData.getId())
										.stream()
										.map(c -> "```" + PCUtils.leftPadString(Long.toString(c.getId()), " ", 5) + "```" + ": "
												+ c.getName())
										.collect(Collectors.joining("\n"))));
	}

}
