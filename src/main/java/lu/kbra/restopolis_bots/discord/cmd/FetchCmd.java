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
			event.getHook().editOriginal("You can't do that.").queue();
			return;
		}

		event.getHook().editOriginal("[0/2] Loading...").queue();
		try {
			restopolisFetcher.runListFetch();
			event.getHook().editOriginal("[1/2] Done fetching list.").queue();
			restopolisFetcher.runMenuFetch();
			event.getHook().editOriginal("[2/2] Done fetching menus.").queue();
		} catch (Exception e) {
			event.getHook().editOriginal("[:x:/2] Failed:\n" + PCUtils.toString(e)).queue();
		}
	}

	@Override
	public String description() {
		return "Admin-only command, ignore this.";
	}

}
