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

import static org.springframework.core.ResolvableType.*;

import am.ik.yavi.core.Validator;
import de.odrotbohm.spring.web.validation.YaviValidator;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.ResolvableType;

/**
 * Auto-configuration that registers a {@link YaviValidator} instance for each {@link Validator} instance contained in
 * the application context.
 *
 * @author Oliver Drotbohm
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(Validator.class)
@ConditionalOnBean(Validator.class)
class YaviValidatorAutoConfiguration {

	@Bean
	static BeanFactoryPostProcessor yaviValidatorRegisteringPostProcessor() {

		return new BeanFactoryPostProcessor() {

			/*
			 * (non-Javadoc)
			 * @see org.springframework.beans.factory.config.BeanFactoryPostProcessor#postProcessBeanFactory(org.springframework.beans.factory.config.ConfigurableListableBeanFactory)
			 */
			@Override
			@SuppressWarnings("unchecked")
			public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {

				if (!DefaultListableBeanFactory.class.isInstance(beanFactory)) {
					return;
				}

				DefaultListableBeanFactory factory = (DefaultListableBeanFactory) beanFactory;

				for (String name : factory.getBeanNamesForType(Validator.class, false, false)) {

					ResolvableType sourceType = beanFactory.getBeanDefinition(name).getResolvableType();

					RootBeanDefinition definition = new RootBeanDefinition(YaviValidator.class,
							() -> new YaviValidator<>(factory.getBean(name, Validator.class)));
					definition.setTargetType(forClassWithGenerics(YaviValidator.class, sourceType.getGeneric(0)));
					definition.setSource(this);

					factory.registerBeanDefinition("__".concat(name), definition);
				}
			}
		};
	}
}
