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

	@Cacheable(cacheNames = "whatsappPlatform.chatId")
	public Optional<WhatsappPlatformData> byChat(String chatId) {
		return super.loadUniqueIfExists(new WhatsappPlatformData(chatId));
	}

	@CacheEvict(cacheNames = "whatsappPlatform.chatId", key = "#data.chatId")
	@Override
	public WhatsappPlatformData updateAndReload(WhatsappPlatformData data) {
		return super.updateAndReload(data);
	}

	@CacheEvict(cacheNames = "whatsappPlatform.chatId", key = "#data.chatId")
	@Override
	public WhatsappPlatformData insertAndReload(WhatsappPlatformData data) {
		return super.insertAndReload(data);
	}

}
