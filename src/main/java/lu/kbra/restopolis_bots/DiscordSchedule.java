package lu.kbra.restopolis_bots;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
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
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

@Component
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
			if (target.getDays() == null || target.getDays().isEmpty() || !target.getDays().contains(dayOfWeek)) {
				return;
			}

			final DiscordPlatformData discordPlatformData = discordPlatformTable
					.load(new DiscordPlatformData(target.getId()));

			final Guild guild = jda.getGuildById(discordPlatformData.getServerId());
			final TextChannel channel = (TextChannel) jda.getGuildChannelById(ChannelType.TEXT,
					discordPlatformData.getChannelId());
			final Role role = discordPlatformData.getRoleId() != null ? jda.getRoleById(discordPlatformData.getRoleId())
					: null;
			if (guild == null || channel == null) {
				return;
			}

			final List<TargetRestaurantSectionData> targetRestaurantSectionDatas = targetRestaurantSectionTable
					.byTarget(target.getId());
			final List<RestaurantSectionData> restaurantSectionDatas = targetRestaurantSectionDatas.stream()
					.map(c -> restaurantSectionTable.byId(c.getRestaurantSectionId())).toList();

			final Map<RestaurantData, Map<RestaurantSectionData, MealSectionData>> map = new HashMap<>();

			restaurantSectionDatas.forEach(restaurantSectionData -> {
				final RestaurantData restaurantData = restaurantTable.byId(restaurantSectionData.getRestaurantId());
				final MealData mealData = mealTable.todayByRestaurant(restaurantSectionData.getRestaurantId());
				final MealSectionData mealSectionData = mealSectionTable.byMealAndRestaurantSection(mealData.getId(),
						restaurantSectionData.getId());

				map.computeIfAbsent(restaurantData, k -> new HashMap<>());
				map.get(restaurantData).put(restaurantSectionData, mealSectionData);
			});

			String msg = buildMessage(map.entrySet().stream()
					.collect(Collectors.toMap(e -> e.getKey().getName(), e -> e.getValue().entrySet().stream().collect(
							Collectors.toMap(e2 -> e2.getKey().getName(), e2 -> e2.getValue().getContent())))));

			if (role != null) {
				msg = role.getAsMention() + "\n" + msg;
			}
			channel.sendMessage(msg).queue();
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

}
