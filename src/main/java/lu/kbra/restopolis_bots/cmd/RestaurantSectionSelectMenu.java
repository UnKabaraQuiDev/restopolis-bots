package lu.kbra.restopolis_bots.cmd;

import java.util.Collections;
import java.util.List;
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
import lu.rescue_rush.spring.jda.menu.DiscordStringMenu;
import lu.rescue_rush.spring.jda.menu.DiscordStringMenuExecutor;
import net.dv8tion.jda.api.components.selections.StringSelectMenu;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;

@Component("restaurant_section_select")
public class RestaurantSectionSelectMenu implements DiscordStringMenu, DiscordStringMenuExecutor {

	private String beanName;

	@Autowired
	private TargetTable targetTable;
	@Autowired
	private DiscordPlatformTable discordPlatformTable;
	@Autowired
	private RestaurantTable restaurantTable;
	@Autowired
	private RestaurantSectionTable restaurantSectionTable;
	@Autowired
	private TargetRestaurantSectionTable targetRestaurantSectionTable;

	@Override
	public void execute(StringSelectInteractionEvent event) {
		event.deferReply(true).queue();
		final DiscordPlatformData discordPlatformData = discordPlatformTable
				.byServer(event.isFromGuild() ? event.getGuild().getIdLong() : event.getChannelIdLong())
				.orElseGet(() -> {
					final TargetData targetData = targetTable
							.insertAndReload(new TargetData(TargetPlatform.DISCORD, Collections.emptyList()));
					return discordPlatformTable.insertAndReload(new DiscordPlatformData(targetData.getId(),
							event.isFromGuild() ? event.getGuild().getId() : event.getChannelId(),
							event.isFromGuild() ? event.getChannelId() : event.getUser().getId(),
							null,
							!event.isFromGuild()));
				});

		event.getSelectedOptions().forEach(c -> {
			targetRestaurantSectionTable
					.loadIfExistsElseInsert(new TargetRestaurantSectionData(discordPlatformData.getId(), Long.parseLong(c.getValue())));
		});

		event.getSelectMenu().getOptions().stream().filter(c -> !event.getSelectedOptions().contains(c)).forEach(c -> {
			targetRestaurantSectionTable
					.deleteIfExists(new TargetRestaurantSectionData(discordPlatformData.getId(), Long.parseLong(c.getValue())));
		});

		final List<RestaurantSectionData> restaurantSections = targetRestaurantSectionTable.byTarget(discordPlatformData.getId())
				.stream()
				.map(c -> restaurantSectionTable.byId(c.getRestaurantSectionId()))
				.sorted((a, b) -> Long.compare(a.getRestaurantId(), b.getRestaurantId()))
				.toList();
		final String msg = restaurantSections.stream()
				.map(c -> "* __" + restaurantTable.byId(c.getRestaurantId()).getName() + "__: " + c.getName())
				.collect(Collectors.joining("\n"));
		event.getHook()
				.sendMessage("**Your restaurants:**\n" + (msg == null || msg.isBlank() ? "*No content*" : msg))
				.setEphemeral(true)
				.queue();
		;
	}

	@Override
	public StringSelectMenu build() {
		throw new UnsupportedOperationException();
	}

	public StringSelectMenu build(final RestaurantData restaurant, List<Long> selected) {
		final List<RestaurantSectionData> all = restaurantSectionTable.byRestaurant(restaurant.getId());
		if(all.isEmpty()) {
			return null;
		}
		final StringSelectMenu.Builder a = StringSelectMenu.create(beanName)
				.setPlaceholder("Select the sections")
				.setMinValues(0)
				.setMaxValues(all.size());
		all.forEach(c -> a.addOption(c.getName(), Long.toString(c.getId())));
		a.setDefaultValues(selected.stream().map(c -> Long.toString(c)).toList());
		return a.build();
	}

	@Override
	public void setBeanName(String name) {
		this.beanName = name;
	}

}
