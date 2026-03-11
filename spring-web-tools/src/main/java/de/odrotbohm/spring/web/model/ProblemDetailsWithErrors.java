/*
 * Copyright 2026 the original author or authors.
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
package de.odrotbohm.spring.web.model;

import lombok.EqualsAndHashCode;
import lombok.Value;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.util.Assert;
import org.springframework.validation.Errors;

/**
 * Enriches a {@link ProblemDetail} instance with an {@code errors} attribute as described
 *
 * @author Oliver Drotbohm
 */
@Value(staticConstructor = "of")
@EqualsAndHashCode(callSuper = true)
class ProblemDetailsWithErrors extends ProblemDetail {

	private static final long serialVersionUID = -3849979415528392197L;

	Errors errors;

	/**
	 * Creates a new {@link ProblemDetailsWithErrors} with the given {@link Errors} object exposed as nested errors.
	 *
	 * @param errors must not be {@literal null}.
	 */
	private ProblemDetailsWithErrors(Errors errors) {

		super(HttpStatus.BAD_REQUEST.value());

		Assert.notNull(errors, "Errors must not be null!");

		this.errors = errors;
	}
}
