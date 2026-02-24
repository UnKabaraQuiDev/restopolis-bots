import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import lu.kbra.pclib.db.query.QueryBuilder;
import lu.kbra.restopolis_bots.RBMain;
import lu.kbra.restopolis_bots.db.data.RestaurantData;
import lu.kbra.restopolis_bots.db.table.RestaurantTable;
import lu.kbra.restopolis_bots.scheduled.RestopolisFetcher;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(classes = RBMain.class)
public class RBTests {

	@Autowired
	private RestopolisFetcher restopolisFetcher;

	@Autowired
	private RestaurantTable restaurantTable;

	@BeforeAll
	public void loadData() throws IOException {
		restopolisFetcher.runListFetch();
	}

	@Test
	public void testCache() {
		final long restaurantId = restaurantTable.query(QueryBuilder.<RestaurantData>select().limit(1).firstThrow()).getId();

		assertThat(restaurantTable.byId(restaurantId) == restaurantTable.byId(restaurantId));
	}

}
