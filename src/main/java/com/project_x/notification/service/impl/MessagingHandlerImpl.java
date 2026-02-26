package com.project_x.notification.service.impl;

import com.project_x.notification.model.MessageType;
import com.project_x.notification.model.Param;
import com.project_x.notification.service.MessagingHandler;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MessagingHandlerImpl implements MessagingHandler {

    @Override
    public void sendEmailNotificationToQueue(List<String> recipientsEmails, List<String> cc, List<String> bcc, MessageType messageType, String messageContentOrTemplateFile, String subject, String sender, List<Param> params, boolean inAsyncThread) {

    }
}
