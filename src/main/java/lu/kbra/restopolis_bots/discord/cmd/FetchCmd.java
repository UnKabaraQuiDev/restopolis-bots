package lu.kbra.restopolis_bots.discord.cmd;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import lu.kbra.pclib.PCUtils;
import lu.kbra.restopolis_bots.scheduled.RestopolisFetcher;
import lu.rescue_rush.spring.jda.command.slash.SlashCommandExecutor;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

@Component("fetch")
@Profile("!noDiscord")
public class FetchCmd implements SlashCommandExecutor {

	@Autowired
	private RestopolisFetcher restopolisFetcher;

	@Value("${discord.admin.userId:nullllllll}")
	private String adminUserId;

	@Override
	public void execute(SlashCommandInteractionEvent event) {
		event.deferReply(true).queue();
		if (!event.getUser().getId().equals(adminUserId)) {
			event.getHook().sendMessage("You can't do that.").setEphemeral(true).queue();
			return;
		}
		event.getHook().sendMessage("OK.").setEphemeral(true).queue();
		try {
			restopolisFetcher.runListFetch();
			event.getHook().sendMessage("Done fetching list.").setEphemeral(true).queue();
			restopolisFetcher.runMenuFetch();
			event.getHook().sendMessage("Done fetching menus.").setEphemeral(true).queue();
		} catch (Exception e) {
			event.getHook().sendMessage("Failed:\n" + PCUtils.toString(e)).setEphemeral(true).queue();
		}
	}

	@Override
	public String description() {
		return "Admin-only command, ignore this.";
	}

}
