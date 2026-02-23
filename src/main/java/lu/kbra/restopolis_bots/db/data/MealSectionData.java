package lu.kbra.restopolis_bots.db.data;

import java.util.List;

import lu.kbra.pclib.db.autobuild.column.Column;
import lu.kbra.pclib.db.autobuild.column.ForeignKey;
import lu.kbra.pclib.db.autobuild.column.PrimaryKey;
import lu.kbra.pclib.db.impl.DataBaseEntry;
import lu.kbra.restopolis_bots.db.table.MealTable;
import lu.kbra.restopolis_bots.db.table.RestaurantSectionTable;

public class MealSectionData implements DataBaseEntry {

	@PrimaryKey
	@ForeignKey(table = MealTable.class)
	@Column
	protected long mealId;

	@PrimaryKey
	@ForeignKey(table = RestaurantSectionTable.class)
	@Column
	protected long restaurantSectionId;

	@Column
	protected List<String> content;

	public MealSectionData() {
	}

	public MealSectionData(long mealId, long restaurantSectionId) {
		this.mealId = mealId;
		this.restaurantSectionId = restaurantSectionId;
	}

	public MealSectionData(long mealId, long restaurantSectionId, List<String> content) {
		this.mealId = mealId;
		this.restaurantSectionId = restaurantSectionId;
		this.content = content;
	}

	public long getMealId() {
		return mealId;
	}

	public long getRestaurantSectionId() {
		return restaurantSectionId;
	}

	public List<String> getContent() {
		return content;
	}

	@Override
	public String toString() {
		return "MealSectionData@" + System.identityHashCode(this) + " [mealId=" + mealId + ", restaurantSectionId="
				+ restaurantSectionId + ", content=" + content + "]";
	}

}
