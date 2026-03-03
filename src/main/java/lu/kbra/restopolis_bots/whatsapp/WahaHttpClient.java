package lu.kbra.restopolis_bots.whatsapp;

import java.util.List;
import java.util.Map;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;

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
		h.set("X-Api-Key", this.configData.getKey());
		return h;
	}

	public String sendText(final String chatId, final String text) {
		final String url = this.configData.getUrl() + "/sendText";
		final Map<String, Object> body = Map.of("session", this.session, "chatId", chatId, "text", text);
		final HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, this.makeHeaders());
		return this.restTemplate.postForObject(url, entity, String.class);
	}

	public String sendText(final String chatId, final String text, List<String> mentions) {
		final String url = this.configData.getUrl() + "/sendText";
		final Map<String, Object> body = Map.of("session", this.session, "chatId", chatId, "text", text, "mentions", mentions);
		final HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, this.makeHeaders());
		return this.restTemplate.postForObject(url, entity, String.class);
	}

	public JsonNode sendPoll(final String chatId, final String question, final List<String> options, final boolean multipleAnswers) {
		final String url = this.configData.getUrl() + "/sendPoll";

		final Map<String, Object> poll = Map.of("name", question, "options", options, "multipleAnswers", multipleAnswers);
		final Map<String, Object> body = Map.of("session", this.session, "chatId", chatId, "poll", poll);

		final HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, this.makeHeaders());
		return this.restTemplate.postForObject(url, entity, JsonNode.class);
	}

	public boolean isSenderAdmin(final String groupId, String sender) {
		if (sender.contains("@lid")) {
			sender = resolveSenderPN(sender);
		}
		if (sender == null || sender.isBlank()) {
			return false;
		}
		final String url = this.configData.getUrl() + "/" + this.session + "/groups/" + groupId + "/participants/v2";
		final ResponseEntity<List<Map<String, Object>>> response = this.restTemplate
				.exchange(url, HttpMethod.GET, new HttpEntity<>(this.makeHeaders()), new ParameterizedTypeReference<>() {
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

	public String resolveSenderPN(String sender) {
		return this.restTemplate
				.exchange(configData.getUrl() + "/" + session + "/lids/" + sender,
						HttpMethod.GET,
						new HttpEntity<>(this.makeHeaders()),
						JsonNode.class)
				.getBody()
				.at("/pn")
				.asText();
	}

	public String sendPollVote(String chatId, String pollMessageId, List<String> choices) {
		final String url = this.configData.getUrl() + "/sendPollVote";

		final Map<String, Object> body = Map
				.of("session", this.session, "chatId", chatId, "pollMessageId", pollMessageId, "votes", choices);

		final HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, this.makeHeaders());
		return this.restTemplate.postForObject(url, entity, String.class);
	}

}