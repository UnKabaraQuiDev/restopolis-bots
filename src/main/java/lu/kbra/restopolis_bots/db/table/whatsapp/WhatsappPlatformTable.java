package lu.kbra.restopolis_bots.db.table.whatsapp;

import java.util.Optional;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import lu.kbra.pclib.db.base.DataBase;
import lu.kbra.pclib.db.table.DeferredDataBaseTable;
import lu.kbra.restopolis_bots.data.TargetPlatform;
import lu.kbra.restopolis_bots.db.data.whatsapp.WhatsappPlatformData;
import lu.kbra.restopolis_bots.db.table.TargetPlatformTable;

@Component
public class WhatsappPlatformTable extends DeferredDataBaseTable<WhatsappPlatformData>
		implements TargetPlatformTable<WhatsappPlatformData> {

	public WhatsappPlatformTable(DataBase dataBase) {
		super(dataBase);
	}

	@Override
	public TargetPlatform getTargetPlatform() {
		return TargetPlatform.WHATSAPP;
	}

	@Cacheable(cacheNames = "discordPlatform.serverId")
	public Optional<WhatsappPlatformData> byServer(long serverId) {
		return super.loadUniqueIfExists(new WhatsappPlatformData(Long.toString(serverId)));
	}

	@CacheEvict(cacheNames = "discordPlatform.serverId", key = "#data.serverId")
	@Override
	public WhatsappPlatformData updateAndReload(WhatsappPlatformData data) {
		return super.updateAndReload(data);
	}

}
