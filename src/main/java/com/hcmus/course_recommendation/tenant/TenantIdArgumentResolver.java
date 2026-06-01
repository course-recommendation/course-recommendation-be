package com.hcmus.course_recommendation.tenant;

import java.security.Principal;

import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import com.hcmus.course_recommendation.common.exception.GlobalErrorCode;
import com.hcmus.course_recommendation.common.exception.NotFoundException;
import com.hcmus.course_recommendation.user.User;
import com.hcmus.course_recommendation.user.UserRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class TenantIdArgumentResolver implements HandlerMethodArgumentResolver {
	private final UserRepository userRepository;

	@Override
	public boolean supportsParameter(MethodParameter parameter) {
		return parameter.hasParameterAnnotation(TenantId.class) && (Long.class.equals(parameter.getParameterType())
			|| long.class.equals(parameter.getParameterType()));
	}

	@Override
	public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
		NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
		Principal principal = webRequest.getUserPrincipal();
		if (principal == null || principal.getName() == null || principal.getName().isBlank()) {
			throw new NotFoundException(GlobalErrorCode.USER_NOT_FOUND);
		}

		String userId = principal.getName();
		User user = userRepository.findById(userId)
			.orElseThrow(() -> new NotFoundException(GlobalErrorCode.USER_NOT_FOUND, userId));

		if (user.getTenantId() == null) {
			throw new NotFoundException(GlobalErrorCode.TENANT_NOT_FOUND);
		}

		return user.getTenantId();
	}
}
