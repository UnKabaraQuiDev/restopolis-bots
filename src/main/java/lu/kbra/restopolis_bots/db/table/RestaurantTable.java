package lu.kbra.restopolis_bots.db.table;

import java.util.List;
import java.util.Optional;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import lu.kbra.pclib.db.base.DataBase;
import lu.kbra.pclib.db.loader.BufferedPagedEnumeration;
import lu.kbra.pclib.db.query.QueryBuilder;
import lu.kbra.pclib.db.table.DeferredDataBaseTable;
import lu.kbra.restopolis_bots.db.data.RestaurantData;

@Component
public class RestaurantTable extends DeferredDataBaseTable<RestaurantData> {

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

	public List<RestaurantData> likeName(String value, int maxChoices) {
		return super.query(
				QueryBuilder.<RestaurantData>select().limit(maxChoices).where(cb -> cb.match("name", "LIKE", "%" + value + "%")).list());
	}

	public Optional<RestaurantData> byName(String restaurantName) {
		return super.loadUniqueIfExists(new RestaurantData(restaurantName));
	}

}
