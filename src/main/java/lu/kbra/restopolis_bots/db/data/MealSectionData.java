package lu.kbra.restopolis_bots.db.data;

import java.util.List;

import lu.kbra.pclib.db.autobuild.column.Column;
import lu.kbra.pclib.db.autobuild.column.ForeignKey;
import lu.kbra.pclib.db.impl.DataBaseEntry;
import lu.kbra.restopolis_bots.db.table.MealTable;
import lu.kbra.restopolis_bots.db.table.RestaurantSectionTable;

public class MealSectionData implements DataBaseEntry {

	@ForeignKey(table = MealTable.class)
	@Column
	protected long mealId;

	@ForeignKey(table = RestaurantSectionTable.class)
	@Column
	protected long restaurantSectionId;

	@Column
	protected List<String> content;

}
