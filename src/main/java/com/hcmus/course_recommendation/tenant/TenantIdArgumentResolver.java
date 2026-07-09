package com.hcmus.course_recommendation.tenant;

import java.net.URI;
import java.util.Objects;

import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import com.hcmus.course_recommendation.common.exception.GlobalErrorCode;
import com.hcmus.course_recommendation.common.exception.NotFoundException;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class TenantIdArgumentResolver implements HandlerMethodArgumentResolver {
	private final TenantRepository tenantRepository;

	@Override
	public boolean supportsParameter(MethodParameter parameter) {
		return parameter.hasParameterAnnotation(TenantId.class) && (Long.class.equals(parameter.getParameterType())
			|| long.class.equals(parameter.getParameterType()));
	}

	@Override
	public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
		NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
		HttpServletRequest request = webRequest.getNativeRequest(HttpServletRequest.class);
		if (request == null) {
			throw new NotFoundException(GlobalErrorCode.TENANT_NOT_FOUND);
		}

		String origin = Objects.requireNonNullElse(request.getHeader("Referer"), request.getHeader("Origin"));
		if (origin.isBlank()) {
			throw new NotFoundException(GlobalErrorCode.TENANT_NOT_FOUND);
		}

		String host;
		try {
			host = URI.create(origin).getHost();
		} catch (IllegalArgumentException e) {
			throw new NotFoundException(GlobalErrorCode.TENANT_NOT_FOUND);
		}

		if (host == null || host.isBlank()) {
			throw new NotFoundException(GlobalErrorCode.TENANT_NOT_FOUND);
		}

		return tenantRepository.findByName(host)
			.map(Tenant::getId)
			.orElseThrow(() -> new NotFoundException(GlobalErrorCode.TENANT_NOT_FOUND, host));
	}
}
