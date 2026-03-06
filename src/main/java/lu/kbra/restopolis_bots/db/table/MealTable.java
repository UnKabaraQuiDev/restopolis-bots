package lu.kbra.restopolis_bots.db.table;

import java.time.LocalDate;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import lu.kbra.pclib.db.base.DataBase;
import lu.kbra.pclib.db.table.DeferredDataBaseTable;
import lu.kbra.restopolis_bots.db.data.MealData;

@Component
public class MealTable extends DeferredDataBaseTable<MealData> {

	public MealTable(DataBase dataBase) {
		super(dataBase);
	}

	@Cacheable(cacheNames = "meal.today-restaurantId")
	public MealData todayByRestaurant(long restaurantId) {
		return super.loadUnique(new MealData(restaurantId, LocalDate.now()));
	}

	@Scheduled(cron = "0 59 23 * * *")
	@CacheEvict(cacheNames = "meal.today-restaurantId", allEntries = true)
	public void clearCache() {
//		System.err.println("Created cache 'meal.today - restaurantId'.");
	}

}
