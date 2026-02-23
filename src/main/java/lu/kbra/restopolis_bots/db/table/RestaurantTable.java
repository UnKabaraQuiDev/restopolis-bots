package lu.kbra.restopolis_bots.db.table;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import lu.kbra.pclib.db.base.DataBase;
import lu.kbra.pclib.db.loader.BufferedPagedEnumeration;
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

}
