package lu.kbra.restopolis_bots.cmd;

import java.util.Collections;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import lu.kbra.restopolis_bots.data.TargetPlatform;
import lu.kbra.restopolis_bots.db.data.TargetData;
import lu.kbra.restopolis_bots.db.data.discord.DiscordPlatformData;
import lu.kbra.restopolis_bots.db.table.TargetTable;
import lu.kbra.restopolis_bots.db.table.discord.DiscordPlatformTable;
import lu.rescue_rush.spring.jda.menu.DiscordEntityMenu;
import lu.rescue_rush.spring.jda.menu.DiscordEntityMenuExecutor;
import net.dv8tion.jda.api.components.selections.EntitySelectMenu;
import net.dv8tion.jda.api.components.selections.EntitySelectMenu.DefaultValue;
import net.dv8tion.jda.api.components.selections.EntitySelectMenu.SelectTarget;
import net.dv8tion.jda.api.events.interaction.component.EntitySelectInteractionEvent;

@Component("role_select")
public class RoleSelectMenu implements DiscordEntityMenu, DiscordEntityMenuExecutor {

	private String beanName;

	@Autowired
	private DiscordPlatformTable discordPlatformTable;
	@Autowired
	private TargetTable targetTable;

	@Override
	public void execute(EntitySelectInteractionEvent event) {
		event.deferReply(true).queue();

		final DiscordPlatformData discordPlatformData = discordPlatformTable
				.byServer(event.isFromGuild() ? event.getGuild().getIdLong() : event.getChannelIdLong())
				.orElseGet(() -> {
					final TargetData targetData = targetTable
							.insertAndReload(new TargetData(TargetPlatform.DISCORD, Collections.emptyList()));
					return discordPlatformTable.insertAndReload(new DiscordPlatformData(targetData.getId(),
							event.isFromGuild() ? event.getGuild().getId() : event.getChannelId(),
							event.getChannelId(),
							null));
				});

		discordPlatformData.setRoleId(event.getMentions().getRoles().isEmpty() ? null : event.getMentions().getRoles().get(0).getId());
		discordPlatformTable.updateAndReload(discordPlatformData);
		event.getHook()
				.sendMessage("Updated role to: "
						+ (discordPlatformData.getRoleId() == null ? "*None*" : ("<@" + discordPlatformData.getRoleId() + ">")))
				.setEphemeral(true)
				.queue();
	}

	@Override
	public EntitySelectMenu build() {
		return build(null);
	}

	public EntitySelectMenu build(String roleId) {
		final EntitySelectMenu.Builder a = EntitySelectMenu.create(beanName, SelectTarget.ROLE)
				.setPlaceholder("Select the role")
				.setMinValues(0)
				.setMaxValues(1);
		if (roleId != null) {
			a.setDefaultValues(DefaultValue.role(roleId));
		}
		return a.build();
	}

	@Override
	public void setBeanName(String name) {
		this.beanName = name;
	}

}
