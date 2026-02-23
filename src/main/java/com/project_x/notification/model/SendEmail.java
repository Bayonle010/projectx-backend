package com.project_x.notification.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.util.List;



@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@EqualsAndHashCode(callSuper = false)
public class SendEmail {
    private List<String> recipientsEmails;
    private List<String> cc;
    private List<String> bcc;
    private MessageType messageType;
    private String messageContentOrTemplateName;
    private String subject;
    private String sender;
    private List<Param>  params;
}
