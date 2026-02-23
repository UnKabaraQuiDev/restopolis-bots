package lu.kbra.restopolis_bots.cmd;

import org.springframework.stereotype.Component;

import lu.rescue_rush.spring.jda.command.slash.SlashCommandAutocomplete;
import lu.rescue_rush.spring.jda.command.slash.SlashCommandExecutor;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

@Component
public class SelectCmd implements SlashCommandExecutor, SlashCommandAutocomplete {

	@Override
	public void execute(SlashCommandInteractionEvent event) {

	}

	@Override
	public void complete(CommandAutoCompleteInteractionEvent event) {

	}

	@Override
	public String description() {
		return "Choose the restaurants you want updates from.";
	}

}
