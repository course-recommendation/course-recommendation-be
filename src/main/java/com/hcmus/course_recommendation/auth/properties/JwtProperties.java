package com.hcmus.course_recommendation.auth.properties;

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "security.jwt")
public record JwtProperties(
	String secretKey,
	long expiration,
	RSAPrivateKey rsaPrivateKey,
	RSAPublicKey rsaPublicKey
) {
}
