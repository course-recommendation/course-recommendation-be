package com.hcmus.course_recommendation.common.event;

import java.io.Serializable;

import org.springframework.context.ApplicationEvent;
import org.springframework.core.ResolvableType;
import org.springframework.core.ResolvableTypeProvider;

import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class NewResourceApplicationEvent<T extends Serializable> extends ApplicationEvent
	implements ResolvableTypeProvider {
	private final T data;

	public NewResourceApplicationEvent(Object source, T data) {
		super(source);
		this.data = data;
	}

	@Override
	public ResolvableType getResolvableType() {
		return ResolvableType.forClassWithGenerics(getClass(), ResolvableType.forInstance(data));
	}
}
