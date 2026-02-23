package lu.kbra.restopolis_bots.cmd;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import lu.rescue_rush.spring.jda.command.slash.SlashCommandExecutor;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

@Component("schedule")
public class ScheduleCmd implements SlashCommandExecutor {

	@Autowired
	private DaySelectMenu daySelectMenu;

	@Override
	public void execute(SlashCommandInteractionEvent event) {
		event.deferReply().queue();
		if (event.isFromGuild() && !event.getMember().hasPermission(event.getGuildChannel(), Permission.ADMINISTRATOR,
				Permission.MANAGE_CHANNEL, Permission.MANAGE_SERVER)) {
			event.getHook().setEphemeral(true).sendMessage("You don't have the permission to do that.").queue();
			return;
		}
		event.getHook().setEphemeral(true).sendMessage("Choose your days:").setActionRow(daySelectMenu.build()).queue();
	}

	@Override
	public String description() {
		return "Select the days you wish to receive the notification.";
	}

}
