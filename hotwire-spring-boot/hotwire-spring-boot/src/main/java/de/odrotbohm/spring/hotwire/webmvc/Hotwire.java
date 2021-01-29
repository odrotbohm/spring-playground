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
package de.odrotbohm.spring.hotwire.webmvc;

import java.util.Map;

import org.springframework.http.MediaType;
import org.springframework.web.servlet.View;

/**
 * API to conveniently create and render Hotwire {@link TurboStreams}.
 *
 * @author Oliver Drotbohm
 */
public interface Hotwire {

	public static final String TURBO_STREAM_VALUE = "text/vnd.turbo-stream.html";
	public static final MediaType TURBO_STREAM = MediaType.parseMediaType(TURBO_STREAM_VALUE);

	/**
	 * Creates a new {@link TurboStreams} instance.
	 *
	 * @return will never be {@literal null}.
	 */
	TurboStreams stream();

	/**
	 * Creates a {@link View} instance for the given {@link TurboStreams}.
	 *
	 * @param streams must not be {@literal null}.
	 * @return will never be {@literal null}.
	 */
	View toView(TurboStreams streams);

	/**
	 * Renders the given {@link TurboStreams} using the given model.
	 *
	 * @param streams must not be {@literal null}.
	 * @param model must not be {@literal null}.
	 * @return will never be {@literal null}.
	 */
	String toSsePayload(TurboStreams streams, Map<String, Object> model);
}
