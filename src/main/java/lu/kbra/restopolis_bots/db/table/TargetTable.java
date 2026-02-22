package lu.kbra.restopolis_bots.db.table;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import lu.kbra.pclib.db.base.DataBase;
import lu.kbra.pclib.db.table.DeferredDataBaseTable;
import lu.kbra.restopolis_bots.data.TargetPlatform;
import lu.kbra.restopolis_bots.db.data.TargetData;
import lu.kbra.restopolis_bots.db.table.discord.TargetPlatformTable;

@Component
public class TargetTable extends DeferredDataBaseTable<TargetData> {

	@Autowired
	@Lazy
	private Map<TargetPlatform, TargetPlatformTable<?>> platformTables;
	
	public TargetTable(DataBase dataBase) {
		super(dataBase);
	}

}
