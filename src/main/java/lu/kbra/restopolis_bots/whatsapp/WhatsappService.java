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

	@Deprecated
	public void pollVote(final JsonNode payload) {
		if (!payload.at("/payload/poll/fromMe").asBoolean() || payload.at("/payload/vote/fromMe").asBoolean()) {
			return;
		}

		final String voter = payload.at("/payload/vote/from").asText();
		final JsonNode values = payload.at("/payload/vote/selectedOptions");
		if (!values.isArray()) {
			return;
		}
		final List<String> selected = IntStream.range(0, values.size()).mapToObj(c -> values.get(c).asText()).toList();

		final String chatId = payload.at("/payload/vote/to").asText();
		final String messageId = payload.at("/payload/poll/id").asText();

		final WhatsappPlatformData whatsappPlatformData = this.whatsappPlatformTable.byChat(chatId).orElseGet(() -> {
			final TargetData td = this.targetTable.insertAndReload(new TargetData(TargetPlatform.WHATSAPP,
					Arrays.asList(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY)));
			return this.whatsappPlatformTable.insertAndReload(new WhatsappPlatformData(td.getId(), chatId));
		});

		if (!this.wahaHttpClient.isSenderAdmin(chatId, voter)) {
			return;
//			wahaHttpClient.sendText(chatId, "You are not an admin @" + voter.split("@")[0] + ".", Arrays.asList(voter));
		} else {
			this.wahaHttpClient.sendText(chatId, String.join("\n", selected));
		}

		final List<RestaurantSectionData> restaurantSections = this.targetRestaurantSectionTable.byTarget(whatsappPlatformData.getId())
				.stream()
				.map(c -> this.restaurantSectionTable.byId(c.getRestaurantSectionId()))
				.sorted(Comparator.comparing(RestaurantSectionData::getRestaurantId))
				.toList();
	}

	public void incomingMessage(final JsonNode payload) {
		final String chatId = payload.at("/payload/from").asText();
		final String from = payload.at("/payload/participant").asText();
		final String content = payload.at("/payload/body").asText();

		if (from == null || from.isBlank() || chatId == null || chatId.isBlank() || content == null || content.isBlank()) {
			return;
		}

		if (!content.startsWith("/") || (chatId.contains("@g.us") && !this.wahaHttpClient.isSenderAdmin(chatId, from))) {
			return;
		}

		final String[] split = content.split("\s+");
		if (split.length == 0) {
			return;
		}

		final String command = split[0].substring(1);
		switch (command.toLowerCase()) {
		case "select" -> {
			if (split.length == 1) {
				this.sendHelp(chatId);
				return;
			}

			final String name = IntStream.range(1, split.length).mapToObj(c -> split[c]).collect(Collectors.joining(" "));
			if (split.length == 2 && name.matches("\\d+")) {
				final Optional<RestaurantData> rd = this.restaurantTable.optById(Integer.parseInt(name));
				rd.ifPresent(value -> this.sendSectionQuery(chatId, value));
				return;
			}
			final List<RestaurantData> list = this.restaurantTable.likeName(name, 25);
			if (list.size() == 1) {
				this.sendSectionQuery(chatId, list.get(0));
			} else {
				this.sendRestaurantQuery(chatId, list);
			}
		}
		case "subscribe" -> {
			if (split.length == 1) {
				this.sendHelp(chatId);
				return;
			}

			final WhatsappPlatformData whatsappPlatformData = this.whatsappPlatformTable.byChat(chatId).orElseGet(() -> {
				final TargetData td = this.targetTable.insertAndReload(new TargetData(TargetPlatform.WHATSAPP, TargetData.allDays()));
				return this.whatsappPlatformTable.insertAndReload(new WhatsappPlatformData(td.getId(), chatId));
			});
			final String allIds = IntStream.range(1, split.length).mapToObj(c -> split[c]).collect(Collectors.joining(" "));
			Arrays.stream(allIds.split(","))
					.map(String::trim)
					.filter(c -> c.matches("\\d+"))
					.mapToInt(Integer::parseInt)
					.forEach(id -> this.targetRestaurantSectionTable
							.loadIfExistsElseInsert(new TargetRestaurantSectionData(whatsappPlatformData.getId(), id)));

			this.sendCurrentSections(chatId);
		}
		case "unsubscribe" -> {
			if (split.length == 1) {
				this.sendHelp(chatId);
				return;
			}

			if (split.length == 2 && "all".equalsIgnoreCase(split[1])) {
				this.whatsappPlatformTable.byChat(chatId).ifPresent(whatsappPlatformData -> {
					this.targetRestaurantSectionTable.byTarget(whatsappPlatformData.getId())
							.forEach(this.targetRestaurantSectionTable::deleteIfExists);
					this.whatsappPlatformTable.deleteIfExists(whatsappPlatformData);
					this.targetTable.deleteIfExists(this.targetTable.byId(whatsappPlatformData.getId()));
				});

				this.wahaHttpClient.sendText(chatId, "Deleted all your selections.");

				return;
			}

			final WhatsappPlatformData whatsappPlatformData = this.whatsappPlatformTable.byChat(chatId).orElseGet(() -> {
				final TargetData td = this.targetTable.insertAndReload(new TargetData(TargetPlatform.WHATSAPP, TargetData.allDays()));
				return this.whatsappPlatformTable.insertAndReload(new WhatsappPlatformData(td.getId(), chatId));
			});
			final String allIds = IntStream.range(1, split.length).mapToObj(c -> split[c]).collect(Collectors.joining(" "));
			Arrays.stream(allIds.split(","))
					.map(String::trim)
					.filter(c -> c.matches("\\d+"))
					.mapToInt(Integer::parseInt)
					.forEach(id -> this.targetRestaurantSectionTable
							.deleteIfExists(new TargetRestaurantSectionData(whatsappPlatformData.getId(), id)));

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
		this.wahaHttpClient.sendText(chatId, """
				*Commands:*

				* _/select [restaurant name]_: Returns a list of all the restaurants matching this name.
				* _/select [restaurant id]_: Returns a list of all the sections from that restaurant.
				* _/subscribe [section id],..._: Subscribe to the sections with that id.
				* _/unsubscribe [section id],..._: Unsubscribe to the sections with that id.
				* _/schedule [day id],..._: Select the days of the week you wish to receive updates.
				* _/show_: Shows the current config for this chat.""");
	}

	private void sendCurrentSchedule(final String chatId) {
		this.whatsappPlatformTable.byChat(chatId).ifPresentOrElse(whatsappPlatformData -> {
			final TargetData targetData = this.targetTable.byId(whatsappPlatformData.getId());

			final String msg = Arrays.stream(DayOfWeek.values())
					.map(c -> (targetData.getDays().contains(c) ? "✅" : "❌") + " ```"
							+ PCUtils.leftPadString(Long.toString(c.getValue()), " ", 5) + "```: *" + c.name() + "*")
					.collect(Collectors.joining("\n"));

			this.wahaHttpClient.sendText(chatId,
					"*Your schedule:*\n" + (msg == null || msg.isBlank() ? "_No content_, use _/schedule [day id],..._ to start !"
							: "Use _/schedule [day id],..._ to set the days you wish to receive updates\n\n" + msg));
		}, () -> this.wahaHttpClient.sendText(chatId, "No data for this chat, use _/schedule [day],..._ to start !"));
	}

	private void sendCurrentSections(final String chatId) {
		this.whatsappPlatformTable.byChat(chatId).ifPresentOrElse(whatsappPlatformData -> {
			final List<RestaurantSectionData> restaurantSections = this.targetRestaurantSectionTable.byTarget(whatsappPlatformData.getId())
					.stream()
					.map(c -> this.restaurantSectionTable.byId(c.getRestaurantSectionId()))
					.sorted(Comparator.comparing(RestaurantSectionData::getRestaurantId))
					.toList();
			final String msg = restaurantSections.stream()
					.map(c -> "* *" + this.restaurantTable.byId(c.getRestaurantId()).getName() + "* (```" + c.getRestaurantId() + "```): "
							+ c.getName() + " (```" + c.getId() + "```)")
					.collect(Collectors.joining("\n"));

			this.wahaHttpClient.sendText(chatId,
					"*Your restaurants:*\n" + (msg == null || msg.isBlank() ? "_No content_, use _/select [restaurant name]_ to start !"
							: "Use _/select [restaurant name/id]_ to add a restaurant\nand _/subscribe [section id],..._ or _/unsubscribe [section id],..._ to select more sections\n\n"
									+ msg));
		}, () -> this.wahaHttpClient.sendText(chatId, "No data for this chat, use _/select [restaurant name]_ to start !"));
	}

	private void sendRestaurantQuery(final String from, final List<RestaurantData> list) {
		if (list.isEmpty()) {
			this.wahaHttpClient.sendText(from, "No matching restaurant found. :(");
		} else {
			this.wahaHttpClient.sendText(from,
					"Use: _/select [restaurant id]_\n\n*ID: Restaurant's name*\n" + list.stream()
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
							+ "```)\nUse: _subscribe [section id],..._ or _unsubscribe [section id],..._\n\n*ID: Section Name*\n"
							+ this.restaurantSectionTable.byRestaurant(restaurantData.getId())
									.stream()
									.map(c -> (set.contains(c.getId()) ? "✅" : "❌") + " ```"
											+ PCUtils.leftPadString(Long.toString(c.getId()), " ", 5) + "```" + ": " + c.getName())
									.collect(Collectors.joining("\n")));
		}, () -> {
			this.wahaHttpClient.sendText(chatId,
					"Select sections for: *" + restaurantData.getName() + "* (```" + restaurantData.getId()
							+ "```)\nUse: _subscribe [section id],..._ or _unsubscribe [section id],..._\n\n*ID: Section Name*\n"
							+ this.restaurantSectionTable.byRestaurant(restaurantData.getId())
									.stream()
									.map(c -> "```" + PCUtils.leftPadString(Long.toString(c.getId()), " ", 5) + "```" + ": " + c.getName())
									.collect(Collectors.joining("\n")));
		});
	}

	@Deprecated
	public void groupJoin(final JsonNode payload) {

	}

}
