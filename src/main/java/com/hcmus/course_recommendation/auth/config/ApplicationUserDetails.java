package com.hcmus.course_recommendation.auth.config;

import java.util.Collection;

import org.jspecify.annotations.NullMarked;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.hcmus.course_recommendation.user.User;

public record ApplicationUserDetails(User user) implements UserDetails {
	private static final String ROLE_PREFIX = "ROLE_";

	@Override
	@NullMarked
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return user.getRoles().stream().map(role -> new SimpleGrantedAuthority(ROLE_PREFIX + role.name())).toList();
	}

	@Override
	public String getPassword() {
		return user.getPassword();
	}

	@Override
	@NullMarked
	public String getUsername() {
		return user.getId();
	}
}
