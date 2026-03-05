package lu.kbra.restopolis_bots.discord;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import lu.kbra.restopolis_bots.data.TargetPlatform;
import lu.kbra.restopolis_bots.db.data.MealData;
import lu.kbra.restopolis_bots.db.data.MealSectionData;
import lu.kbra.restopolis_bots.db.data.RestaurantData;
import lu.kbra.restopolis_bots.db.data.RestaurantSectionData;
import lu.kbra.restopolis_bots.db.data.TargetRestaurantSectionData;
import lu.kbra.restopolis_bots.db.data.discord.DiscordPlatformData;
import lu.kbra.restopolis_bots.db.table.MealSectionTable;
import lu.kbra.restopolis_bots.db.table.MealTable;
import lu.kbra.restopolis_bots.db.table.RestaurantSectionTable;
import lu.kbra.restopolis_bots.db.table.RestaurantTable;
import lu.kbra.restopolis_bots.db.table.TargetRestaurantSectionTable;
import lu.kbra.restopolis_bots.db.table.TargetTable;
import lu.kbra.restopolis_bots.db.table.discord.DiscordPlatformTable;
import lu.rescue_rush.spring.jda.DiscordSenderService;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;

@Component
@Profile("!noDiscord")
public class DiscordSchedule {

	@Autowired
	private DiscordSenderService discordSenderService;

	@Autowired
	private TargetTable targetTable;
	@Autowired
	private DiscordPlatformTable discordPlatformTable;
	@Autowired
	private TargetRestaurantSectionTable targetRestaurantSectionTable;
	@Autowired
	private RestaurantSectionTable restaurantSectionTable;
	@Autowired
	private MealSectionTable mealSectionTable;
	@Autowired
	private RestaurantTable restaurantTable;
	@Autowired
	private MealTable mealTable;

	@Scheduled(cron = "0 0 9 * * *")
	public void runTargets() {
		discordSenderService.awaitJDAReady();

		final JDA jda = discordSenderService.getJda();

		final LocalDate today = LocalDate.now();
		final DayOfWeek dayOfWeek = today.getDayOfWeek();

		targetTable.all(TargetPlatform.DISCORD).forEachRemaining(target -> {
			try {
				if (target.getDays() == null || target.getDays().isEmpty() || !target.getDays().contains(dayOfWeek)) {
					return;
				}

				final DiscordPlatformData discordPlatformData = discordPlatformTable
						.loadIfExists(new DiscordPlatformData(target.getId()))
						.orElse(null);
				if (discordPlatformData == null) {
					return;
				}
				if (discordPlatformData.isDm()) {
					jda.openPrivateChannelById(discordPlatformData.getUserId()).complete();
				}
				final MessageChannel channel = discordPlatformData.isDm() ? jda.getPrivateChannelById(discordPlatformData.getChannelId())
						: jda.getTextChannelById(discordPlatformData.getChannelId());
				final Role role = !discordPlatformData.isDm() && discordPlatformData.getRoleId() != null
						? jda.getRoleById(discordPlatformData.getRoleId())
						: null;
				if (channel == null) {
					return;
				}

				final List<TargetRestaurantSectionData> targetRestaurantSectionDatas = targetRestaurantSectionTable
						.byTarget(target.getId());
				if (targetRestaurantSectionDatas.isEmpty()) {
					return;
				}
				final List<RestaurantSectionData> restaurantSectionDatas = targetRestaurantSectionDatas
						.stream()
						.map(c -> restaurantSectionTable.byId(c.getRestaurantSectionId()))
						.toList();

				final Map<RestaurantData, Map<RestaurantSectionData, MealSectionData>> map = new HashMap<>();

				restaurantSectionDatas.forEach(restaurantSectionData -> {
					final RestaurantData restaurantData = restaurantTable.byId(restaurantSectionData.getRestaurantId());
					final MealData mealData = mealTable.todayByRestaurant(restaurantSectionData.getRestaurantId());
					final MealSectionData mealSectionData = mealSectionTable
							.byMealAndRestaurantSection(mealData.getId(), restaurantSectionData.getId());

					map.computeIfAbsent(restaurantData, k -> new HashMap<>());
					map.get(restaurantData).put(restaurantSectionData, mealSectionData);
				});

				String msg = buildMessage(map
						.entrySet()
						.stream()
						.collect(Collectors
								.toMap(e -> e.getKey().getName(),
										e -> e
												.getValue()
												.entrySet()
												.stream()
												.collect(
														Collectors.toMap(e2 -> e2.getKey().getName(), e2 -> e2.getValue().getContent())))));

				if (role != null) {
					msg = role.getAsMention() + "\n" + msg;
				}
				channel.sendMessage(msg).queue();
			} catch (Exception e) {
				System.err.println(target);
				e.printStackTrace();
			}
		});
	}

	public String buildMessage(Map<String, Map<String, List<String>>> maps) {
		final StringBuilder sb = new StringBuilder();

		sb.append("# " + ("<t:" + Instant.now().getEpochSecond() + ":D>") + "\n");

		for (Entry<String, Map<String, List<String>>> pairVal : maps.entrySet()) {
			sb.append("## " + pairVal.getKey() + "\n"); // restaurant name

			final Map<String, List<String>> map = pairVal.getValue();
			for (Entry<String, List<String>> e2 : map.entrySet()) {
				sb.append("__**" + e2.getKey() + "**__\n"); // course title
				e2.getValue().forEach(c -> sb.append("* " + c + "\n"));
			}
		}

		return sb.toString();
	}

	private int index = 0;
	private final String[] messages = { "Yum yum", "Blergh", "Mew >:3c", "Ratin' good foog" };

	@Scheduled(fixedRate = 60000)
	public void changeActivity() {
		if (!discordSenderService.isReady()) {
			return;
		}

		discordSenderService.getJda().getPresence().setActivity(Activity.playing(messages[index]));

		index = (index + 1) % messages.length;
	}

}
