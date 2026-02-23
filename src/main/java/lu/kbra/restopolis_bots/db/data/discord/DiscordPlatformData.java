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

	@Column(length = 20)
	protected String channelId;

	@Column(length = 20)
	@Nullable
	protected String roleId;

	public DiscordPlatformData() {
	}

	public DiscordPlatformData(long id) {
		this.id = id;
	}

	public DiscordPlatformData(String serverId) {
		this.serverId = serverId;
	}

	public DiscordPlatformData(String serverId, String channelId, String roleId) {
		this.serverId = serverId;
		this.channelId = channelId;
		this.roleId = roleId;
	}

	@Override
	public long getId() {
		return id;
	}

	public String getServerId() {
		return serverId;
	}

	public String getChannelId() {
		return channelId;
	}

	public String getRoleId() {
		return roleId;
	}

	@Override
	public String toString() {
		return "DiscordPlatformData@" + System.identityHashCode(this) + " [id=" + id + ", serverId=" + serverId
				+ ", channelId=" + channelId + ", roleId=" + roleId + "]";
	}

}
