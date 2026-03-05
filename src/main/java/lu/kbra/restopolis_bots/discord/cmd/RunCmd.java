package lu.kbra.restopolis_bots.discord.cmd;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import lu.kbra.restopolis_bots.discord.DiscordSchedule;
import lu.rescue_rush.spring.jda.command.slash.SlashCommandExecutor;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

@Component("run")
@Profile("!noDiscord")
public class RunCmd implements SlashCommandExecutor {

	@Autowired
	private DiscordSchedule discordSchedule;

	@Value("${discord.admin.userId:nullllllll}")
	private String adminUserId;

	@Override
	public void execute(SlashCommandInteractionEvent event) {
		if (!event.getUser().getId().equals(adminUserId)) {
			event.reply("You can't do that.").setEphemeral(true).queue();
			return;
		}
		event.reply("OK.").setEphemeral(true).queue();
		discordSchedule.runTargets();
	}

	@Override
	public String description() {
		return "Admin-only command, ignore this.";
	}

}
