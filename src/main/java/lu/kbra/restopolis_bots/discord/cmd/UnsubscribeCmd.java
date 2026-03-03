package lu.kbra.restopolis_bots.discord.cmd;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import lu.kbra.restopolis_bots.db.data.TargetData;
import lu.kbra.restopolis_bots.db.data.discord.DiscordPlatformData;
import lu.kbra.restopolis_bots.db.table.TargetTable;
import lu.kbra.restopolis_bots.db.table.discord.DiscordPlatformTable;
import lu.rescue_rush.spring.jda.command.slash.SlashCommandExecutor;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

@Component("unsubscribe")
public class UnsubscribeCmd implements SlashCommandExecutor {

	@Autowired
	private DiscordPlatformTable discordPlatformTable;
	@Autowired
	private TargetTable targetTable;

	@Override
	public void execute(SlashCommandInteractionEvent event) {
		event.deferReply(true).queue();
		if (event.isFromGuild() && !event
				.getMember()
				.hasPermission(event.getGuildChannel(), Permission.ADMINISTRATOR, Permission.MANAGE_CHANNEL, Permission.MANAGE_SERVER)) {
			event.getHook().sendMessage("You don't have the permission to do that.").setEphemeral(true).queue();
			return;
		}
		final DiscordPlatformData discordPlatformData = discordPlatformTable
				.byServer(event.isFromGuild() ? event.getGuild().getIdLong() : event.getChannelIdLong())
				.orElse(null);
		if (discordPlatformData == null) {
			event.getHook().sendMessage("You weren't subscribed in the first place :'(").setEphemeral(true).queue();
			return;
		}
		final TargetData targetData = targetTable.byId(discordPlatformData.getId());
		discordPlatformTable.deleteIfExists(discordPlatformData);
		targetTable.deleteIfExists(targetData);
		event.getHook().sendMessage("Unsubscribed :sob:.").setEphemeral(true).queue();
	}

	@Override
	public String description() {
		return "Unsubscribe your server from our list.";
	}

}
