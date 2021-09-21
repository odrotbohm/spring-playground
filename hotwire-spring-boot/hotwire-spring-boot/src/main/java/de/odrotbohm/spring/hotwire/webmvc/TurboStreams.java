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

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.Value;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

import org.springframework.util.Assert;

/**
 * Representation of Turbo Streams.
 *
 * @author Oliver Drotbohm
 */
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class TurboStreams {

	private final Collection<TurboStream> streams;

	public TurboStreams() {
		this.streams = new ArrayList<>();
	}

	/**
	 * Append the rendered turbo stream template or fragment.
	 *
	 * @param target must not be {@literal null} or empty.
	 * @return
	 */
	public TurboStreamBuilder append(String target) {
		return new TurboStreamBuilder(streams, target, Action.APPEND);
	}

	/**
	 * Append the rendered turbo stream template or fragment.
	 *
	 * @param target must not be {@literal null} or empty.
	 * @return
	 */
	public TurboStreamBuilder prepend(String target) {
		return new TurboStreamBuilder(streams, target, Action.PREPEND);
	}

	/**
	 * Remove the rendered turbo stream template or fragment.
	 *
	 * @param template must not be {@literal null} or empty.
	 * @return
	 */
	public TurboStreams remove(String target) {
		return new TurboStreamBuilder(streams, target, Action.REMOVE)
				.with("¯\\_(ツ)_/¯");
	}

	/**
	 * Replace the rendered turbo stream template or fragment.
	 *
	 * @param target must not be {@literal null} or empty.
	 * @return
	 */
	public TurboStreamBuilder replace(String target) {
		return new TurboStreamBuilder(streams, target, Action.REPLACE);
	}

	/**
	 * Update the rendered turbo stream template or fragment.
	 *
	 * @param target must not be {@literal null} or empty.
	 * @return
	 */
	public TurboStreamBuilder update(String target) {
		return new TurboStreamBuilder(streams, target, Action.UPDATE);
	}

	Iterable<TurboStream> toIterable() {
		return () -> streams.iterator();
	}

	public enum Action {

		APPEND,

		PREPEND,

		REPLACE,

		UPDATE,

		REMOVE;

		String toAttribute() {
			return name().toLowerCase(Locale.ENGLISH);
		}
	}

	@Value
	@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
	public static class TurboStreamBuilder {

		private Collection<TurboStream> streams;
		private String target;
		private Action action;

		/**
		 * @param templateOrFragment the identifier of a template or fragment.
		 * @return will never be {@literal null}.
		 */
		public TurboStreams with(String templateOrFragment) {
			return and(new TurboStream(action, target, templateOrFragment));
		}

		/**
		 * Renders the fragment with the current target name within the given template.
		 *
		 * @param template must not be {@literal null} or empty.
		 * @return will never be {@literal null}.
		 */
		public TurboStreams withinTemplate(String template) {

			Assert.hasText(template, "Template name must not be null or empty!");

			return and(new TurboStream(action, target, template.concat(" :: ".concat(target))));
		}

		/**
		 * Renders the given fragment as Turbo Stream.
		 *
		 * @param fragment must not be {@literal null} or empty and a valid fragment identifier.
		 * @return will never be {@literal null}.
		 */
		public TurboStreams withFragment(String fragment) {

			Assert.hasText(fragment, "Fragment must not be null or empty!");
			Assert.isTrue(fragment.contains("::"), () -> "Invalid fragment identifier " + fragment + "!");

			return and(new TurboStream(action, target, fragment));
		}

		private TurboStreams and(TurboStream stream) {

			List<TurboStream> list = new ArrayList<>(streams);
			list.add(stream);

			return new TurboStreams(list);
		}
	}

	@Value
	static class TurboStream {

		Action action;
		String target, template;

		String openStream() {
			return String.format("<turbo-stream action=\"%s\" target=\"%s\">", action.toAttribute(), target);
		}

		String closeStream() {
			return "</turbo-stream>";
		}

		String openTemplate() {
			return "<template>";
		}

		String openTemplateFormatted() {
			return String.format("\n\t%s\n\t\t", openTemplate());
		}

		String closeTemplate() {
			return "</template>";
		}

		String closeTemplateFormatted() {
			return String.format("\n\t%s\n", closeTemplate());
		}

		boolean isRemove() {
			return Action.REMOVE.equals(action);
		}
	}
}
