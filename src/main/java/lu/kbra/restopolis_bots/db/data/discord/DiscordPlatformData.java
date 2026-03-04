package lu.kbra.restopolis_bots.db.data.discord;

import lu.kbra.pclib.db.autobuild.column.Column;
import lu.kbra.pclib.db.autobuild.column.ForeignKey;
import lu.kbra.pclib.db.autobuild.column.Nullable;
import lu.kbra.pclib.db.autobuild.column.PrimaryKey;
import lu.kbra.pclib.db.autobuild.column.Unique;
import lu.kbra.pclib.db.autobuild.table.ForeignKeyData.OnAction;
import lu.kbra.restopolis_bots.db.data.TargetPlatformData;
import lu.kbra.restopolis_bots.db.table.TargetTable;

public class DiscordPlatformData implements TargetPlatformData {

	@PrimaryKey
	@Column
	@ForeignKey(table = TargetTable.class, onDelete = OnAction.CASCADE)
	protected long id;

	// channel id for dm
	@Column(length = 20)
	@Unique
	protected String serverId;

	// user id for dm
	@Column(length = 20)
	protected String channelId;

	@Column(length = 20)
	@Nullable
	protected String roleId;

	@Column
	protected boolean dm = false;

	public DiscordPlatformData() {
	}

	public DiscordPlatformData(long id) {
		this.id = id;
	}

	public DiscordPlatformData(String serverId) {
		this.serverId = serverId;
	}

	public DiscordPlatformData(long id, String serverId, String channelId, String roleId, boolean dm) {
		this.id = id;
		this.serverId = serverId;
		this.channelId = channelId;
		this.roleId = roleId;
		this.dm = dm;
	}

	@Override
	public long getId() {
		return id;
	}

	public String getServerId() {
		if (dm) {
			throw new UnsupportedOperationException("Only usable in Server: " + toString());
		}
		return serverId;
	}

	public String getChannelId() {
		return dm ? serverId : channelId;
	}

	public String getRoleId() {
		if (dm) {
			throw new UnsupportedOperationException("Only usable in Server: " + toString());
		}
		return roleId;
	}

	public boolean isDm() {
		return dm;
	}

	public String getUserId() {
		if (!dm) {
			throw new UnsupportedOperationException("Only usable in DM: " + toString());
		}
		return channelId;
	}

	public void setRoleId(String roleId) {
		this.roleId = roleId;
	}

	public void setChannelId(String channelId) {
		if (dm) {
			this.serverId = channelId;
		} else {
			this.channelId = channelId;
		}
	}

	@Override
	public String toString() {
		return "DiscordPlatformData@" + System.identityHashCode(this) + " [id=" + id + ", serverId=" + serverId + ", channelId=" + channelId
				+ ", roleId=" + roleId + ", dm=" + dm + "]";
	}

}
