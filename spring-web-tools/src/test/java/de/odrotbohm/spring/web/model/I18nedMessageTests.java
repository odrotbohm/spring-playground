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

import org.junit.jupiter.api.Test;

/**
 * @author Oliver Drotbohm
 */
class I18nedMessageTests {

	@Test
	void exposesMainCode() {

		I18nedMessage message = I18nedMessage.of("some.code");

		assertThat(message.getCodes()).containsExactly("some.code");
		assertThat(message.getArguments()).isEmpty();
		assertThat(message.getDefaultMessage()).isNull();
	}

	@Test
	void exposesAdditionalCodes() {

		I18nedMessage message = I18nedMessage.of("some.code", "some.other.code");

		assertThat(message.getCodes()).containsExactly("some.code", "some.other.code");
		assertThat(message.getArguments()).isEmpty();
		assertThat(message.getDefaultMessage()).isNull();
	}

	@Test
	void exposesArguments() {

		I18nedMessage message = I18nedMessage.of("some.code")
				.withArguments("arg");

		assertThat(message.getArguments()).containsExactly("arg");
	}

	@Test
	void exposesDefaultMessage() {

		I18nedMessage message = I18nedMessage.of("some.code")
				.withDefaultMessage("default message");

		assertThat(message.getDefaultMessage()).isEqualTo("default message");
	}
}
