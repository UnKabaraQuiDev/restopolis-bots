package lu.kbra.restopolis_bots.db.data;

import lu.kbra.pclib.db.autobuild.column.Column;
import lu.kbra.pclib.db.autobuild.column.PrimaryKey;
import lu.kbra.pclib.db.autobuild.column.Unique;
import lu.kbra.pclib.db.impl.DataBaseEntry;

public class RestaurantSiteData implements DataBaseEntry {

	@PrimaryKey
	@Column
	protected long id;

	@Column(length = 128)
	@Unique
	protected String name;

	public RestaurantSiteData() {
	}

	public RestaurantSiteData(long id) {
		this.id = id;
	}

	public RestaurantSiteData(long id, String name) {
		this.id = id;
		this.name = name;
	}

	@Override
	public String toString() {
		return "RestaurantSiteData@" + System.identityHashCode(this) + " [id=" + id + ", name=" + name + "]";
	}

}
