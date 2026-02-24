package lu.kbra.restopolis_bots.cmd;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import lu.kbra.restopolis_bots.data.TargetPlatform;
import lu.kbra.restopolis_bots.db.data.RestaurantData;
import lu.kbra.restopolis_bots.db.data.RestaurantSectionData;
import lu.kbra.restopolis_bots.db.data.TargetData;
import lu.kbra.restopolis_bots.db.data.TargetRestaurantSectionData;
import lu.kbra.restopolis_bots.db.data.discord.DiscordPlatformData;
import lu.kbra.restopolis_bots.db.table.RestaurantSectionTable;
import lu.kbra.restopolis_bots.db.table.RestaurantTable;
import lu.kbra.restopolis_bots.db.table.TargetRestaurantSectionTable;
import lu.kbra.restopolis_bots.db.table.TargetTable;
import lu.kbra.restopolis_bots.db.table.discord.DiscordPlatformTable;
import lu.rescue_rush.spring.jda.command.slash.SlashCommandAutocomplete;
import lu.rescue_rush.spring.jda.command.slash.SlashCommandExecutor;
import lu.rescue_rush.spring.jda.command.slash.SubSlashCommandExecutor;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

@Component("select")
public class SelectCmd implements SlashCommandExecutor, SlashCommandAutocomplete {

	@Override
	public void execute(SlashCommandInteractionEvent event) {

	}

	@Override
	public void complete(CommandAutoCompleteInteractionEvent event) {

	}

	@Override
	public String description() {
		return "Choose the restaurants you want updates from.";
	}

	@Component("remove")
	public static class SelectRemoveCmd implements SubSlashCommandExecutor, SlashCommandAutocomplete {

		@Autowired
		private RestaurantTable restaurantTable;
		@Autowired
		private RestaurantSectionTable restaurantSectionTable;
		@Autowired
		private DiscordPlatformTable discordPlatformTable;
		@Autowired
		private TargetRestaurantSectionTable targetRestaurantSectionTable;
		@Autowired
		private TargetTable targetTable;

		@Override
		public void execute(SlashCommandInteractionEvent event) {
			event.deferReply(true).queue();
			final String restaurantName = event.getOption("restaurant").getAsString();
			final Optional<RestaurantData> restaurant = restaurantTable.byName(restaurantName);
			if (restaurant.isPresent()) {
				final DiscordPlatformData discordPlatformData = discordPlatformTable
						.byServer(event.getGuild().getIdLong())
						.orElseGet(() -> {
							final TargetData targetData = targetTable
									.insertAndReload(new TargetData(TargetPlatform.DISCORD, Collections.emptyList()));
							return discordPlatformTable
									.insertAndReload(new DiscordPlatformData(targetData.getId(), event.getGuild().getId(),
											event.getChannelId(), null));
						});
				final List<Long> restaurantSectionDatas = targetRestaurantSectionTable
						.byTarget(discordPlatformData.getId())
						.stream()
						.map(TargetRestaurantSectionData::getRestaurantSectionId)
						.toList();
				restaurantSectionDatas.forEach(c -> {
					targetRestaurantSectionTable.deleteIfExists(new TargetRestaurantSectionData(discordPlatformData.getId(), c));
				});

				final List<RestaurantSectionData> restaurantSections = targetRestaurantSectionTable
						.byTarget(discordPlatformData.getId())
						.stream()
						.map(c -> restaurantSectionTable.byId(c.getRestaurantSectionId()))
						.sorted((a, b) -> Long.compare(a.getRestaurantId(), b.getRestaurantId()))
						.toList();
				event
						.getHook()
						.sendMessage(restaurantSections
								.stream()
								.map(c -> "* " + restaurantTable.byId(c.getRestaurantId()).getName() + ": " + c.getName())
								.collect(Collectors.joining("\n")))
						.setEphemeral(true)
						.queue();
			} else {
				event.getHook().sendMessage("No restaurant with name: " + restaurantName + ", found.").setEphemeral(true).queue();
			}
		}

		@Override
		public void complete(CommandAutoCompleteInteractionEvent event) {
			event
					.replyChoiceStrings(restaurantTable
							.likeName(event.getFocusedOption().getValue(), OptionData.MAX_CHOICES)
							.stream()
							.map(RestaurantData::getName)
							.toList())
					.queue();
		}

		@Override
		public String description() {
			return "Remove all the sections of a restaurant.";
		}

		@Override
		public OptionData[] options() {
			return new OptionData[] {
					new OptionData(OptionType.STRING, "restaurant", "The restaurant's name.").setAutoComplete(true).setRequired(true) };
		}

		@Override
		public Class<? extends SlashCommandExecutor> getCommandClass() {
			return SelectCmd.class;
		}

	}

	@Component("config")
	public static class SelectConfigCmd implements SubSlashCommandExecutor, SlashCommandAutocomplete {

		@Autowired
		private RestaurantTable restaurantTable;
		@Autowired
		private DiscordPlatformTable discordPlatformTable;
		@Autowired
		private TargetRestaurantSectionTable targetRestaurantSectionTable;
		@Autowired
		private TargetTable targetTable;

		@Autowired
		private RestaurantSectionSelectMenu restaurantSectionSelectMenu;

		@Override
		public void execute(SlashCommandInteractionEvent event) {
			event.deferReply(true).queue();
			final String restaurantName = event.getOption("restaurant").getAsString();
			final Optional<RestaurantData> restaurant = restaurantTable.byName(restaurantName);
			if (restaurant.isPresent()) {
				final DiscordPlatformData discordPlatformData = discordPlatformTable
						.byServer(event.getGuild().getIdLong())
						.orElseGet(() -> {
							final TargetData targetData = targetTable
									.insertAndReload(new TargetData(TargetPlatform.DISCORD, Collections.emptyList()));
							return discordPlatformTable
									.insertAndReload(new DiscordPlatformData(targetData.getId(), event.getGuild().getId(),
											event.getChannelId(), null));
						});
				final List<Long> restaurantSectionDatas = targetRestaurantSectionTable
						.byTarget(discordPlatformData.getId())
						.stream()
						.map(TargetRestaurantSectionData::getRestaurantSectionId)
						.toList();

				event
						.getHook()
						.sendMessage("Restaurant: " + restaurantName + ":")
						.setEphemeral(true)
						.setActionRow(restaurantSectionSelectMenu.build(restaurant.get(), restaurantSectionDatas))
						.queue();
			} else {
				event.getHook().sendMessage("No restaurant with name: " + restaurantName + ", found.").setEphemeral(true).queue();
			}
		}

		@Override
		public void complete(CommandAutoCompleteInteractionEvent event) {
			event
					.replyChoiceStrings(restaurantTable
							.likeName(event.getFocusedOption().getValue(), OptionData.MAX_CHOICES)
							.stream()
							.map(RestaurantData::getName)
							.toList())
					.queue();
		}

		@Override
		public String description() {
			return "Choose the sections of a restaurant.";
		}

		@Override
		public OptionData[] options() {
			return new OptionData[] {
					new OptionData(OptionType.STRING, "restaurant", "The restaurant's name.").setAutoComplete(true).setRequired(true) };
		}

		@Override
		public Class<? extends SlashCommandExecutor> getCommandClass() {
			return SelectCmd.class;
		}

	}

}
