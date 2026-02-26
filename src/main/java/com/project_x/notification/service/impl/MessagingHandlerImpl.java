package com.project_x.notification.service.impl;

import com.project_x.notification.email.EmailService;
import com.project_x.notification.model.MessageType;
import com.project_x.notification.model.Param;
import com.project_x.notification.model.SendEmail;
import com.project_x.notification.service.MessagingHandler;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MessagingHandlerImpl implements MessagingHandler {
    private final EmailService emailService;

    public MessagingHandlerImpl(EmailService emailService) {
        this.emailService = emailService;
    }

    @Override
    public void sendEmailNotificationToQueue(List<String> recipientsEmails, List<String> cc, List<String> bcc, MessageType messageType, String messageContentOrTemplateFile, String subject, String sender, List<Param> params, boolean inAsyncThread) {
        SendEmail sendEmail = new SendEmail();
        sendEmail.setRecipientsEmails(recipientsEmails);
        sendEmail.setCc(cc);
        sendEmail.setBcc(bcc);
        sendEmail.setMessageType(messageType);
        sendEmail.setMessageContentOrTemplateName(messageContentOrTemplateFile);
        sendEmail.setSubject(subject);
        sendEmail.setSender(sender);
        sendEmail.setParams(params);

        if (inAsyncThread){
            publishEmailToQueueAsync(sendEmail);
        }else {
            publishEmailToQueue(sendEmail);
        }



    }

    private void publishEmailToQueue(SendEmail sendEmail) {
        emailService.sendEmailWithParamMap(sendEmail); // sync
    }

    @Async
    private void publishEmailToQueueAsync(SendEmail sendEmail) {
        emailService.sendEmailWithParamMap(sendEmail);
    }


}
