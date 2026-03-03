package lu.kbra.restopolis_bots.whatsapp;

import java.time.DayOfWeek;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
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

		final WhatsappPlatformData whatsappPlatformData = whatsappPlatformTable.byChat(chatId).orElseGet(() -> {
			final TargetData td = targetTable.insertAndReload(new TargetData(TargetPlatform.WHATSAPP,
					Arrays.asList(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY)));
			return whatsappPlatformTable.insertAndReload(new WhatsappPlatformData(td.getId(), chatId));
		});

		if (!wahaHttpClient.isSenderAdmin(chatId, voter)) {
			return;
//			wahaHttpClient.sendText(chatId, "You are not an admin @" + voter.split("@")[0] + ".", Arrays.asList(voter));
		} else {
			wahaHttpClient.sendText(chatId, String.join("\n", selected));
		}

		final List<RestaurantSectionData> restaurantSections = targetRestaurantSectionTable.byTarget(whatsappPlatformData.getId())
				.stream()
				.map(c -> restaurantSectionTable.byId(c.getRestaurantSectionId()))
				.sorted((a, b) -> Long.compare(a.getRestaurantId(), b.getRestaurantId()))
				.toList();
	}

	public void incomingMessage(final JsonNode payload) {
		final String chatId = payload.at("/payload/from").asText();
		final String from = payload.at("/payload/participant").asText();
		final String content = payload.at("/payload/body").asText();

		if (from == null || from.isBlank() || chatId == null || chatId.isBlank() || content == null || content.isBlank()) {
			return;
		}

		if (!content.startsWith("/")) {
			return;
		}

		if (chatId.contains("@g.us") && !wahaHttpClient.isSenderAdmin(chatId, from)) {
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
				return;
			}

			final String name = IntStream.range(1, split.length).mapToObj(c -> split[c]).collect(Collectors.joining(" "));
			if (split.length == 2 && name.matches("\\d+")) {
				final Optional<RestaurantData> rd = this.restaurantTable.optById(Integer.parseInt(name));
				rd.ifPresent(value -> this.sendSectionQuery(chatId, value));
				return;
			}
			final List<RestaurantData> list = this.restaurantTable.likeName(name, 25);
			this.sendRestaurantQuery(chatId, list);
		}
		}

	}

	private void sendRestaurantQuery(final String from, final List<RestaurantData> list) {
		if (list.isEmpty()) {
			wahaHttpClient.sendText(from, "No matching restaurant found. :(");
		} else {
			wahaHttpClient.sendText(from,
					"Use: _select [restaurant id]_\n\n*ID: Restaurant's name*\n" + list.stream()
							.map(c -> "```" + PCUtils.leftPadString(Long.toString(c.getId()), " ", 5) + "```" + ": " + c.getName())
							.collect(Collectors.joining("\n")));
		}
	}

	private void sendSectionQuery(final String chatId, final RestaurantData restaurantData) {
		final JsonNode node = wahaHttpClient.sendPoll(chatId,
				"Select sections for: *" + restaurantData.getName() + "* (```" + restaurantData.getId() + "```)",
				restaurantSectionTable.byRestaurant(restaurantData.getId()).stream().map(RestaurantSectionData::getName).toList(),
				true);
		whatsappPlatformTable.byChat(chatId)
				.ifPresent(whatsappPlatformData -> wahaHttpClient.sendPollVote(node.at("/at/remote").asText(),
						node.at("/id/_serialized").asText(),
						targetRestaurantSectionTable.byTarget(whatsappPlatformData.getId())
								.stream()
								.map(c -> restaurantSectionTable.byId(c.getRestaurantSectionId()))
								.map(RestaurantSectionData::getName)
								.toList()));
	}

	public void groupJoin(final JsonNode payload) {

	}

}
