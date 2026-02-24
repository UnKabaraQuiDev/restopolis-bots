package lu.kbra.restopolis_bots.db.ro_data;

import lu.kbra.pclib.db.autobuild.column.Column;
import lu.kbra.pclib.db.autobuild.column.ForeignKey;
import lu.kbra.pclib.db.autobuild.column.PrimaryKey;
import lu.kbra.pclib.db.autobuild.column.Unique;
import lu.kbra.pclib.db.impl.DataBaseEntry.ReadOnlyDataBaseEntry;
import lu.kbra.restopolis_bots.db.table.RestaurantSiteTable;
import lu.kbra.restopolis_bots.db.table.TargetTable;

public class TargetRestaurantROData implements ReadOnlyDataBaseEntry {

	@PrimaryKey
	@Column
	protected long id;

	@Column(length = 64)
	@Unique
	protected String name;

	@Column
	@ForeignKey(table = RestaurantSiteTable.class)
	protected long restaurantSiteId;

	@Column
	@ForeignKey(table = TargetTable.class)
	protected long targetId;

	public long getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public long getRestaurantSiteId() {
		return restaurantSiteId;
	}

	public long getTargetId() {
		return targetId;
	}

	@Override
	public String toString() {
		return "TargetRestaurantROData@" + System.identityHashCode(this) + " [id=" + id + ", name=" + name + ", restaurantSiteId="
				+ restaurantSiteId + ", targetId=" + targetId + "]";
	}

}
