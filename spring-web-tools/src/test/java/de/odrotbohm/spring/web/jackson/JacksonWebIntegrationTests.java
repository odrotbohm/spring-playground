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

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import de.odrotbohm.spring.web.jackson.ErrorsSerializer.ErrorsJson;
import de.odrotbohm.spring.web.model.ErrorsWithDetails;
import de.odrotbohm.spring.web.model.I18nedMessage;

import java.util.Collections;
import java.util.HashMap;

import org.junit.jupiter.api.Test;
import org.springframework.context.MessageSource;
import org.springframework.validation.MapBindingResult;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;

/**
 * @author Oliver Drotbohm
 */
class JacksonWebIntegrationTests {

	private static final String I18NED_PATTERN_CITY = "Bitte geben Sie eine gültige Stadt an!";

	private final ObjectMapper jackson;

	JacksonWebIntegrationTests() {

		MessageSource mock = mock(MessageSource.class);
		when(mock.getMessage(any(), any())).thenReturn(I18NED_PATTERN_CITY);

		this.jackson = new ObjectMapper();
		this.jackson.registerModule(new ErrorsModule(mock));
	}

	@Test // CORE-442
	void i18nizesMessagesWhenRenderingAnError() throws Exception {

		MapBindingResult result = new MapBindingResult(new HashMap<>(), "test");
		result.rejectValue("field", "Pattern.city");

		String rendered = jackson.writeValueAsString(new ErrorsJson(result));

		assertThat(JsonPath.parse(rendered).read("$.field", String.class)).isEqualTo(I18NED_PATTERN_CITY);
	}

	@Test // CORE-442
	void rendersSimpleErrorsWithDetails() throws Exception {

		MapBindingResult result = new MapBindingResult(Collections.emptyMap(), "test");
		result.rejectValue("field", "Pattern.city");

		ErrorsWithDetails errors = ErrorsWithDetails.of(result);
		errors.addDetails("anotherField", Collections.singletonMap("key", "value"));

		DocumentContext rendered = JsonPath.parse(jackson.writeValueAsString(errors));

		assertThat(rendered.read("$.anotherField.key", String.class)).isEqualTo("value");
	}

	@Test // CORE-442
	void overridesFieldErrorWithDetails() throws Exception {

		MapBindingResult result = new MapBindingResult(new HashMap<>(), "test");
		result.rejectValue("field", "Pattern.city");

		ErrorsWithDetails errors = ErrorsWithDetails.of(result);
		errors.addDetails("field", Collections.singletonMap("key", "value"));

		String rendered = jackson.writeValueAsString(errors);

		assertThat(JsonPath.parse(rendered).read("$.field.key", String.class)).isEqualTo("value");
	}

	@Test // CORE-442
	void rendersMessageSourceResolvableWithinDetails() throws Exception {

		MapBindingResult result = new MapBindingResult(Collections.emptyMap(), "test");
		result.rejectValue("field", "Pattern.city");

		ErrorsWithDetails errors = ErrorsWithDetails.of(result)
				.addDetails("field", Collections.singletonMap("key", I18nedMessage.of("Pattern.city")));

		String rendered = jackson.writeValueAsString(errors);

		assertThat(JsonPath.parse(rendered).read("$.field.key", String.class)).isEqualTo(I18NED_PATTERN_CITY);
	}

	@JsonAutoDetect(fieldVisibility = Visibility.ANY)
	static class Sample {
		String name;
	}
}
