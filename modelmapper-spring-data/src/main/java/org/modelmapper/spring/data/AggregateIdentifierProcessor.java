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

import java.util.Collection;
import java.util.Collections;

import org.springframework.plugin.core.Plugin;

/**
 * SPI interface to customize identifier to aggregate mapping in {@link RepositoryMappingModule}.
 * {@link AggregareIdentifierProcessor} implementations can be registered with it to pre- and post process the source and target
 * identifier values obtained from source and target objects.
 *
 * @author Oliver Drotbohm
 */
public interface AggregateIdentifierProcessor extends Plugin<Class<?>> {

	/**
	 * Pre-process the identifier obtained from the source object to finally resolve an aggregate from the returned value.
	 *
	 * @param identifier the original identifier obtained from the source object. Must not be {@literal null}.
	 * @param targetType the target aggregate type the given identifier is supposed to eventually identify. Must not be
	 *          {@literal null}.
	 * @return will never be {@literal null}.
	 */
	default Object preProcessIdentifier(Object identifier, Class<?> targetType) {
		return identifier;
	}

	/**
	 * Post process the identifier obtained from the aggregate to finally constitute the value to be set on the target
	 * object.
	 *
	 * @param identifier the original identifier of the aggregate to be transformed into the target object's property
	 *          value. Must not be {@literal null}.
	 * @param sourceType the source aggregate type. Must not be {@literal null}.
	 * @param targetType the target type to create for this aggregate. Can be used to decide whether to actually apply
	 *          post-processing.
	 * @return will never be {@literal null}.
	 */
	default Object postProcessIdentifier(Object identifier, Class<?> sourceType, Class<?> targetType) {
		return identifier;
	}

	/**
	 * Returns additional identifier types that the processor will be able to handle.
	 *
	 * @return will never be {@literal null}.
	 */
	default Collection<Class<?>> getAdditionalIdentifierTypes() {
		return Collections.emptyList();
	}
}
