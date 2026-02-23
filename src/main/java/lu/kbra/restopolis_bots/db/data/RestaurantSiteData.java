package lu.kbra.restopolis_bots.db.data;

import lu.kbra.pclib.db.autobuild.column.Column;
import lu.kbra.pclib.db.autobuild.column.PrimaryKey;
import lu.kbra.pclib.db.impl.DataBaseEntry;

public class RestaurantSiteData implements DataBaseEntry {

	@PrimaryKey
	@Column
	protected long id;

	public RestaurantSiteData() {
	}

	public RestaurantSiteData(long id) {
		this.id = id;
	}

}
