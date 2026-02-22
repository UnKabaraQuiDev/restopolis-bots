package lu.kbra.restopolis_bots.db.data;

import lu.kbra.pclib.db.autobuild.column.AutoIncrement;
import lu.kbra.pclib.db.autobuild.column.Column;
import lu.kbra.pclib.db.autobuild.column.PrimaryKey;
import lu.kbra.pclib.db.autobuild.column.Unique;
import lu.kbra.pclib.db.impl.DataBaseEntry;

public class RestaurantSectionData implements DataBaseEntry {

	@PrimaryKey
	@Column
	@AutoIncrement
	protected long id;

	@Column(length = 64)
	@Unique
	protected String name;

}
