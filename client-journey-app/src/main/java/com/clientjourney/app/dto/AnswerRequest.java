package com.clientjourney.app.dto;

import io.micronaut.core.annotation.Introspected;

@Introspected
public record AnswerRequest(String answerCode, String answerValue) {
}
