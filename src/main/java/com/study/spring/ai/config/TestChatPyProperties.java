package com.study.spring.ai.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "testchatpy")
public record TestChatPyProperties(String baseUrl) {
}

