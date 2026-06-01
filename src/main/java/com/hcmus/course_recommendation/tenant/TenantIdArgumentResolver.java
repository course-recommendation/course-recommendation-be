package com.hcmus.course_recommendation.tenant;

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
		if (request == null || request.getServerName() == null || request.getServerName().isBlank()) {
			throw new NotFoundException(GlobalErrorCode.TENANT_NOT_FOUND);
		}

		String serverName = request.getServerName();
		return tenantRepository.findByName(serverName)
			.map(Tenant::getId)
			.orElseThrow(() -> new NotFoundException(GlobalErrorCode.TENANT_NOT_FOUND, serverName));
	}
}
