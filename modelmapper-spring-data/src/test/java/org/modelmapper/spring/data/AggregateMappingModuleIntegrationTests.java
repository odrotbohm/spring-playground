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

import static org.assertj.core.api.Assertions.*;

import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.net.URI;
import java.util.UUID;
import java.util.function.Consumer;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.modelmapper.MappingException;
import org.modelmapper.ModelMapper;
import org.modelmapper.spring.data.AggregateMappingModule.AggregateReferenceMappingException;
import org.modelmapper.spring.data.AggregateMappingModule.NullHandling;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.data.map.repository.config.EnableMapRepositories;
import org.springframework.data.repository.support.Repositories;
import org.springframework.test.context.TestConstructor;
import org.springframework.test.context.TestConstructor.AutowireMode;
import org.springframework.test.context.junit.jupiter.SpringExtension;

/**
 * Integration tests for {@link RepositoryMappingModule}.
 *
 * @author Oliver Drotbohm
 */
@TestConstructor(autowireMode = AutowireMode.ALL)
@ExtendWith(SpringExtension.class)
@RequiredArgsConstructor
class AggregateMappingModuleIntegrationTests {

	private final UUID id = UUID.fromString("9ebd9beb-5af6-4c69-9b79-f04b472d1bec");
	private final String VALID_URI = "/symptoms/" + id;
	private final String INVALID_URI = "/symptoms/9ebd9beb-5af6-4c69-9b79-f04b472d1beb";

	private final ApplicationContext context;
	private final ConversionService conversions;
	private final SymptomRepository symptoms;

	@Configuration
	@EnableMapRepositories
	static class TestConfig {

		@Bean
		ConversionService conversionService() {
			return new DefaultConversionService();
		}
	}

	@Test
	void extractsSymptomFromUri() {

		symptoms.save(new Symptom(id));

		Source source = new Source();
		source.symptom = VALID_URI;
		source.symptomUri = URI.create(VALID_URI);

		Sink sink = initMapper().map(source, Sink.class);

		assertThat(sink.symptom).isNotNull();
		assertThat(sink.symptomUri).isNotNull();
	}

	@Test
	void rejectsFailedLookupWithException() {

		Source source = new Source();
		source.symptom = INVALID_URI;

		assertThatExceptionOfType(MappingException.class)
				.isThrownBy(() -> initMapper().map(source, Sink.class))
				.satisfies(it -> {
					it.getErrorMessages().forEach(message -> {
						assertThat(message.getCause()).isInstanceOf(AggregateReferenceMappingException.class);
					});
				});
	}

	@Test
	void returnsNullForMissingAggregateIfExplicitlyConfigured() {

		ModelMapper mapper = initMapper(it -> {
			it.nullHandling(Symptom.class, NullHandling.RETURN_NULL);
		});

		Source source = new Source();
		source.symptom = INVALID_URI;

		Sink sink = mapper.map(source, Sink.class);

		assertThat(sink.symptom).isNull();
		assertThat(sink.symptomUri).isNull();
	}

	@Test
	void writesUriForAggregate() {

		UUID id = UUID.fromString("9ebd9beb-5af6-4c69-9b79-f04b472d1bec");
		symptoms.save(new Symptom(id));

		Symptom symptom = symptoms.save(new Symptom(id));

		Sink sink = new Sink();
		sink.symptom = symptom;
		sink.symptomUri = symptom;

		Source source = initMapper().map(sink, Source.class);

		assertThat(source.symptom).isEqualTo(VALID_URI);
		assertThat(source.symptomUri.toString()).isEqualTo(VALID_URI);
	}

	private ModelMapper initMapper() {
		return initMapper(__ -> {});
	}

	private ModelMapper initMapper(Consumer<AggregateMappingModule> configurer) {

		UriTemplateIdentifierProcessor register = new UriTemplateIdentifierProcessor()
				.register(Symptom.class, "/symptoms/{id}");
		AggregateMappingModule module = new AggregateMappingModule(new Repositories(context), conversions)
				.register(register);

		configurer.accept(module);

		ModelMapper mapper = new ModelMapper();
		mapper.registerModule(module);

		return mapper;
	}

	@Data
	static class Source {
		String symptom;
		URI symptomUri;
	}

	@Data
	static class Sink {
		Symptom symptom;
		Symptom symptomUri;
	}
}
