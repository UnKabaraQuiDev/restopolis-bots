package lu.kbra.restopolis_bots.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "waha")
public class WahaConfigData {

	private String url;
	private String key;

	public String getUrl() {
		return url;
	}

	public void setUrl(String host) {
		this.url = host;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

}