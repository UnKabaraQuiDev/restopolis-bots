package lu.kbra.restopolis_bots.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import lu.kbra.restopolis_bots.RBMain;
import lu.kbra.restopolis_bots.discord.DiscordSchedule;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;

@Configuration
@Profile("!noDiscord")
@ComponentScan(basePackageClasses = DiscordSchedule.class)
public class DiscordConfig {

	@Bean
	public JDA jdaConfig(@Value("${discord.token}") String token) throws InterruptedException {
		if (token == null || token.isBlank()) {
			throw new IllegalStateException("Expecting discord token (" + RBMain.CONFIG_FILE.toFile().getAbsolutePath() + ")");
		}
		final JDA jda = JDABuilder
				.createDefault(token)
				.setActivity(Activity.playing("Rating restopolis food!").withState("3/5 🌟"))
				.build();
		return jda;
	}

}
