package lu.kbra.restopolis_bots.whatsapp;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/whatsapp")
@Profile("!noWhatsapp")
public class WhatsappController {

	@Autowired
	private WhatsappService whatsappService;
	
	@PostMapping("/webhook")
	public ResponseEntity<Void> webhook(@RequestBody Map<String, Object> payload) {
		System.out.println("WAHA webhook: " + payload);
		return ResponseEntity.ok().build();
	}

}
