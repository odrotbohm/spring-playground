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

import de.odrotbohm.spring.hotwire.webmvc.TurboStreams.TurboStream;
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
import org.thymeleaf.TemplateSpec;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.spring6.view.ThymeleafView;
import org.thymeleaf.spring6.view.ThymeleafViewResolver;
import org.thymeleaf.templatemode.TemplateMode;

/**
 * API to conveniently build Hotwire streams.
 *
 * @author Oliver Drotbohm
 */
@RequiredArgsConstructor
public class WebMvcHotwire implements Hotwire {

	private final @NonNull ThymeleafViewResolver views;
	private final @NonNull SpringTemplateEngine engine;
	private final @NonNull LocaleResolver locales;

	/*
	 * (non-Javadoc)
	 * @see org.springframework.boot.hotwire.webmvc.WebMvcHotwire#stream()
	 */
	public TurboStreams stream() {
		return new TurboStreams();
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.boot.hotwire.webmvc.WebMvcHotwire#toView(org.springframework.boot.hotwire.webmvc.TurboStreams)
	 */
	public View toView(TurboStreams streams) {

		Assert.notNull(streams, "TurboStreams must not be null!");

		return (model, request, response) -> {

			Locale locale = locales.resolveLocale(request);
			PrintWriter writer = response.getWriter();

			for (TurboStream it : streams.toIterable()) {

				writer.write(it.openStream());

				if (!it.isRemove()) {

					writer.write(it.openTemplateFormatted());

					ThymeleafView delegate = (ThymeleafView) views.resolveViewName(it.getTemplate(), locale);
					delegate.setContentType(Hotwire.TURBO_STREAM_VALUE);
					delegate.render(model, request, response);

					writer.write(it.closeTemplateFormatted());
				}

				writer.write(it.closeStream());
			}
		};
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.boot.hotwire.webmvc.WebMvcHotwire#toSsePayload(org.springframework.boot.hotwire.webmvc.TurboStreams, java.util.Map)
	 */
	public String toSsePayload(TurboStreams streams, Map<String, Object> model) {

		Assert.notNull(streams, "TurboStreams must not be null!");
		Assert.notNull(model, "Model must not be null!");

		StringBuilder builder = new StringBuilder();

		for (TurboStream it : streams.toIterable()) {

			builder.append(it.openStream());

			if (!it.isRemove()) {

				Context context = new Context();
				context.setVariables(model);

				String[] parts = it.getTemplate().split("::");
				Set<String> fragments = parts.length > 1 ? Collections.singleton(parts[1].trim()) : Collections.emptySet();
				TemplateSpec spec = new TemplateSpec(parts[0].trim(), fragments, TemplateMode.HTML, null);

				builder.append(it.openTemplate());
				builder.append(engine.process(spec, context).replaceAll("\n", ""));
				builder.append(it.closeTemplate());
			}

			builder.append(it.closeStream());
		}

		return builder.toString();
	}
}
