package lu.kbra.restopolis_bots.discord.cmd;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import lu.kbra.restopolis_bots.db.data.TargetData;
import lu.kbra.restopolis_bots.db.data.discord.DiscordPlatformData;
import lu.kbra.restopolis_bots.db.table.TargetTable;
import lu.kbra.restopolis_bots.db.table.discord.DiscordPlatformTable;
import lu.kbra.restopolis_bots.discord.menu.DaySelectMenu;
import lu.rescue_rush.spring.jda.command.slash.SlashCommandExecutor;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

@Component("schedule")
public class ScheduleCmd implements SlashCommandExecutor {

	@Autowired
	private DaySelectMenu daySelectMenu;
	@Autowired
	private TargetTable targetTable;
	@Autowired
	private DiscordPlatformTable discordPlatformTable;

	@Override
	public void execute(SlashCommandInteractionEvent event) {
		event.deferReply(true).queue();
		if (event.isFromGuild() && !event.getMember()
				.hasPermission(event.getGuildChannel(), Permission.ADMINISTRATOR, Permission.MANAGE_CHANNEL, Permission.MANAGE_SERVER)) {
			event.getHook().sendMessage("You don't have the permission to do that.").setEphemeral(true).queue();
			return;
		}
		final DiscordPlatformData discordPlatformData = discordPlatformTable
				.byServer(event.isFromGuild() ? event.getGuild().getIdLong() : event.getChannelIdLong())
				.orElse(null);
		if (discordPlatformData == null) {
			event.getHook().sendMessage("Choose your days:").setEphemeral(true).addComponents(ActionRow.of(daySelectMenu.build())).queue();
			return;
		}
		final TargetData targetData = targetTable.byId(discordPlatformData.getId());
		event.getHook()
				.sendMessage("Choose your days:")
				.setEphemeral(true)
				.addComponents(ActionRow.of(daySelectMenu.build(targetData.getDays())))
				.queue();

	}

	@Override
	public String description() {
		return "Select the days you wish to receive the notification.";
	}

}
