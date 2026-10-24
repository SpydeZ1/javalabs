package org.example.dto;

import jakarta.validation.constraints.NotBlank;

public class RequestChangesRequest {

    @NotBlank
    private String content;

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
