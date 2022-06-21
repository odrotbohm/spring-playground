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
package de.odrotbohm.spring.web.autoconfigure;

import de.odrotbohm.spring.web.mvc.MappedPayloadHandlerMethodArgumentResolver;
import de.odrotbohm.spring.web.mvc.MappedPayloadProperties;
import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.beans.factory.ObjectFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;

/**
 * @author Oliver Drotbohm
 */
@Configuration(proxyBeanMethods = false)
@RequiredArgsConstructor
@EnableConfigurationProperties(MappedPayloadProperties.class)
class MappedPayloadAutoConfiguration implements WebMvcConfigurer {

	private final ObjectFactory<RequestMappingHandlerAdapter> adapter;
	private final MappedPayloadProperties configuration;

	/*
	 * (non-Javadoc)
	 * @see org.springframework.web.servlet.config.annotation.WebMvcConfigurer#addArgumentResolvers(java.util.List)
	 */
	@Override
	public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
		resolvers.add(0, new MappedPayloadHandlerMethodArgumentResolver(() -> adapter.getObject(), configuration));
	}
}
