package lu.kbra.restopolis_bots;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import lu.kbra.pclib.datastructure.pair.Pair;
import lu.rescue_rush.spring.jda.DiscordSenderService;

@Component
public class DiscordSchedule {

	@Autowired
	private DiscordSenderService discordSenderService;
	
	@Scheduled(cron = "0 0 9 * * *")
	public void runTargets() {

	}

	public void sendMessage(List<Pair<String, Map<String, String>>> maps) {
		final StringBuilder sb = new StringBuilder();
		final String[] wantedList = new String[0];

		final Instant now = Instant.now();
		final long unixSeconds = now.getEpochSecond();
		final String discordTime = "<t:" + unixSeconds + ":D>";
		sb.append("# " + discordTime + "\n");

		for (Pair<String, Map<String, String>> pairVal : maps) {
			sb.append("## " + pairVal.getKey() + "\n"); // restaurant name

			final Map<String, String> map = pairVal.getValue();
			for (String w : wantedList) {
				if (map.containsKey(w)) {
					sb.append("__**" + w + "**__\n"); // course title
					sb.append(map.get(w) + "\n");
				}
			}
		}
	}

}
