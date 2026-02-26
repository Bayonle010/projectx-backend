package com.project_x.notification.service;

import com.project_x.notification.model.MessageType;
import com.project_x.notification.model.Param;

import java.util.List;

public interface MessagingHandler {
    void sendEmailNotificationToQueue(List<String> recipientsEmails, List<String> cc, List<String> bcc, MessageType messageType,
                                      String messageContentOrTemplateFile, String subject, String sender, List<Param> params, boolean inAsyncThread);
}
