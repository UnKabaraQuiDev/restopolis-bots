package lu.kbra.restopolis_bots.db.data;

import java.time.LocalDate;

import lu.kbra.pclib.db.autobuild.column.AutoIncrement;
import lu.kbra.pclib.db.autobuild.column.Column;
import lu.kbra.pclib.db.autobuild.column.ForeignKey;
import lu.kbra.pclib.db.autobuild.column.PrimaryKey;
import lu.kbra.pclib.db.autobuild.column.Unique;
import lu.kbra.pclib.db.impl.DataBaseEntry;
import lu.kbra.restopolis_bots.db.table.RestaurantTable;

public class MealData implements DataBaseEntry {

	@PrimaryKey
	@Column
	@AutoIncrement
	protected long id;

	@Column
	@Unique
	@ForeignKey(table = RestaurantTable.class)
	protected long restaurantId;

	@Column
	@Unique
	protected LocalDate date;

	public MealData() {
	}

	public MealData(long id) {
		this.id = id;
	}

	public MealData(long restaurantId, LocalDate date) {
		this.restaurantId = restaurantId;
		this.date = date;
	}

	public long getId() {
		return id;
	}

	public long getRestaurantId() {
		return restaurantId;
	}

	public LocalDate getDate() {
		return date;
	}

	@Override
	public String toString() {
		return "MealData@" + System.identityHashCode(this) + " [id=" + id + ", restaurantId=" + restaurantId + ", date="
				+ date + "]";
	}

}
