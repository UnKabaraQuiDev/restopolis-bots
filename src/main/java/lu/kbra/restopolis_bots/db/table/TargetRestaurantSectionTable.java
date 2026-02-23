package lu.kbra.restopolis_bots.db.table;

import java.util.List;

import org.springframework.stereotype.Component;

import lu.kbra.pclib.db.autobuild.query.Query;
import lu.kbra.pclib.db.base.DataBase;
import lu.kbra.pclib.db.table.DeferredDataBaseTable;
import lu.kbra.restopolis_bots.db.data.TargetData;
import lu.kbra.restopolis_bots.db.data.TargetRestaurantSectionData;

@Component
public abstract class TargetRestaurantSectionTable extends DeferredDataBaseTable<TargetRestaurantSectionData> {

	public TargetRestaurantSectionTable(DataBase dataBase) {
		super(dataBase);
	}

	@Query(columns = { "target_id" })
	public abstract List<TargetRestaurantSectionData> byTarget(long id);

	public List<TargetRestaurantSectionData> byTarget(TargetData id) {
		return byTarget(id.getId());
	}

}
