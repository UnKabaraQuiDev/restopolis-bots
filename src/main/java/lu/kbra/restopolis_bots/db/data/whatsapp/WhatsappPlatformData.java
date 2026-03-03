package lu.kbra.restopolis_bots.db.data.whatsapp;

import lu.kbra.pclib.db.autobuild.column.Column;
import lu.kbra.pclib.db.autobuild.column.ForeignKey;
import lu.kbra.pclib.db.autobuild.column.PrimaryKey;
import lu.kbra.pclib.db.autobuild.column.Unique;
import lu.kbra.restopolis_bots.db.data.TargetPlatformData;
import lu.kbra.restopolis_bots.db.table.TargetTable;

public class WhatsappPlatformData implements TargetPlatformData {

	@PrimaryKey
	@Column
	@ForeignKey(table = TargetTable.class)
	protected long id;

	@Column(length = 30)
	@Unique
	protected String chatId;

	public WhatsappPlatformData() {
	}

	public WhatsappPlatformData(long id) {
		this.id = id;
	}

	public WhatsappPlatformData(String chatId) {
		this.chatId = chatId;
	}

	public WhatsappPlatformData(long id, String chatId) {
		this.id = id;
		this.chatId = chatId;
	}

	@Override
	public long getId() {
		return id;
	}

	public String getChatId() {
		return chatId;
	}

	@Override
	public String toString() {
		return "WhatsappPlatformData [id=" + id + ", chatId=" + chatId + "]";
	}

}
