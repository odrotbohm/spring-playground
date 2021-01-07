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
package de.odrotbohm.spring.web.mvc;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import de.odrotbohm.spring.web.model.I18nedMessage;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.mock.http.MockHttpInputMessage;
import org.springframework.mock.http.MockHttpOutputMessage;

/**
 * @author Oliver Drotbohm
 */
@ExtendWith(MockitoExtension.class)
class MessageSourceResolvableHttpMessageConverterTests {

	@Mock MessageSourceAccessor accessor;

	@Test
	void supportsMessageSourceResolvableForTextPlain() throws Exception {

		HttpMessageConverter<MessageSourceResolvable> converter = new MessageSourceResolvableHttpMessageConverter(accessor);

		assertThat(converter.canWrite(MessageSourceResolvable.class, MediaType.TEXT_PLAIN)).isTrue();
		assertThat(converter.canRead(MessageSourceResolvable.class, MediaType.TEXT_PLAIN)).isFalse();
	}

	@Test
	void translatesMessageSourceResolvable() throws Exception {

		MessageSourceResolvable message = I18nedMessage.of("some.code");
		String expected = "Translated";

		MockHttpOutputMessage outputMessage = new MockHttpOutputMessage();
		when(accessor.getMessage(message)).thenReturn(expected);

		new MessageSourceResolvableHttpMessageConverter(accessor)
				.write(message, MediaType.TEXT_PLAIN, outputMessage);

		assertThat(outputMessage.getBodyAsString()).isEqualTo(expected);
	}

	@Test
	void doesNotSupportReadingInput() {

		assertThatExceptionOfType(UnsupportedOperationException.class)
				.isThrownBy(() -> new MessageSourceResolvableHttpMessageConverter(accessor)
						.read(MessageSourceResolvable.class, new MockHttpInputMessage("".getBytes())));
	}
}
