package com.hcmus.course_recommendation.common.event;

import java.io.Serializable;

import org.springframework.context.ApplicationEvent;
import org.springframework.core.ResolvableType;
import org.springframework.core.ResolvableTypeProvider;

import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class ModifiedResourceApplicationEvent<T extends Serializable> extends ApplicationEvent
	implements ResolvableTypeProvider {
	private final T newData;
	private final T oldData;

	public ModifiedResourceApplicationEvent(Object source, T oldData, T newData) {
		super(source);
		if (newData == null || oldData == null) {
			throw new IllegalArgumentException("oldData and newData must not be null");
		}
		this.newData = newData;
		this.oldData = oldData;
	}

	@Override
	public ResolvableType getResolvableType() {
		return ResolvableType.forClassWithGenerics(getClass(), ResolvableType.forInstance(newData));
	}
}