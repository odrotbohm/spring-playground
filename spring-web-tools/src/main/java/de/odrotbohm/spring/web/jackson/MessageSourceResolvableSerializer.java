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
package de.odrotbohm.spring.web.jackson;

import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ser.std.StdSerializer;

import org.springframework.context.MessageSourceResolvable;
import org.springframework.context.support.MessageSourceAccessor;

/**
 * A Jackson serializer triggering message resolution via a {@link MessageSourceAccessor} for
 * {@link MessageSourceResolvable} instances about to be serialized.
 *
 * @author Oliver Drotbohm
 */
class MessageSourceResolvableSerializer extends StdSerializer<MessageSourceResolvable> {

	private static final long serialVersionUID = 4302540100251549622L;

	private final MessageSourceAccessor accessor;

	/**
	 * Creates a new {@link MessageSourceResolvableSerializer} for the given {@link MessageSourceAccessor}.
	 *
	 * @param accessor must not be {@literal null}.
	 */
	public MessageSourceResolvableSerializer(MessageSourceAccessor accessor) {

		super(MessageSourceResolvable.class);

		this.accessor = accessor;
	}

	/*
	 * (non-Javadoc)
	 * @see tools.jackson.databind.ser.std.StdSerializer#serialize(java.lang.Object, tools.jackson.core.JsonGenerator, tools.jackson.databind.SerializationContext)
	 */
	@Override
	public void serialize(MessageSourceResolvable value, JsonGenerator gen, SerializationContext provider)
			throws JacksonException {

		gen.writeString(accessor.getMessage(value));
	}
}
