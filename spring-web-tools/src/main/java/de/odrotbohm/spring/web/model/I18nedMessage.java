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
package de.odrotbohm.spring.web.model;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.With;

import java.util.Arrays;
import java.util.stream.Stream;

import org.springframework.context.MessageSourceResolvable;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

/**
 * Simple implementation of {@link MessageSourceResolvable} to trigger translation of the given code(s). Useful, to e.g.
 * translate texts within {@link ErrorsWithDetails}.
 *
 * @author Oliver Drotbohm
 * @author Jens Kutzsche
 * @see ErrorsWithDetails
 * @see MessageSourceResolvableSerializer
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class I18nedMessage implements MessageSourceResolvable {

	@With
	@Nullable
	@Getter //
	private final String[] codes;

	@Nullable
	@Getter //
	private final Object[] arguments;

	/**
	 * This text is used if the codes can't resolved.
	 */
	@With
	@Nullable
	@Getter //
	private final String defaultMessage;

	/**
	 * Creates a new {@link I18nedMessage} for the given codes.
	 *
	 * @param code the primary code to resolve.
	 * @param additionalCodes additional codes to be used for lookup.
	 * @return will never be {@literal null}.
	 */
	public static I18nedMessage of(String code, String... additionalCodes) {

		Assert.hasText(code, "Code must not be null or empty!");

		String[] codes = Stream.concat(Stream.of(code), Arrays.stream(additionalCodes)).toArray(String[]::new);

		return new I18nedMessage(codes, new Object[0], null);
	}

	public I18nedMessage withArguments(Object... arguments) {
		return new I18nedMessage(codes, arguments, defaultMessage);
	}
}
