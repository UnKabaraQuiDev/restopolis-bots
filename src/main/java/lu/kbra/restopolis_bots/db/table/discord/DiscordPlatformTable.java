package lu.kbra.restopolis_bots.db.table.discord;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import lu.kbra.pclib.db.base.DataBase;
import lu.kbra.pclib.db.table.DeferredDataBaseTable;
import lu.kbra.restopolis_bots.data.TargetPlatform;
import lu.kbra.restopolis_bots.db.data.discord.DiscordPlatformData;

@Component
public class DiscordPlatformTable extends DeferredDataBaseTable<DiscordPlatformData>
		implements TargetPlatformTable<DiscordPlatformData> {

	public DiscordPlatformTable(DataBase dataBase) {
		super(dataBase);
	}

	@Override
	public TargetPlatform getTargetPlatform() {
		return TargetPlatform.DISCORD;
	}

	@Cacheable(cacheNames = "discordPlatform.serverId")
	public DiscordPlatformData byServer(long serverId) {
		return super.loadUnique(new DiscordPlatformData(Long.toString(serverId)));
	}

	@CacheEvict(cacheNames = "discordPlatform.serverId", key = "#data.getServerId()")
	@Override
	public DiscordPlatformData updateAndReload(DiscordPlatformData data) {
		return super.updateAndReload(data);
	}

}
