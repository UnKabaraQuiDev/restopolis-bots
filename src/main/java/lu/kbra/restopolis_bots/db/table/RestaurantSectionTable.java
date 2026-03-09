package lu.kbra.restopolis_bots.db.table;

import java.util.List;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Component;

import lu.kbra.pclib.db.base.DataBase;
import lu.kbra.pclib.db.exception.DBException;
import lu.kbra.pclib.db.table.DeferredDataBaseTable;
import lu.kbra.restopolis_bots.db.data.RestaurantSectionData;

@Component
public class RestaurantSectionTable extends DeferredDataBaseTable<RestaurantSectionData> {

	public RestaurantSectionTable(DataBase dataBase) {
		super(dataBase);
	}

	@Cacheable(cacheNames = "restaurant-section.id")
	public RestaurantSectionData byId(long id) {
		return super.load(new RestaurantSectionData(id));
	}

	public List<RestaurantSectionData> byRestaurant(long id) {
		return super.loadByUnique(new RestaurantSectionData(id, null));
	}

	@Cacheable(cacheNames = "restaurant-section.exists.id")
	public boolean exists(int id) {
		return exists(new RestaurantSectionData(id));
	}

	@Caching(
			evict = {
					@CacheEvict(cacheNames = "restaurant-section.id", key = "#data.id"),
					@CacheEvict(cacheNames = "restaurant-section.exists.id", allEntries = true) }
	)
	@Override
	public RestaurantSectionData insertAndReload(RestaurantSectionData data) throws DBException {
		return super.insertAndReload(data);
	}

	@Caching(evict = { @CacheEvict(cacheNames = "restaurant-section.id", key = "#data.id") })
	@Override
	public RestaurantSectionData updateAndReload(RestaurantSectionData data) throws DBException {
		return super.updateAndReload(data);
	}

	@Cacheable(cacheNames = "restaurant-section.id", key = "#data.id")
	@Override
	public RestaurantSectionData loadIfExistsElseInsert(RestaurantSectionData data) throws DBException {
		return super.loadIfExistsElseInsert(data);
	}

}
