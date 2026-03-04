package lu.kbra.restopolis_bots.discord.cmd;

import org.springframework.stereotype.Component;

import lu.rescue_rush.spring.jda.command.slash.SlashCommandExecutor;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

@Component("help")
public class HelpCmd implements SlashCommandExecutor {

	@Override
	public void execute(SlashCommandInteractionEvent event) {
		event.getHook().sendMessage("""
				__Commands:__
				* */select config [restaurant name]*: Select the sections for a restaurant.
				* */select remove [restaurant name]*: Returns a list of all the sections from that restaurant.
				* */unsubscribe*: Unsubscribe from all restaurants.
				* */schedule*: Select the days of the week you wish to receive updates.
				* */select show*: Shows the current config for this chat.

				__Server-only commands:__
				* */channel*: Select the channel for daily updates.
				* */role*: Select the role to be pinged.""").queue();
	}

	@Override
	public String description() {
		return "Get help.";
	}

}
