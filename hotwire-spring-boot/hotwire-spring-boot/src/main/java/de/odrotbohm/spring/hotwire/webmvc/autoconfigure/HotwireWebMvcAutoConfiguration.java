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
package de.odrotbohm.spring.hotwire.webmvc.autoconfigure;

import de.odrotbohm.spring.hotwire.webmvc.Hotwire;
import de.odrotbohm.spring.hotwire.webmvc.WebMvcHotwire;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.beans.factory.ObjectFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.thymeleaf.spring5.SpringTemplateEngine;
import org.thymeleaf.spring5.view.ThymeleafViewResolver;

/**
 * @author Oliver Drotbohm
 */
@RequiredArgsConstructor
@Configuration(proxyBeanMethods = false)
class HotwireWebMvcAutoConfiguration implements WebMvcConfigurer {

	private final @NonNull ThymeleafViewResolver resolver;
	private final @NonNull SpringTemplateEngine engine;
	private final @NonNull ObjectFactory<LocaleResolver> locales;

	@Bean
	Hotwire hotwire() {
		return new WebMvcHotwire(resolver, engine, locales.getObject());
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.web.servlet.config.annotation.WebMvcConfigurer#addArgumentResolvers(java.util.List)
	 */
	@Override
	public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
		resolvers.add(new HotwireArgumentResolver(resolver, engine, locales.getObject()));
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.web.servlet.config.annotation.WebMvcConfigurer#addInterceptors(org.springframework.web.servlet.config.annotation.InterceptorRegistry)
	 */
	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		registry.addInterceptor(new HotwireViewHandlerInterceptor(resolver, engine, locales.getObject()));
	}
}
