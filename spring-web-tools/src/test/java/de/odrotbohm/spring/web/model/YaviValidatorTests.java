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
package de.odrotbohm.spring.web.model;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import am.ik.yavi.builder.ValidatorBuilder;
import am.ik.yavi.core.Validator;
import de.odrotbohm.spring.web.validation.YaviValidator;
import lombok.Value;

import java.util.function.Function;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;

/**
 * Integration tests for {@link YaviValidator} integration.
 *
 * @author Oliver Drotbohm
 */
@ExtendWith(MockitoExtension.class)
class YaviValidatorTests {

	YaviValidator<Sample> validator;

	@Mock Function<Errors, ResponseEntity<?>> errorHandler;

	@BeforeEach
	void setUp() {

		Validator<Sample> source = ValidatorBuilder.of(Sample.class)
				.constraint(Sample::getFirstname, "firstname", it -> it.notBlank())
				.build();

		this.validator = new YaviValidator<>(source);
	}

	@Test
	void invokesYaviValidator() {

		Sample payload = new Sample("");
		when(errorHandler.apply(any())).thenReturn(ResponseEntity.ok(null));

		MappedPayloads.of(payload, new BeanPropertyBindingResult(payload, "payload"))
				.validate(validator)
				.onErrors(errorHandler)
				.concludeWithoutContent();

		ArgumentCaptor<Errors> captor = ArgumentCaptor.forClass(Errors.class);
		verify(errorHandler, times(1)).apply(captor.capture());

		assertThat(captor.getValue().hasFieldErrors("firstname"));
	}

	@Value
	static class Sample {
		String firstname;
	}
}
