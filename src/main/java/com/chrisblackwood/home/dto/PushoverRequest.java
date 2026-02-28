package com.chrisblackwood.home.dto;

public record PushoverRequest (
        String token,
        String user,
        String message
) {}
