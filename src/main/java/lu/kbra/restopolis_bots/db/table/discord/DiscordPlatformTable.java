package lu.kbra.restopolis_bots.db.table.discord;

import org.springframework.stereotype.Component;

import lu.kbra.pclib.db.base.DataBase;
import lu.kbra.pclib.db.table.DeferredDataBaseTable;
import lu.kbra.restopolis_bots.data.TargetPlatform;
import lu.kbra.restopolis_bots.db.data.discord.DiscordPlatformData;

@Component
public class DiscordPlatformTable extends DeferredDataBaseTable<DiscordPlatformData> implements TargetPlatformTable<DiscordPlatformData> {

	public DiscordPlatformTable(DataBase dataBase) {
		super(dataBase);
	}

	@Override
	public TargetPlatform getTargetPlatform() {
		return TargetPlatform.DISCORD;
	}

}
