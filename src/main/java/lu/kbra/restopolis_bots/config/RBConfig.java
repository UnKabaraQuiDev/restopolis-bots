package lu.kbra.restopolis_bots.config;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.convert.ApplicationConversionService;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Primary;
import org.springframework.core.convert.ConversionService;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;

import lu.kbra.pclib.db.connector.DataBaseConnectorFactory;
import lu.kbra.pclib.db.connector.MySQLDataBaseConnector;
import lu.kbra.pclib.db.utils.SpringDataBaseEntryUtils;
import lu.kbra.restopolis_bots.data.TargetPlatform;
import lu.kbra.restopolis_bots.db.table.TargetPlatformTable;

@Configuration
@EnableScheduling
@EnableCaching
@EnableConfigurationProperties({ DbConfigData.class, WahaConfigData.class })
public class RBConfig {

	@Lazy
	@Bean
	public Map<TargetPlatform, TargetPlatformTable<?>> targetPlatformTables(final List<TargetPlatformTable<?>> tables) {
		return tables.stream().collect(Collectors.toMap(TargetPlatformTable::getTargetPlatform, Function.identity()));
	}

	@Bean
	public DataBaseConnectorFactory dbConnectorFactory(final DbConfigData config) {
		return () -> new MySQLDataBaseConnector(config.getUsername(), config.getPassword(), config.getHost(), config.getPort());
	}

	@Bean
	public SpringDataBaseEntryUtils defaultSpringDataBaseEntryUtils(
			final ObjectMapper objectMapper,
			final ConversionService conversionService) {
		return new SpringDataBaseEntryUtils(objectMapper, conversionService);
	}

	@Bean
	@Primary
	public ObjectMapper objectMapper() {
		return new ObjectMapper();
	}

	@Bean
	@Primary
	public ConversionService conversionService() {
		return new ApplicationConversionService();
	}

	@Bean
	public RestTemplate restTemplate() {
		final RestTemplate restTemplate = new RestTemplate();
		restTemplate.setRequestFactory(new SimpleClientHttpRequestFactory());
		return restTemplate;
	}

}
