package lu.kbra.restopolis_bots.cmd;

import java.time.DayOfWeek;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import lu.kbra.restopolis_bots.data.TargetPlatform;
import lu.kbra.restopolis_bots.db.data.TargetData;
import lu.kbra.restopolis_bots.db.data.discord.DiscordPlatformData;
import lu.kbra.restopolis_bots.db.table.TargetTable;
import lu.kbra.restopolis_bots.db.table.discord.DiscordPlatformTable;
import lu.rescue_rush.spring.jda.menu.DiscordStringMenu;
import lu.rescue_rush.spring.jda.menu.DiscordStringMenuExecutor;
import net.dv8tion.jda.api.components.selections.StringSelectMenu;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;

@Component("days_select")
public class DaySelectMenu implements DiscordStringMenu, DiscordStringMenuExecutor {

	private String beanName;

	@Autowired
	private DiscordPlatformTable discordPlatformTable;
	@Autowired
	private TargetTable targetTable;

	@Override
	public void execute(StringSelectInteractionEvent event) {
		event.deferReply(true).queue();

		final DiscordPlatformData discordPlatformData = discordPlatformTable
				.byServer(event.isFromGuild() ? event.getGuild().getIdLong() : event.getChannelIdLong())
				.orElseGet(() -> {
					final TargetData targetData = targetTable
							.insertAndReload(new TargetData(TargetPlatform.DISCORD,
									new ArrayList<>(Arrays
											.asList(DayOfWeek.MONDAY,
													DayOfWeek.TUESDAY,
													DayOfWeek.WEDNESDAY,
													DayOfWeek.THURSDAY,
													DayOfWeek.FRIDAY))));
					return discordPlatformTable
							.insertAndReload(new DiscordPlatformData(targetData.getId(),
									event.isFromGuild() ? event.getGuild().getId() : event.getChannelId(),
									event.isFromGuild() ? event.getChannelId() : event.getUser().getId(), null, !event.isFromGuild()));
				});

		final TargetData targetData = targetTable.byId(discordPlatformData);
		targetData.setDays(event.getSelectedOptions().stream().map(c -> DayOfWeek.valueOf(c.getValue())).toList());
		targetTable.updateAndReload(targetData);
		event.getHook().sendMessage("Updated days to: " + targetData.getDays()).setEphemeral(true).queue();
	}

	@Override
	public StringSelectMenu build() {
		return build(Collections.emptyList());
	}

	public StringSelectMenu build(List<DayOfWeek> dows) {
		final StringSelectMenu.Builder a = StringSelectMenu
				.create(beanName)
				.setPlaceholder("Select the days")
				.setMinValues(0)
				.setMaxValues(7);
		for (DayOfWeek dow : DayOfWeek.values()) {
			a.addOption(dow.getDisplayName(TextStyle.FULL, Locale.ENGLISH), dow.name());
		}
		a.setDefaultValues(dows.stream().map(DayOfWeek::name).toList());
		return a.build();
	}

	@Override
	public void setBeanName(String name) {
		this.beanName = name;
	}

}
