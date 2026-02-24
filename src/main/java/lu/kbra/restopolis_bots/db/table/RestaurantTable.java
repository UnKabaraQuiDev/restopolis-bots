package lu.kbra.restopolis_bots.db.table;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import lu.kbra.pclib.db.base.DataBase;
import lu.kbra.pclib.db.loader.BufferedPagedEnumeration;
import lu.kbra.pclib.db.query.QueryBuilder;
import lu.kbra.pclib.db.table.DeferredDataBaseTable;
import lu.kbra.restopolis_bots.db.data.RestaurantData;

@Component
public class RestaurantTable extends DeferredDataBaseTable<RestaurantData> {

	@Autowired
	@Lazy
	private TargetRestaurantSectionTable targetRestaurantSectionTable;
	@Autowired
	@Lazy
	private RestaurantSectionTable restaurantSectionTable;

	public RestaurantTable(DataBase dataBase) {
		super(dataBase);
	}

	public BufferedPagedEnumeration<RestaurantData> all() {
		return new BufferedPagedEnumeration<>(20, this);
	}

	@Cacheable(cacheNames = "restaurants.id")
	public RestaurantData byId(long restaurantId) {
		return super.load(new RestaurantData(restaurantId));
	}

	@Cacheable(cacheNames = "restaurants.likeName")
	public List<RestaurantData> likeName(String value, int maxChoices) {
		return super.query(
				QueryBuilder.<RestaurantData>select().limit(maxChoices).where(cb -> cb.match("name", "LIKE", "%" + value + "%")).list());
	}

	

	@Cacheable(cacheNames = "restaurants.name")
	public Optional<RestaurantData> byName(String restaurantName) {
		return super.loadUniqueIfExists(new RestaurantData(restaurantName));
	}

	@Caching(
			put = {
					@CachePut(cacheNames = "restaurants.id", key = "#data.id"),
					@CachePut(cacheNames = "restaurants.name", key = "#data.name") },
			evict = { @CacheEvict(cacheNames = "restaurants.likeName", allEntries = true) }
	)
	@Override
	public RestaurantData updateAndReload(RestaurantData data) {
		return super.updateAndReload(data);
	}

}
