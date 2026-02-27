package lu.kbra.restopolis_bots.cmd;

import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import lu.kbra.restopolis_bots.data.TargetPlatform;
import lu.kbra.restopolis_bots.db.data.TargetData;
import lu.kbra.restopolis_bots.db.data.discord.DiscordPlatformData;
import lu.kbra.restopolis_bots.db.table.TargetTable;
import lu.kbra.restopolis_bots.db.table.discord.DiscordPlatformTable;
import lu.kbra.restopolis_bots.menu.ChannelSelectMenu;
import lu.rescue_rush.spring.jda.command.slash.SlashCommandExecutor;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

@Component("channel")
public class ChannelCmd implements SlashCommandExecutor {

	@Autowired
	private ChannelSelectMenu channelSelectMenu;
	@Autowired
	private TargetTable targetTable;
	@Autowired
	private DiscordPlatformTable discordPlatformTable;

	@Override
	public void execute(SlashCommandInteractionEvent event) {
		event.deferReply(true).queue();
		if (!event.isFromGuild()) {
			event.getHook().sendMessage("Server-only command.").setEphemeral(true).queue();
			return;
		}
		if (!event.getMember().hasPermission(event.getGuildChannel(), Permission.MANAGE_CHANNEL, Permission.MANAGE_SERVER)) {
			event.getHook().sendMessage("You don't have the permission to do that.").setEphemeral(true).queue();
			return;
		}
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
		event
				.getHook()
				.sendMessage("Select your channel:")
				.setEphemeral(true)
				.addComponents(ActionRow.of(channelSelectMenu.build(discordPlatformData.getChannelId())))
				.queue();

	}

	@Override
	public String description() {
		return "Select the role that'll be pinged in the notification.";
	}

}
