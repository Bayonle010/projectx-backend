package com.project_x.notification.email;

import com.project_x.notification.model.SendEmail;

public interface MailTransport {
    void send(SendEmail email, String htmlBody);
}
