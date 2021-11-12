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

import de.odrotbohm.spring.htmx.webmvc.HtmxPartials.Partial;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.io.PrintWriter;
import java.util.Collections;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.springframework.util.Assert;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.ViewResolver;
import org.thymeleaf.TemplateSpec;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring5.SpringTemplateEngine;
import org.thymeleaf.templatemode.TemplateMode;

/**
 * API to conveniently build Hotwire streams.
 *
 * @author Oliver Drotbohm
 */
@RequiredArgsConstructor
public class WebMvcHtmx implements Htmx {

	private final @NonNull ViewResolver views;
	private final @NonNull SpringTemplateEngine engine;
	private final @NonNull LocaleResolver locales;

	/*
	 * (non-Javadoc)
	 * @see de.odrotbohm.spring.htmx.webmvc.Htmx#stream()
	 */
	public HtmxPartials stream() {
		return new HtmxPartials();
	}

	/*
	 * (non-Javadoc)
	 * @see de.odrotbohm.spring.htmx.webmvc.Htmx#toView(de.odrotbohm.spring.htmx.webmvc.HtmxPartials)
	 */
	public View toView(HtmxPartials streams) {

		Assert.notNull(streams, "TurboStreams must not be null!");

		return (model, request, response) -> {

			Locale locale = locales.resolveLocale(request);
			PrintWriter writer = response.getWriter();

			for (Partial it : streams.toIterable()) {

				writer.write(it.openWrapper());

				if (!it.isRemove()) {
					views.resolveViewName(it.getTemplate(), locale)
							.render(model, request, response);
				}

				writer.write(it.closeWrapper());
			}
		};
	}

	/*
	 * (non-Javadoc)
	 * @see de.odrotbohm.spring.htmx.webmvc.Htmx#toSsePayload(de.odrotbohm.spring.htmx.webmvc.HtmxPartials, java.util.Map)
	 */
	public String toSsePayload(HtmxPartials streams, Map<String, Object> model) {

		Assert.notNull(streams, "TurboStreams must not be null!");
		Assert.notNull(model, "Model must not be null!");

		StringBuilder builder = new StringBuilder();

		for (Partial it : streams.toIterable()) {

			builder.append(it.openWrapper());

			if (!it.isRemove()) {

				Context context = new Context();
				context.setVariables(model);

				String[] parts = it.getTemplate().split("::");
				Set<String> fragments = parts.length > 1 ? Collections.singleton(parts[1].trim()) : Collections.emptySet();
				TemplateSpec spec = new TemplateSpec(parts[0].trim(), fragments, TemplateMode.HTML, null);

				builder.append(engine.process(spec, context).replaceAll("\n", ""));
			}

			builder.append(it.closeWrapper());
		}

		return builder.toString();
	}
}
