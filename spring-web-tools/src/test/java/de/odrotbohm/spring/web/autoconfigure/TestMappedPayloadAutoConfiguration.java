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

import de.odrotbohm.spring.web.mvc.MappedPayloadProperties;

import org.springframework.beans.factory.ObjectFactory;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;

/**
 * Extension for tests to be able to refer to the autoconfiguration from other packages.
 *
 * @author Oliver Drotbohm
 */
public class TestMappedPayloadAutoConfiguration extends MappedPayloadAutoConfiguration {

	TestMappedPayloadAutoConfiguration(ObjectFactory<RequestMappingHandlerAdapter> adapter,
			MappedPayloadProperties configuration) {
		super(adapter, configuration);
	}
}
