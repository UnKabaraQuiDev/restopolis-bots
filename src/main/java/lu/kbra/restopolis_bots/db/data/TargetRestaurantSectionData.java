package lu.kbra.restopolis_bots.db.data;

import lu.kbra.pclib.db.autobuild.column.Column;
import lu.kbra.pclib.db.autobuild.column.ForeignKey;
import lu.kbra.pclib.db.autobuild.column.PrimaryKey;
import lu.kbra.pclib.db.autobuild.table.ForeignKeyData.OnAction;
import lu.kbra.pclib.db.impl.DataBaseEntry;
import lu.kbra.restopolis_bots.db.table.RestaurantSectionTable;
import lu.kbra.restopolis_bots.db.table.TargetTable;

public class TargetRestaurantSectionData implements DataBaseEntry {

	@PrimaryKey
	@Column
	@ForeignKey(table = TargetTable.class, onDelete = OnAction.CASCADE)
	protected long targetId;

	@PrimaryKey
	@Column
	@ForeignKey(table = RestaurantSectionTable.class)
	protected long restaurantSectionId;

	public TargetRestaurantSectionData() {
	}

	public TargetRestaurantSectionData(long targetId, long restaurantSectionId) {
		this.targetId = targetId;
		this.restaurantSectionId = restaurantSectionId;
	}

	public long getTargetId() {
		return targetId;
	}

	public long getRestaurantSectionId() {
		return restaurantSectionId;
	}

	@Override
	public String toString() {
		return "TargetRestaurantSectionData@" + System.identityHashCode(this) + " [targetId=" + targetId
				+ ", restaurantSectionId=" + restaurantSectionId + "]";
	}

}
