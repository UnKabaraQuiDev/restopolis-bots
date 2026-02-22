package lu.kbra.restopolis_bots.db.table;

import org.springframework.stereotype.Component;

import lu.kbra.pclib.db.base.DataBase;
import lu.kbra.pclib.db.table.DeferredDataBaseTable;
import lu.kbra.restopolis_bots.db.data.TargetRestaurantSectionData;

@Component
public class TargetRestaurantSectionTable extends DeferredDataBaseTable<TargetRestaurantSectionData> {

	public TargetRestaurantSectionTable(DataBase dataBase) {
		super(dataBase);
	}

}
