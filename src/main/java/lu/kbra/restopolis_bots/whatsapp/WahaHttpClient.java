package lu.kbra.restopolis_bots.whatsapp;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import lu.kbra.restopolis_bots.config.WahaConfigData;

@Component
public class WahaHttpClient {

	private final RestTemplate restTemplate;
	private final WahaConfigData configData;
	private final String session = "default";

	public WahaHttpClient(final WahaConfigData configData) {
		this.restTemplate = new RestTemplate();
		this.configData = configData;
	}

	private HttpHeaders makeHeaders() {
		final HttpHeaders h = new HttpHeaders();
		h.setContentType(MediaType.APPLICATION_JSON);
		h.set("X-Api-Key", configData.getKey());
		return h;
	}

	public String sendText(/* final String session, */final String chatId, final String text) {
		final String url = configData.getUrl() + "/sendText";
		final Map<String, Object> body = Map.of("session", session, "chatId", chatId, "text", text);
		final HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, makeHeaders());
		return restTemplate.postForObject(url, entity, String.class);
	}

//	@Cacheable
	public boolean isSenderAdmin(/* final String session, */final String groupId, final String sender) {
		final String url = configData.getUrl() + "/groups/" + URLEncoder.encode(groupId, StandardCharsets.UTF_8)
				+ "/participants/v2?session=" + session;
		final ResponseEntity<List<Map<String, Object>>> response = restTemplate
				.exchange(url, HttpMethod.GET, new HttpEntity<>(makeHeaders()), new ParameterizedTypeReference<>() {
				});
		for (final Map<String, Object> p : response.getBody()) {
			final String id = (String) p.get("id");
			final String role = (String) p.get("role");
			if (sender.equals(id) && ("admin".equals(role) || "superadmin".equals(role))) {
				return true;
			}
		}
		return false;
	}

}