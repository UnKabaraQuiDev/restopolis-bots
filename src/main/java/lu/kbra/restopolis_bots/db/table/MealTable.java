package lu.kbra.restopolis_bots.db.table;

import org.springframework.stereotype.Component;

import lu.kbra.pclib.db.base.DataBase;
import lu.kbra.pclib.db.table.DeferredDataBaseTable;
import lu.kbra.restopolis_bots.db.data.MealData;

@Component
public class MealTable extends DeferredDataBaseTable<MealData> {

	public MealTable(DataBase dataBase) {
		super(dataBase);
	}

}
