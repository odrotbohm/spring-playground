/*
 * Copyright 2021 the original author or authors.
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
package de.odrotbohm.spring.web.validation;

import am.ik.yavi.core.Validator;
import de.odrotbohm.spring.web.model.MappedPayloads.MappedPayload;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Delegate;

import java.util.function.BiConsumer;

import org.springframework.validation.Errors;

/**
 * A delegate for a YAVI {@link Validator} that can also be handed into {@link MappedPayload#validate(BiConsumer)} right
 * away to trigger validation
 *
 * @author Oliver Drotbohm
 */
@RequiredArgsConstructor
public class YaviValidator<T> implements BiConsumer<T, Errors> {

	private final @Delegate Validator<T> delegate;

	/*
	 * (non-Javadoc)
	 * @see java.util.function.BiConsumer#accept(java.lang.Object, java.lang.Object)
	 */
	@Override
	public void accept(T t, Errors u) {
		validate(t).apply(u::rejectValue);
	}
}
