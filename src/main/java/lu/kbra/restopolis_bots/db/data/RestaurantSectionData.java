package lu.kbra.restopolis_bots.db.data;

import lu.kbra.pclib.db.autobuild.column.AutoIncrement;
import lu.kbra.pclib.db.autobuild.column.Column;
import lu.kbra.pclib.db.autobuild.column.ForeignKey;
import lu.kbra.pclib.db.autobuild.column.PrimaryKey;
import lu.kbra.pclib.db.autobuild.column.Unique;
import lu.kbra.pclib.db.impl.DataBaseEntry;
import lu.kbra.restopolis_bots.db.table.RestaurantTable;

public class RestaurantSectionData implements DataBaseEntry {

	@PrimaryKey
	@Column
	@AutoIncrement
	protected long id;

	@Column
	@Unique
	@ForeignKey(table = RestaurantTable.class)
	protected long restaurantId;

	@Column(length = 64)
	@Unique
	protected String name;

}
