package lu.kbra.restopolis_bots.db.data;

import java.time.LocalDate;

import lu.kbra.pclib.db.autobuild.column.AutoIncrement;
import lu.kbra.pclib.db.autobuild.column.Column;
import lu.kbra.pclib.db.autobuild.column.PrimaryKey;
import lu.kbra.pclib.db.autobuild.column.Unique;
import lu.kbra.pclib.db.impl.DataBaseEntry;

public class MealData implements DataBaseEntry {

	@PrimaryKey
	@Column
	@AutoIncrement
	protected long id;

	@Column
	@Unique
	protected long restaurantId;

	@Column
	@Unique
	protected LocalDate date;

}
