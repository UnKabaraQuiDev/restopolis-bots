package lu.kbra.restopolis_bots.whatsapp;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.JsonNode;

@RestController
@RequestMapping("/whatsapp")
@Profile("!noWhatsapp")
public class WhatsappController {

	@Autowired
	private WhatsappService whatsappService;

	@PostMapping("/webhook")
	public ResponseEntity<Void> webhook(@RequestBody JsonNode payload) {
		System.out.println(payload.path("event").asText() + ": " + payload.toString());

		if (payload.at("/payload/fromMe").asBoolean()) {
			return ResponseEntity.ok().build();
		}

		switch (payload.path("event").asText()) {
		case "message" -> whatsappService.incomingMessage(payload);
		case "poll.vote" -> whatsappService.pollVote(payload);
		case "group.join" -> whatsappService.groupJoin(payload);
		}

		return ResponseEntity.ok().build();
	}

}
