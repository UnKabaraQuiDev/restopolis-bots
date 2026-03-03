package lu.kbra.restopolis_bots.db.table;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import lu.kbra.pclib.db.base.DataBase;
import lu.kbra.pclib.db.loader.BufferedPagedEnumeration;
import lu.kbra.pclib.db.table.DBException;
import lu.kbra.pclib.db.table.DeferredDataBaseTable;
import lu.kbra.restopolis_bots.data.TargetPlatform;
import lu.kbra.restopolis_bots.db.data.TargetData;
import lu.kbra.restopolis_bots.db.data.TargetPlatformData;

@Component
public class TargetTable extends DeferredDataBaseTable<TargetData> {

	@Autowired
	@Lazy
	private Map<TargetPlatform, TargetPlatformTable<?>> platformTables;

	public TargetTable(DataBase dataBase) {
		super(dataBase);
	}

	public BufferedPagedEnumeration<TargetData> all(TargetPlatform targetPlatform) {
		return new BufferedPagedEnumeration<>(20, this, cb -> cb.match("target_platform", "=", targetPlatform));
	}

	@Cacheable(cacheNames = "target.id")
	public TargetData byId(long id) {
		return super.load(new TargetData(id));
	}

	@Cacheable(cacheNames = "target.id", key = "#id.getId()")
	public TargetData byId(TargetPlatformData id) {
		return byId(id.getId());
	}

	@CacheEvict(cacheNames = "target.id", key = "#data.getId()")
	@Override
	public TargetData updateAndReload(TargetData data) {
		return super.updateAndReload(data);
	}

	@Override
	public TargetData insertAndReload(TargetData data) throws DBException {
		// TODO Auto-generated method stub
		return super.insertAndReload(data);
	}
}
