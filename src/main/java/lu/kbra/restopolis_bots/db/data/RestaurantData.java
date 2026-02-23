package lu.kbra.restopolis_bots.db.data;

import java.util.Objects;

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

	public RestaurantData(final long id) {
		this.id = id;
	}

	public RestaurantData(final String name) {
		this.name = name;
	}

	public RestaurantData(final long id, final String name, final long restaurantSiteId) {
		this.id = id;
		this.name = name;
		this.restaurantSiteId = restaurantSiteId;
	}

	public long getId() {
		return this.id;
	}

	public String getName() {
		return this.name;
	}

	public long getRestaurantSiteId() {
		return this.restaurantSiteId;
	}

	@Override
	public String toString() {
		return "RestaurantData@" + System.identityHashCode(this) + " [id=" + this.id + ", name=" + this.name
				+ ", restaurantSiteId=" + this.restaurantSiteId + "]";
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.id);
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj)
			return true;
		if ((obj == null) || (this.getClass() != obj.getClass()))
			return false;
		final RestaurantData other = (RestaurantData) obj;
		return this.id == other.id;
	}

}
