package lu.kbra.restopolis_bots.db.table;

import org.springframework.stereotype.Component;

import lu.kbra.pclib.db.base.DataBase;
import lu.kbra.pclib.db.table.DeferredDataBaseTable;
import lu.kbra.restopolis_bots.db.data.RestaurantSiteData;

@Component
public class RestaurantSiteTable extends DeferredDataBaseTable<RestaurantSiteData> {

	public RestaurantSiteTable(DataBase dataBase) {
		super(dataBase);
	}

}
