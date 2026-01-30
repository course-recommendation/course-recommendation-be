package com.hcmus.course_recommendation.auth.config;

import java.time.Instant;

import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Service;

import com.hcmus.course_recommendation.auth.properties.JwtProperties;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class JwtService {

	private final JwtProperties jwtProperties;
	private final JwtEncoder encoder;
	private final JwtDecoder decoder;

	public String extractUsername(String token) {
		Jwt jwt = decoder.decode(token);
		return jwt.getSubject();
	}

	public String generateToken(String username) {
		Instant now = Instant.now();

		JwtClaimsSet claims = JwtClaimsSet.builder()
			.issuedAt(now)
			.expiresAt(now.plusMillis(jwtProperties.expiration()))
			.subject(username)
			.build();

		var encoderParameters = JwtEncoderParameters.from(
			JwsHeader.with(SignatureAlgorithm.RS256).build(), claims);

		return encoder.encode(encoderParameters).getTokenValue();
	}

	public boolean isTokenExpired(String token) {
		try {
			Jwt jwt = decoder.decode(token);
			Instant expiration = jwt.getExpiresAt();
			return expiration != null && expiration.isBefore(Instant.now());
		} catch (JwtException e) {
			return true;
		}
	}
}
