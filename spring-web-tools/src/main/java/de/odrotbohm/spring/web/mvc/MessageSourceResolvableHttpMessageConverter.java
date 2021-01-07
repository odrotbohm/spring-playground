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
package de.odrotbohm.spring.web.mvc;

import java.io.IOException;

import org.springframework.context.MessageSourceResolvable;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.AbstractHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.util.Assert;

/**
 * An {@link HttpMessageConverter} that immediately renders {@link MessageSourceResolvable} instances returned from a
 * Spring MVC controller as plain text if {@link MediaType#TEXT_PLAIN} is requested.
 *
 * @author Oliver Drotbohm
 */
public class MessageSourceResolvableHttpMessageConverter
		extends AbstractHttpMessageConverter<MessageSourceResolvable> {

	private final MessageSourceAccessor messages;

	/**
	 * Creates a new {@link MessageSourceResolvableHttpMessageConverter} for the given {@link MessageSourceAccessor}.
	 *
	 * @param messages must not be {@literal null}.
	 */
	MessageSourceResolvableHttpMessageConverter(MessageSourceAccessor messages) {

		super(MediaType.TEXT_PLAIN);

		Assert.notNull(messages, "Messages must not be null!");

		this.messages = messages;
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.http.converter.AbstractHttpMessageConverter#supports(java.lang.Class)
	 */
	@Override
	protected boolean supports(Class<?> clazz) {
		return MessageSourceResolvable.class.isAssignableFrom(clazz);
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.http.converter.AbstractHttpMessageConverter#writeInternal(java.lang.Object, org.springframework.http.HttpOutputMessage)
	 */
	@Override
	protected void writeInternal(MessageSourceResolvable t, HttpOutputMessage outputMessage)
			throws IOException, HttpMessageNotWritableException {
		outputMessage.getBody().write(messages.getMessage(t).getBytes());
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.http.converter.AbstractHttpMessageConverter#canRead(org.springframework.http.MediaType)
	 */
	@Override
	protected boolean canRead(MediaType mediaType) {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.http.converter.AbstractHttpMessageConverter#readInternal(java.lang.Class, org.springframework.http.HttpInputMessage)
	 */
	@Override
	protected MessageSourceResolvable readInternal(Class<? extends MessageSourceResolvable> clazz,
			HttpInputMessage inputMessage)
			throws IOException, HttpMessageNotReadableException {
		throw new UnsupportedOperationException();
	}
}
