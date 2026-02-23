import java.io.IOException;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import lu.kbra.restopolis_bots.DiscordSchedule;
import lu.kbra.restopolis_bots.RBMain;
import lu.kbra.restopolis_bots.RestopolisFetcher;
import lu.rescue_rush.spring.jda.DiscordSenderService;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(classes = RBMain.class)
public class RBTest {

	@Autowired
	protected RestopolisFetcher restopolisFetcher;

	@Autowired(required = false)
	private DiscordSenderService discordSenderService;
	@Autowired(required = false)
	private DiscordSchedule discordSchedule;

	@BeforeAll
	public void waitForJDA() {
		if (discordSenderService != null) {
			discordSenderService.awaitJDAReady();
		}
	}

	@Test
	@Disabled
	public void testSite() throws IOException {
		System.err.println("fetching sites");
		restopolisFetcher.runListFetch();
	}

	@Test
	@Disabled
	public void fetchMenu() {
		System.err.println("fetching menu");
		restopolisFetcher.fetchForRestaurant(1198);
	}

	@Test
	@Disabled
	public void fetchMenus() {
		System.err.println("fetching menus");
		restopolisFetcher.runMenuFetch();
	}

	@Test
	public void testMessage() {
		discordSchedule.runTargets();
	}

}
