package lu.kbra.restopolis_bots.db.table.discord;

import java.util.Optional;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import lu.kbra.pclib.db.base.DataBase;
import lu.kbra.pclib.db.table.DeferredDataBaseTable;
import lu.kbra.restopolis_bots.data.TargetPlatform;
import lu.kbra.restopolis_bots.db.data.discord.DiscordPlatformData;
import lu.kbra.restopolis_bots.db.table.TargetPlatformTable;

@Component
public class DiscordPlatformTable extends DeferredDataBaseTable<DiscordPlatformData> implements TargetPlatformTable<DiscordPlatformData> {

	public DiscordPlatformTable(DataBase dataBase) {
		super(dataBase);
	}

	@Override
	public TargetPlatform getTargetPlatform() {
		return TargetPlatform.DISCORD;
	}

	@Cacheable(cacheNames = "discordPlatform.serverId")
	public Optional<DiscordPlatformData> byServer(long serverId) {
		return super.loadUniqueIfExists(new DiscordPlatformData(Long.toString(serverId)));
	}

	@CacheEvict(cacheNames = "discordPlatform.serverId", key = "#data.serverId")
	@Override
	public DiscordPlatformData updateAndReload(DiscordPlatformData data) {
		return super.updateAndReload(data);
	}

	@CacheEvict(cacheNames = "discordPlatform.serverId", key = "#data.serverId")
	@Override
	public DiscordPlatformData insertAndReload(DiscordPlatformData data) {
		return super.insertAndReload(data);
	}

}
