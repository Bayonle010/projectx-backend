package com.project_x.notification.email;

import com.project_x.notification.model.SendEmail;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Component
@ConditionalOnProperty(name = "mail.transport", havingValue = "api", matchIfMissing = true)
public class ZeptoApiMailTransport implements MailTransport{

    private final RestClient zeptoMailRestClient;

    @Value("${zeptomail.from.address:noreply@project-x-house.space}")
    private String defaultFrom;

    @Value("${zeptomail.include-text:true}")
    private boolean includeText;

    ZeptoApiMailTransport(@Qualifier("zeptomailRestClient") RestClient zeptoMailRestClient) {
        this.zeptoMailRestClient = zeptoMailRestClient;
    }


    @Override
    public void send(SendEmail email, String htmlBody) {
        // Build ZeptoMail payload
        String from = (email.getSender() != null && !email.getSender().isBlank())
                ? email.getSender() : defaultFrom;

        var toList = email.getRecipientsEmails().stream()
                .map(addr -> Map.of("email_address", Map.of("address", addr)))
                .toList();

        var ccList = email.getCc().stream()
                .map(addr -> Map.of("email_address", Map.of("address", addr)))
                .toList();

        var bccList = email.getBcc().stream()
                .map(addr -> Map.of("email_address", Map.of("address", addr)))
                .toList();

        var payload = new java.util.LinkedHashMap<String, Object>();
        payload.put("from", Map.of("address", from));
        payload.put("to", toList);
        if (!ccList.isEmpty()) payload.put("cc", ccList);
        if (!bccList.isEmpty()) payload.put("bcc", bccList);
        payload.put("subject", email.getSubject());
        payload.put("htmlbody", htmlBody);
        if (includeText) {
            payload.put("textbody",
                    htmlBody.replaceAll("<[^>]+>", " ").replaceAll("\\s+", " ").trim());
        }

        ResponseEntity<String> resp = zeptoMailRestClient.post()
                .uri("/email")
                .body(payload)
                .retrieve()
                .toEntity(String.class); // global defaultStatusHandler will throw on 4xx/5xx

                // log resp.getBody()

    }
}
