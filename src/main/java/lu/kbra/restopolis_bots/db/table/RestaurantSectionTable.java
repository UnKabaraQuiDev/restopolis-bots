package lu.kbra.restopolis_bots.db.table;

import org.springframework.stereotype.Component;

import lu.kbra.pclib.db.base.DataBase;
import lu.kbra.pclib.db.table.DeferredDataBaseTable;
import lu.kbra.restopolis_bots.db.data.RestaurantSectionData;

@Component
public class RestaurantSectionTable extends DeferredDataBaseTable<RestaurantSectionData> {

	public RestaurantSectionTable(DataBase dataBase) {
		super(dataBase);
	}

}
