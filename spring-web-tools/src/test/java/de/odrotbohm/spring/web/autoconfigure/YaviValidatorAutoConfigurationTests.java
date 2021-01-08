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
package de.odrotbohm.spring.web.autoconfigure;

import static org.assertj.core.api.Assertions.*;

import am.ik.yavi.builder.ValidatorBuilder;
import am.ik.yavi.core.Validator;
import de.odrotbohm.spring.web.validation.YaviValidator;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.ResolvableType;

/**
 * Unit tests for {@link YaviValidatorAutoConfiguration}.
 *
 * @author Oliver Drotbohm
 */
class YaviValidatorAutoConfigurationTests {

	private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
			.withConfiguration(AutoConfigurations.of(YaviValidatorAutoConfiguration.class));

	@Configuration
	static class TestConfiguration {

		@Bean
		Validator<Sample> sampleValidator() {
			return ValidatorBuilder.of(Sample.class).build();
		}
	}

	@Test
	void registersYaviValidatorForUserBean() {

		contextRunner.withUserConfiguration(TestConfiguration.class)
				.run(ctx -> {

					ResolvableType type = ResolvableType.forClassWithGenerics(YaviValidator.class, Sample.class);
					assertThat(ctx.getBeanProvider(type).getObject()).isNotNull();
				});
	}

	static class Sample {}
}
