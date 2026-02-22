package lu.kbra.restopolis_bots.db.table;

import org.springframework.stereotype.Component;

import lu.kbra.pclib.db.base.DataBase;
import lu.kbra.pclib.db.table.DeferredDataBaseTable;
import lu.kbra.restopolis_bots.db.data.MealSectionData;

@Component
public class MealSectionTable extends DeferredDataBaseTable<MealSectionData> {

	public MealSectionTable(DataBase dataBase) {
		super(dataBase);
	}

}
