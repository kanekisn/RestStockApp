package org.reststockapp.exceptions;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CustomErrorDetails {
    private String description;
    private String message;

    public CustomErrorDetails(String description, String message) {
        this.description = description;
        this.message = message;
    }
}