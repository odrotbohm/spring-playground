/*
 * Copyright 2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.odrotbohm.spring.web.mvc;

import de.odrotbohm.spring.web.model.MappedPayloads;
import de.odrotbohm.spring.web.model.MappedPayloads.MappedPayload;
import lombok.RequiredArgsConstructor;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.function.Supplier;

import org.springframework.core.MethodParameter;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.validation.Errors;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.annotation.ErrorsMethodArgumentResolver;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;
import org.springframework.web.servlet.mvc.method.annotation.RequestResponseBodyMethodProcessor;

/**
 * {@link HandlerMethodArgumentResolver} for {@link MappedPayload} parameters in Spring MVC controllers. Delegates to
 * both the {@link RequestResponseBodyMethodProcessor} and {@link ErrorsMethodArgumentResolver} discovered from the
 * {@link RequestMappingHandlerAdapter} registered in the application for actual parameter binding and validation to
 * eventually create {@link MappedPayload} instance of the results.
 *
 * @author Oliver Drotbohm
 */
@RequiredArgsConstructor
public class MappedPayloadHandlerMethodArgumentResolver implements HandlerMethodArgumentResolver {

	private final Supplier<RequestMappingHandlerAdapter> adapter;
	private final MappedPayloadProperties configuration;

	private RequestResponseBodyMethodProcessor requestBodyResolver;
	private ErrorsMethodArgumentResolver errorsResolver;

	/*
	 * (non-Javadoc)
	 * @see org.springframework.web.method.support.HandlerMethodArgumentResolver#supportsParameter(org.springframework.core.MethodParameter)
	 */
	@Override
	public boolean supportsParameter(MethodParameter parameter) {
		return MappedPayload.class.isAssignableFrom(parameter.getParameterType());
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.web.method.support.HandlerMethodArgumentResolver#resolveArgument(org.springframework.core.MethodParameter, org.springframework.web.method.support.ModelAndViewContainer, org.springframework.web.context.request.NativeWebRequest, org.springframework.web.bind.support.WebDataBinderFactory)
	 */
	@Override
	public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
			NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {

		MethodParameter nested = configuration.isAlwaysValidate()
				? new AlwaysValidatingMethodParameter(parameter.nested())
				: parameter.nested();

		Object argument = getRequestBodyProcessor().resolveArgument(nested, mavContainer, webRequest, binderFactory);
		Errors errors = (Errors) getErrorsResolver().resolveArgument(nested, mavContainer, webRequest, binderFactory);

		return MappedPayloads.of(argument, errors);
	}

	private RequestResponseBodyMethodProcessor getRequestBodyProcessor() {

		if (requestBodyResolver == null) {
			this.requestBodyResolver = getArgumentResolver(RequestResponseBodyMethodProcessor.class);
		}

		return this.requestBodyResolver;
	}

	private ErrorsMethodArgumentResolver getErrorsResolver() {

		if (errorsResolver == null) {
			this.errorsResolver = getArgumentResolver(ErrorsMethodArgumentResolver.class);
		}

		return this.errorsResolver;
	}

	private <T> T getArgumentResolver(Class<T> type) {

		return adapter.get().getArgumentResolvers().stream()
				.filter(type::isInstance)
				.findFirst()
				.map(type::cast)
				.orElseThrow(
						() -> new IllegalArgumentException(String.format("Could not find resolver of type ", type.getName())));
	}

	/**
	 * Custom {@link MethodParameter} that implicitly adds an {@link Validated} annotation to the list of annotations
	 * returned for the parameter unless, there's already one declared.
	 *
	 * @author Oliver Drotbohm
	 */
	private static class AlwaysValidatingMethodParameter extends MethodParameter {

		private static final Annotation VALIDATED_ANNOTATION = AnnotationUtils.synthesizeAnnotation(Validated.class);

		public AlwaysValidatingMethodParameter(MethodParameter delegate) {
			super(delegate);
		}

		/*
		 * (non-Javadoc)
		 * @see org.springframework.core.MethodParameter#getParameterAnnotations()
		 */
		@Override
		public Annotation[] getParameterAnnotations() {

			Annotation[] annotations = super.getParameterAnnotations();

			return Arrays.stream(annotations)
					.map(Annotation::annotationType)
					.anyMatch(it -> it.getSimpleName().startsWith("Valid"))
							? annotations
							: ObjectUtils.addObjectToArray(annotations, VALIDATED_ANNOTATION);
		}
	}
}
