package com.project_x.notification.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum MessageType {
    Template, Text;

    @JsonCreator
    public static MessageType jsonDecode(final String type) {return valueOf(type);}

    @JsonValue
    private String jsonEncode() {return name();}
}
