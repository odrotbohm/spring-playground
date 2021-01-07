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
package org.modelmapper.spring.autoconfigure;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.modelmapper.spring.data.AggregateIdentifierProcessor;
import org.modelmapper.spring.data.AggregateMappingConfigurer;
import org.modelmapper.spring.data.AggregateMappingModule;
import org.modelmapper.spring.data.Symptom;
import org.modelmapper.spring.data.TestConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * @author Oliver Drotbohm
 */
class AggregateMappingModuleAutoConfigurationTests {

	private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
			.withConfiguration(
					AutoConfigurations.of(AggregateMappingModuleAutoConfiguration.class, ModelMapperAutoConfiguration.class))
			.withBean(ConversionService.class, DefaultConversionService::new);

	@Test
	void registersModuleIfSpringDataIsOnTheClasspath() {

		contextRunner
				.withUserConfiguration(TestConfiguration.class)
				.run(ctx -> {

					assertThat(ctx).hasSingleBean(AggregateMappingModule.class)
							.hasSingleBean(ModelMapper.class);

					ModelMapper mapper = ctx.getBean(ModelMapper.class);

					assertThat(mapper.getTypeMap(String.class, Symptom.class)).isNotNull();
				});
	}

	@Test
	void doesNotRegisterModuleIfSpringDataIsNotOnTheClasspath() {

		contextRunner
				.withClassLoader(new FilteredClassLoader("org.springframework.data"))
				.run(ctx -> assertThat(ctx).doesNotHaveBean(AggregateMappingModule.class));
	}

	@Test
	void doesNotRegisterModuleIfModelMapperIsNotOnTheClasspath() {

		contextRunner
				.withClassLoader(new FilteredClassLoader("org.modelmapper"))
				.run(ctx -> assertThat(ctx).doesNotHaveBean(AggregateMappingModule.class));
	}

	@Test
	@SuppressWarnings("unchecked")
	void picksUpIdentifierProcessorBeans() {

		AggregateIdentifierProcessor processor = mock(AggregateIdentifierProcessor.class);

		contextRunner
				.withBean(AggregateIdentifierProcessor.class, () -> processor)
				.run(ctx -> {

					AggregateMappingModule module = ctx.getBean(AggregateMappingModule.class);
					List<AggregateIdentifierProcessor> processors = (List<AggregateIdentifierProcessor>) ReflectionTestUtils
							.getField(module, "processors");

					assertThat(processors).containsExactly(processor);
				});
	}

	@Test
	void invokesConfigurerBean() {

		AggregateMappingConfigurer configurer = mock(AggregateMappingConfigurer.class);

		contextRunner
				.withBean(AggregateMappingConfigurer.class, () -> configurer)
				.run(ctx -> {
					verify(configurer, times(1)).configure(any());
				});
	}
}
