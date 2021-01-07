/*
 * Copyright 2020-2021 the original author or authors.
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
package org.modelmapper.spring.data;

import lombok.Setter;
import lombok.Value;

import java.net.URI;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.springframework.util.Assert;
import org.springframework.web.util.UriTemplate;

/**
 * An {@link IdentifierProcessor} that allows to register {@link UriTemplate}s for aggregate types so that the model
 * mapping is able to extract the actual aggregate identifier from URIs.
 *
 * @author Oliver Drotbohm
 */
public class UriTemplateIdentifierProcessor implements AggregateIdentifierProcessor {

	private final Map<Class<?>, ExtractionConfiguration> configuration = new HashMap<>();

	private @Setter boolean onlyApplyToUri;

	/**
	 * Registers the given URI template for the given aggregate type. Assumes a single URI template parameter be in place.
	 *
	 * @param type must not be {@literal null}.
	 * @param uriTemplate must not be {@literal null} or empty.
	 * @return will never be {@literal null}.
	 */
	public UriTemplateIdentifierProcessor register(Class<?> type, String uriTemplate) {

		Assert.notNull(type, "Aggreate type must not be null!");
		Assert.hasText(uriTemplate, "UriTemplate must not be null or empty!");

		return register(type, new UriTemplate(uriTemplate));
	}

	/**
	 * Registers the given URI template for the given aggregate type. Assumes a single URI template parameter be in place.
	 *
	 * @param type must not be {@literal null}.
	 * @param uriTemplate must not be {@literal null}.
	 * @return will never be {@literal null}.
	 */
	public UriTemplateIdentifierProcessor register(Class<?> type, UriTemplate template) {

		Assert.notNull(type, "Aggreate type must not be null!");
		Assert.notNull(template, "UriTemplate must not be null!");

		List<String> variableNames = template.getVariableNames();
		Assert.isTrue(variableNames.size() == 1, "Template must contain a single template variable.");

		return register(type, template, variableNames.get(0));
	}

	/**
	 * Registers the given {@link UriTemplate} and template variable name for the given aggregate type.
	 *
	 * @param type must not be {@literal null}.
	 * @param template must not be {@literal null}.
	 * @param variableName must not be {@literal null} or empty.
	 * @return will never be {@literal null}.
	 */
	public UriTemplateIdentifierProcessor register(Class<?> type, UriTemplate template, String variableName) {

		Assert.notNull(type, "Aggregate type must not be null!");
		Assert.notNull(template, "UriTemplate must not be null!");
		Assert.hasText(variableName, "Variable name must not be null or empty!");

		Function<Object, Object> uriCreator = it -> {

			Map<String, Object> parameters = new HashMap<>();
			parameters.put(variableName, it);

			return template.expand(parameters);
		};

		return register(type, template, variableName, uriCreator);
	}

	/*
	 * (non-Javadoc)
	 * @see org.modelmapper.spring.data.IdentifierProcessor#preProcessIdentifier(java.lang.Object, java.lang.Class)
	 */
	@Override
	public Object preProcessIdentifier(Object identifier, Class<?> targetType) {

		if (onlyApplyToUri && !URI.class.isInstance(identifier)) {
			return identifier;
		}

		ExtractionConfiguration configuration = this.configuration.get(targetType);

		if (configuration == null) {
			return identifier;
		}

		Map<String, String> variables = configuration.template.match(identifier.toString());

		return variables.get(configuration.variableName);
	}

	/*
	 * (non-Javadoc)
	 * @see org.modelmapper.spring.data.IdentifierProcessor#postProcessIdentifier(java.lang.Object, java.lang.Class, java.lang.Class)
	 */
	@Override
	public Object postProcessIdentifier(Object identifier, Class<?> sourceType, Class<?> targetType) {

		if (onlyApplyToUri && !URI.class.isAssignableFrom(targetType)) {
			return identifier;
		}

		ExtractionConfiguration configuration = this.configuration.get(sourceType);

		return configuration == null
				? identifier
				: configuration.uriCreator.apply(identifier);
	}

	/*
	 * (non-Javadoc)
	 * @see org.modelmapper.spring.data.AggregateIdentifierProcessor#getAdditionalIdentifierTypes()
	 */
	@Override
	public Collection<Class<?>> getAdditionalIdentifierTypes() {
		return Arrays.asList(URI.class);
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.plugin.core.Plugin#supports(java.lang.Object)
	 */
	public boolean supports(Class<?> delimiter) {

		return configuration.keySet().stream()
				.anyMatch(delimiter::isAssignableFrom);
	}

	@SuppressWarnings("unchecked")
	private <T> UriTemplateIdentifierProcessor register(Class<?> type, UriTemplate template, String variableName,
			Function<T, Object> uriCreator) {

		Assert.notNull(type, "Aggregate type must not be null!");
		Assert.notNull(template, "UriTemplate must not be null!");
		Assert.hasText(variableName, "Variable name must not be null or empty!");

		configuration.put(type, ExtractionConfiguration.of(template, variableName, (Function<Object, Object>) uriCreator));

		return this;
	}

	@Value(staticConstructor = "of")
	private static class ExtractionConfiguration {

		UriTemplate template;
		String variableName;
		Function<Object, Object> uriCreator;
	}
}
