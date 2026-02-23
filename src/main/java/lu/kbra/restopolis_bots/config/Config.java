package lu.kbra.restopolis_bots.config;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.convert.ApplicationConversionService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.convert.ConversionService;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;

import lu.kbra.pclib.db.connector.DataBaseConnectorFactory;
import lu.kbra.pclib.db.connector.MySQLDataBaseConnector;
import lu.kbra.restopolis_bots.data.TargetPlatform;
import lu.kbra.restopolis_bots.db.table.discord.TargetPlatformTable;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;

@Configuration
@EnableScheduling
@EnableConfigurationProperties({ DbConfigData.class, DiscordConfigData.class })
public class Config {

	@Lazy
	@Bean
	public Map<TargetPlatform, TargetPlatformTable<?>> targetPlatformTables(final List<TargetPlatformTable<?>> tables) {
		return tables.stream().collect(Collectors.toMap(TargetPlatformTable::getTargetPlatform, Function.identity()));
	}

	@Bean
	public DataBaseConnectorFactory dbConnectorFactory(final DbConfigData config) {
		return () -> new MySQLDataBaseConnector(config.getUsername(), config.getPassword(), config.getHost(),
				config.getPort());
	}

	@Bean
	public ObjectMapper objectMapper() {
		return new ObjectMapper();
	}

	@Bean
	public ConversionService conversionService() {
		return new ApplicationConversionService();
	}

	@Bean
	public RestTemplate restTemplate() {
		final RestTemplate restTemplate = new RestTemplate();
		restTemplate.setRequestFactory(new SimpleClientHttpRequestFactory());
		return restTemplate;
	}

	@Bean
	public JDA jdaConfig(DiscordConfigData config) throws InterruptedException {
		final JDA jda = JDABuilder.createDefault(config.getToken()).setChunkingFilter(ChunkingFilter.ALL).build();

		return jda;
	}

}
