import java.io.IOException;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import lu.kbra.restopolis_bots.RBMain;
import lu.kbra.restopolis_bots.db.data.RestaurantData;
import lu.kbra.restopolis_bots.db.table.RestaurantTable;
import lu.kbra.restopolis_bots.discord.DiscordSchedule;
import lu.kbra.restopolis_bots.scheduled.RestopolisFetcher;
import lu.kbra.restopolis_bots.scheduled.WhatsappSchedule;
import lu.rescue_rush.spring.jda.DiscordSenderService;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(classes = RBMain.class)
public class RBManual {

	@Autowired
	private RestopolisFetcher restopolisFetcher;
	@Autowired
	private RestaurantTable restaurantTable;

	@Autowired(required = false)
	private DiscordSenderService discordSenderService;
	@Autowired(required = false)
	private DiscordSchedule discordSchedule;

	@Autowired(required = false)
	private WhatsappSchedule whatsappSchedule;

	@BeforeAll
	@Disabled
	public void waitForJDA() throws IOException {
		if (discordSenderService != null) {
			discordSenderService.awaitJDAReady();
		}
		restopolisFetcher.runListFetch();
	}

	@Test
//	@Disabled
	public void testSite() throws IOException {
		System.err.println("fetching sites");
		restopolisFetcher.runListFetch();
	}

	@Test
	@Disabled
	public void fetchMenu() {
		System.err.println("fetching menu");
		final RestaurantData rd = restaurantTable.byId(1198);
		restopolisFetcher.fetchForRestaurant(rd);
	}

	@Test
	@Disabled
	public void fetchMenus() {
		System.err.println("fetching menus");
		restopolisFetcher.runMenuFetch();
	}

	@Test
	@Disabled
	public void testMessageDiscord() throws InterruptedException {
		System.err.println("running discord targets");
		discordSchedule.runTargets();

		if (discordSenderService != null) {
			System.err.println("waiting for messages");
			discordSenderService.shutdown();
		}
	}

	@Test
//	@Disabled
	public void testMessageWhatsapp() throws InterruptedException {
		System.err.println("running whatsapp targets");
		whatsappSchedule.runTargets();

		Thread.sleep(2000);
	}

}
