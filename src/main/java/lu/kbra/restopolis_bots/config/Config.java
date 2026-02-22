package lu.kbra.restopolis_bots.config;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

import lu.kbra.restopolis_bots.data.TargetPlatform;
import lu.kbra.restopolis_bots.db.table.discord.TargetPlatformTable;

@Configuration
public class Config {

	@Lazy
	@Bean
	public Map<TargetPlatform, TargetPlatformTable<?>> targetPlatformTables(final List<TargetPlatformTable<?>> tables) {
		return tables.stream().collect(Collectors.toMap(TargetPlatformTable::getTargetPlatform, Function.identity()));
	}

}
