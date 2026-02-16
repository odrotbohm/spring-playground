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
package de.odrotbohm.spring.web.jackson;

import lombok.RequiredArgsConstructor;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ser.std.StdSerializer;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.validation.Errors;
import org.springframework.validation.FieldError;

import com.fasterxml.jackson.annotation.JsonAnyGetter;

/**
 * A Jackson serializer that translates an {@link Errors} instance into a {@link Map} keyed by field and the
 * {@link FieldError}s as values.
 *
 * @author Oliver Drotbohm
 */
public class ErrorsSerializer extends StdSerializer<Errors> {

	ErrorsSerializer() {
		super(Errors.class);
	}

	/*
	 * (non-Javadoc)
	 * @see tools.jackson.databind.ser.std.StdSerializer#serialize(java.lang.Object, tools.jackson.core.JsonGenerator, tools.jackson.databind.SerializationContext)
	 */
	@Override
	public void serialize(Errors value, JsonGenerator gen, SerializationContext context) throws JacksonException {

		context.findValueSerializer(ErrorsJson.class)
				.serialize(new ErrorsJson(value), gen, context);
	}

	@RequiredArgsConstructor
	static class ErrorsJson {

		private final Errors errors;

		@JsonAnyGetter
		Map<String, Object> toMap() {

			return errors.getFieldErrors().stream()
					.collect(Collectors.toMap(FieldError::getField, Function.identity(), (l, r) -> r));
		}
	}
}
