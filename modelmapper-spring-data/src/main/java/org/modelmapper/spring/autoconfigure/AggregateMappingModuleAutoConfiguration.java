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

import java.util.List;

import org.modelmapper.ModelMapper;
import org.modelmapper.spring.data.AggregateIdentifierProcessor;
import org.modelmapper.spring.data.AggregateMappingConfigurer;
import org.modelmapper.spring.data.AggregateMappingModule;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.core.convert.ConversionService;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.support.Repositories;

/**
 * @author Oliver Drotbohm
 */
@ConditionalOnClass({ ModelMapper.class, Repository.class })
@AutoConfigureBefore(ModelMapperAutoConfiguration.class)
class AggregateMappingModuleAutoConfiguration {

	@Bean
	@ConditionalOnMissingBean
	AggregateMappingModule repositoryMappingModule(ApplicationContext context, ConversionService conversionService,
			List<AggregateIdentifierProcessor> processors, List<AggregateMappingConfigurer> configurers) {

		Repositories repositories = new Repositories(context);

		AggregateMappingModule module = new AggregateMappingModule(repositories, conversionService);

		processors.forEach(module::register);
		configurers.forEach(it -> it.configure(module));

		return module;
	}
}
