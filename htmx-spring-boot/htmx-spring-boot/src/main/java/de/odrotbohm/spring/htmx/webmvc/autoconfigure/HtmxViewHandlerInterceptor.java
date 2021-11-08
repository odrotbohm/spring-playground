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
package de.odrotbohm.spring.htmx.webmvc.autoconfigure;

import de.odrotbohm.spring.htmx.webmvc.HtmxPartials;
import de.odrotbohm.spring.htmx.webmvc.WebMvcHtmx;
import lombok.RequiredArgsConstructor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.ModelAndView;
import org.thymeleaf.spring5.SpringTemplateEngine;
import org.thymeleaf.spring5.view.ThymeleafViewResolver;

/**
 * A {@link HandlerInterceptor} that turns {@link HtmxPartials} instances returned from controller methods into a
 *
 * @author Oliver Drotbohm
 */
@RequiredArgsConstructor
class HtmxViewHandlerInterceptor implements HandlerInterceptor {

	private final WebMvcHtmx htmx;

	public HtmxViewHandlerInterceptor(ThymeleafViewResolver views, SpringTemplateEngine engine,
			LocaleResolver locales) {

		this.htmx = new WebMvcHtmx(views, engine, locales);
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.web.servlet.HandlerInterceptor#postHandle(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, java.lang.Object, org.springframework.web.servlet.ModelAndView)
	 */
	@Override
	public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
			ModelAndView modelAndView) throws Exception {

		if (modelAndView == null || !HandlerMethod.class.isInstance(handler)) {
			return;
		}

		HandlerMethod method = (HandlerMethod) handler;

		if (!method.getReturnType().getParameterType().equals(HtmxPartials.class)) {
			return;
		}

		Object attribute = modelAndView.getModel().get("htmxPartials");

		if (!HtmxPartials.class.isInstance(attribute)) {
			return;
		}

		HtmxPartials streams = (HtmxPartials) attribute;

		modelAndView.setView(htmx.toView(streams));
	}
}
