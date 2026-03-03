package lu.kbra.restopolis_bots.whatsapp;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile("!noWhatsapp")
public class WhatsappService {

}
