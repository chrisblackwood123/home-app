package com.chrisblackwood.home.dto;

import java.util.List;

public record PushoverResponse(
        int status,
        String request,
        List<String> errors,
        String message,
        String user
) {}
