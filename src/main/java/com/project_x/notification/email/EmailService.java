package com.project_x.notification.email;

import com.project_x.notification.model.MessageType;
import com.project_x.notification.model.SendEmail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Map;

public class EmailService {
    private final Logger log = LoggerFactory.getLogger(EmailService.class.getName());
    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Value("${spring.mail.properties.mail.smtp.from}")
    private String from;

    @Value("${spring.mail.properties.mail.smtp.display-name:PROJECTX}")
    private String fromDisplayName;

    @Value("${spring.mail.from.default:noreply@project-x-house.space}")
    private String defaultFrom;

    public EmailService(JavaMailSender mailSender, TemplateEngine templateEngine) {
        this.mailSender = mailSender;
        this.templateEngine = templateEngine;
    }

    public void sendEmailWithParamMap(SendEmail sendEmail) {
        try {
            // Render body (unchanged)
            String html;
            if (sendEmail.getMessageType().equals(MessageType.Template)) {
                Map<String, Object> paraMap = MessageTemplateFactory.getInstance()
                        .resolveParamsToMap(sendEmail.getParams());
                Context context = MessageTemplateFactory.getInstance()
                        .generateContextOutOfMap(paraMap);
                html = templateEngine.process(sendEmail.getMessageContentOrTemplateName(), context);
            } else {
                html = sendEmail.getMessageContentOrTemplateName();
            }

            // Ensure a From address is set (sender field from a call site)
            if (sendEmail.getSender() == null || sendEmail.getSender().isBlank()) {
                sendEmail.setSender(defaultFrom);
            }

            // Optional: add display name to params if your API transport supports it
            //sendEmail.getMeta().putIfAbsent("fromDisplayName", fromDisplayName);

            // Hand off to transport (API on Render free; SMTP elsewhere)
            mailTransport.send(sendEmail, html);

            logger.info("Email queued/sent to: {}", sendEmail.getRecipientsEmails());
        } catch (Exception e) {
            logger.error("Failed to send email to: {}", sendEmail.getRecipientsEmails(), e);
            throw new RuntimeException(e);
        }
    }
}
