package lu.kbra.restopolis_bots;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class RBMain {

	public static final Path CONFIG_DIR = Paths.get("./config/");
	public static final Path CONFIG_FILE = CONFIG_DIR.resolve("application.properties");

	public static void main(String[] args) throws IOException {
		if (!Files.exists(CONFIG_DIR)) {
			Files.createDirectories(CONFIG_DIR);
		}

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
			System.out.println("Created empty config to: " + CONFIG_FILE);
			return;
		}

		SpringApplication.run(RBMain.class, args);
	}

}
