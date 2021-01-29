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

import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.lang.Nullable;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * Convenience API to create Hotwire SSE streams. To be injected into Spring MVC controllers. Initialize a stream using
 * {@link #initStream()} or any of the overloads and use {@link #push(TurboStreams, Map)} methods to sent
 * {@link TurboStreams} instances to clients.
 *
 * @author Oliver Drotbohm
 */
@RequiredArgsConstructor
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class HotwireEvents {

	private static final String DEFAULT_STREAM_NAME = "¯\\_(ツ)_/¯";

	private final Map<String, SseEmitter> streams = new ConcurrentHashMap<>();
	private final Hotwire delegate;

	public SseEmitter initStream() {
		return initStream(DEFAULT_STREAM_NAME);
	}

	public SseEmitter initStream(String name) {
		return initStreamInternal(name, null);
	}

	public SseEmitter initStream(String name, Duration duration) {
		return initStreamInternal(name, duration);
	}

	public void push(TurboStreams streams, Map<String, Object> model) throws IOException {
		push(streams, model, DEFAULT_STREAM_NAME);
	}

	public void push(TurboStreams streams, Map<String, Object> model, String stream) throws IOException {
		push(stream, delegate.toSsePayload(streams, model));
	}

	private SseEmitter initStreamInternal(String name, @Nullable Duration duration) {

		SseEmitter emitter = duration == null ? new SseEmitter() : new SseEmitter(duration.toMillis());
		emitter.onCompletion(() -> streams.remove(name));
		emitter.onError(it -> streams.remove(name));
		emitter.onTimeout(() -> streams.remove(name));

		streams.put(name, emitter);

		return emitter;
	}

	private void push(String stream, Object payload) throws IOException {

		SseEmitter emitter = streams.get(stream);

		if (emitter == null) {
			return;
		}

		emitter.send(payload);
	}
}
