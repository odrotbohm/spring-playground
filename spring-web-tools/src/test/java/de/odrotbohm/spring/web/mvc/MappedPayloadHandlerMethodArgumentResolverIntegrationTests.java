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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import am.ik.yavi.builder.ValidatorBuilder;
import am.ik.yavi.core.Validator;
import de.odrotbohm.spring.web.autoconfigure.TestMappedPayloadAutoConfiguration;
import de.odrotbohm.spring.web.jackson.ErrorsModule;
import de.odrotbohm.spring.web.model.MappedPayloads.MappedPayload;
import de.odrotbohm.spring.web.validation.YaviValidator;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpEntity;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;

/**
 * Integration tests for {@link MappedPayloadHandlerMethodArgumentResolver}.
 *
 * @author Oliver Drotbohm
 */
@WebMvcTest
@ImportAutoConfiguration(TestMappedPayloadAutoConfiguration.class)
public class MappedPayloadHandlerMethodArgumentResolverIntegrationTests {

	@Autowired MockMvc mvc;
	@Autowired RequestMappingHandlerAdapter adapter;

	@Test
	void bindsAndReturnsSerializedObject() throws Exception {

		mvc.perform(post("/")
				.accept(MediaType.APPLICATION_JSON)
				.contentType(MediaType.APPLICATION_JSON)
				.content("{ \"firstname\" : \"Dave\", \"lastname\" : \"Matthews\"}"))
				.andExpect(status().isOk());
	}

	@Test
	void rejectsMissingField() throws Exception {

		mvc.perform(post("/")
				.accept(MediaType.APPLICATION_JSON)
				.contentType(MediaType.APPLICATION_JSON)
				.content("{ \"firstname\" : \"Dave\" }"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.lastname").exists());
	}

	@SpringBootApplication
	@Import(SampleController.class)
	static class App {

		@Bean
		YaviValidator<SampleDto> sampleDtoValidator() {

			Validator<SampleDto> validator = ValidatorBuilder.of(SampleDto.class)
					.constraint(SampleDto::getLastname, "lastname", it -> it.notBlank())
					.build();

			return new YaviValidator<>(validator);
		}

		@Bean
		ErrorsModule jacksonErrorsModule(MessageSource messageSource) {
			return new ErrorsModule(messageSource);
		}
	}

	@RestController
	@RequiredArgsConstructor
	public static class SampleController {

		private final YaviValidator<SampleDto> validator;

		@PostMapping("/")
		HttpEntity<SampleDto> something(MappedPayload<SampleDto> payload) {

			return payload
					.validate(validator)
					.concludeIfValid(ResponseEntity::ok);
		}
	}

	@Data
	static class SampleDto {
		public String firstname, lastname;
	}
}
