package lu.kbra.restopolis_bots.db.table;

import java.util.List;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import lu.kbra.pclib.db.base.DataBase;
import lu.kbra.pclib.db.table.DeferredDataBaseTable;
import lu.kbra.restopolis_bots.db.data.RestaurantSectionData;

@Component
public class RestaurantSectionTable extends DeferredDataBaseTable<RestaurantSectionData> {

	public RestaurantSectionTable(DataBase dataBase) {
		super(dataBase);
	}

	@Cacheable(cacheNames = "restaurant-section.id")
	public RestaurantSectionData byId(long restaurantSectionId) {
		return super.load(new RestaurantSectionData(restaurantSectionId));
	}

	public List<RestaurantSectionData> byRestaurant(long id) {
		return super.loadByUnique(new RestaurantSectionData(id, null));
	}

}
