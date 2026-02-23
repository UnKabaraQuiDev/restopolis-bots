package lu.kbra.restopolis_bots.db.data;

import lu.kbra.pclib.db.autobuild.column.Column;
import lu.kbra.pclib.db.autobuild.column.ForeignKey;
import lu.kbra.pclib.db.autobuild.column.PrimaryKey;
import lu.kbra.pclib.db.autobuild.column.Unique;
import lu.kbra.pclib.db.impl.DataBaseEntry;
import lu.kbra.restopolis_bots.db.table.RestaurantSiteTable;

public class RestaurantData implements DataBaseEntry {

	@PrimaryKey
	@Column
	protected long id;

	@Column(length = 64)
	@Unique
	protected String name;

	@Column
	@ForeignKey(table = RestaurantSiteTable.class)
	protected long restaurantSiteId;

	public RestaurantData() {
	}

	public RestaurantData(long id) {
		this.id = id;
	}

	public RestaurantData(String name) {
		this.name = name;
	}

	public RestaurantData(long id, String name, long restaurantSiteId) {
		this.id = id;
		this.name = name;
		this.restaurantSiteId = restaurantSiteId;
	}

	public long getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public long getRestaurantSiteId() {
		return restaurantSiteId;
	}

	@Override
	public String toString() {
		return "RestaurantData@" + System.identityHashCode(this) + " [id=" + id + ", name=" + name
				+ ", restaurantSiteId=" + restaurantSiteId + "]";
	}

}
