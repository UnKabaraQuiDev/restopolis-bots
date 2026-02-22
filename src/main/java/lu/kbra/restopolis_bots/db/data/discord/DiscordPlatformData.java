package lu.kbra.restopolis_bots.db.data.discord;

import lu.kbra.pclib.db.autobuild.column.Column;
import lu.kbra.pclib.db.autobuild.column.ForeignKey;
import lu.kbra.pclib.db.autobuild.column.Nullable;
import lu.kbra.pclib.db.autobuild.column.PrimaryKey;
import lu.kbra.pclib.db.autobuild.column.Unique;
import lu.kbra.restopolis_bots.db.table.TargetTable;

public class DiscordPlatformData implements TargetPlatformData {

	@PrimaryKey
	@Column
	@ForeignKey(table = TargetTable.class)
	protected long id;

	@Column(length = 20)
	@Unique
	protected String serverId;

	@Column
	protected String channelId;

	@Column
	@Nullable
	protected String roleId;

}
