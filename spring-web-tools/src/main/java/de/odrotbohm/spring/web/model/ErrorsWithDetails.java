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
package de.odrotbohm.spring.web.model;

import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.Map;

import org.springframework.util.Assert;
import org.springframework.validation.Errors;

import com.fasterxml.jackson.annotation.JsonAnyGetter;

/**
 * A model object to allow replacing the error messages rendered for field errors with custom details.
 *
 * @author Oliver Drotbohm
 */
@RequiredArgsConstructor(staticName = "of")
public class ErrorsWithDetails {

	private final Errors errors;
	private final Map<String, Object> details = new HashMap<>();

	/**
	 * Registers the given details object to be used for the given key, i.e. field name.
	 *
	 * @param key must not be {@literal null} or empty.
	 * @param details can be {@literal null}.
	 * @return will never be {@literal null}.
	 */
	public ErrorsWithDetails addDetails(String key, Object details) {

		Assert.hasText(key, "Key must not be null or empty!");

		this.details.put(key, details);

		return this;
	}

	@JsonAnyGetter
	public Map<String, Object> toMap() {

		Map<String, Object> fields = new HashMap<>();

		// Add field errors first
		errors.getFieldErrors().forEach(it -> {
			fields.put(it.getField(), it);
		});

		// Add and potentially override errors with custom details
		details.forEach((key, value) -> fields.put(key, value));

		return fields;
	}
}
