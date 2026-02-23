package lu.kbra.restopolis_bots.db.table;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import lu.kbra.pclib.db.base.DataBase;
import lu.kbra.pclib.db.table.DeferredDataBaseTable;
import lu.kbra.restopolis_bots.db.data.MealSectionData;

@Component
public class MealSectionTable extends DeferredDataBaseTable<MealSectionData> {

	public MealSectionTable(DataBase dataBase) {
		super(dataBase);
	}

	@Cacheable(cacheNames = "mealSection.mealId-restaurantSectionId")
	public MealSectionData byMealAndRestaurantSection(long mealId, long restaurantSectionId) {
		System.err.println(mealId + ", " + restaurantSectionId);
		return super.load(new MealSectionData(mealId, restaurantSectionId));
	}

}
