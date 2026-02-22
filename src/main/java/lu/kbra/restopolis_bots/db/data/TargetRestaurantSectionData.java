package lu.kbra.restopolis_bots.db.data;

import lu.kbra.pclib.db.autobuild.column.Column;
import lu.kbra.pclib.db.autobuild.column.ForeignKey;
import lu.kbra.pclib.db.autobuild.column.PrimaryKey;
import lu.kbra.pclib.db.impl.DataBaseEntry;
import lu.kbra.restopolis_bots.db.table.RestaurantSectionTable;
import lu.kbra.restopolis_bots.db.table.RestaurantTable;
import lu.kbra.restopolis_bots.db.table.TargetTable;

public class TargetRestaurantSectionData implements DataBaseEntry {

	@PrimaryKey
	@Column
	@ForeignKey(table = TargetTable.class)
	protected long targetId;

	@PrimaryKey
	@Column
	@ForeignKey(table = RestaurantTable.class)
	protected long restaurantId;

	@PrimaryKey
	@Column
	@ForeignKey(table = RestaurantSectionTable.class)
	protected long restaurantSectionId;

}
