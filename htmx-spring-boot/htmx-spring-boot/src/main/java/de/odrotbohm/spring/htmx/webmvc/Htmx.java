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
package de.odrotbohm.spring.htmx.webmvc;

import java.util.Map;

import org.springframework.web.servlet.View;

/**
 * API to conveniently create and render HTMX {@link HtmxPartials}.
 *
 * @author Oliver Drotbohm
 */
public interface Htmx {

	public static final String HTMX_HEADER = "HX-Request";

	/**
	 * Creates a new {@link HtmxPartials} instance.
	 *
	 * @return will never be {@literal null}.
	 */
	HtmxPartials stream();

	/**
	 * Creates a {@link View} instance for the given {@link HtmxPartials}.
	 *
	 * @param streams must not be {@literal null}.
	 * @return will never be {@literal null}.
	 */
	View toView(HtmxPartials streams);

	/**
	 * Renders the given {@link HtmxPartials} using the given model.
	 *
	 * @param streams must not be {@literal null}.
	 * @param model must not be {@literal null}.
	 * @return will never be {@literal null}.
	 */
	String toSsePayload(HtmxPartials streams, Map<String, Object> model);
}
