package lu.kbra.restopolis_bots;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(
		basePackages = {
				"lu.kbra.restopolis_bots", // your main app
				"lu.rescue_rush.spring.jda" // the dependency
		}
)
public class RBMain {

	public static Path JAR_DIR;
	public static Path CONFIG_DIR;
	public static Path CONFIG_FILE;

	static {
		try {
			JAR_DIR = Paths.get(".").toRealPath();
			CONFIG_DIR = JAR_DIR.resolve("config/");
			CONFIG_FILE = CONFIG_DIR.resolve("application.properties");
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	public static void main(String[] args) throws IOException {
		if (!Files.exists(CONFIG_DIR)) {
			Files.createDirectories(CONFIG_DIR);
		}

		System.out.println("Config: " + CONFIG_FILE.toFile().getAbsolutePath());
		if (!Files.exists(CONFIG_FILE)) {
			Files.writeString(CONFIG_FILE, """
					db.username=user
					db.password=pass
					db.host=localhost
					# db.proto=mysql
					db.port=3306
					db.name=restopolis_bots
					discord.token=
										""");
			System.out.println("Created empty config to: " + CONFIG_FILE.toFile().getAbsolutePath());
			return;
		}

		final SpringApplication app = new SpringApplication(RBMain.class);
		app.run(args);
	}

}
