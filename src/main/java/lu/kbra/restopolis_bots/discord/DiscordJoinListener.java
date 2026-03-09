package lu.kbra.restopolis_bots.discord;

import org.springframework.context.annotation.Profile;

import lu.kbra.restopolis_bots.discord.cmd.HelpCmd;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

//@Component
@Profile("!noDiscord")
public class DiscordJoinListener extends ListenerAdapter {

	@Override
	public void onGuildJoin(GuildJoinEvent event) {
		if (event.getGuild().getSystemChannel() != null) {
			event.getGuild().getSystemChannel().sendMessage(HelpCmd.TEXT).queue();
		} else {
			event.getGuild().getDefaultChannel().asTextChannel().sendMessage(HelpCmd.TEXT).queue();
		}
	}

}
