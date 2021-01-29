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
package example;

import de.odrotbohm.spring.hotwire.webmvc.Hotwire;
import de.odrotbohm.spring.hotwire.webmvc.TurboStreams;
import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * Controller to show how to work
 *
 * @author Oliver Drotbohm
 */
@Controller
@RequiredArgsConstructor
class SseController {

	private final Hotwire hotwire;
	private SseEmitter emitter;

	@GetMapping(path = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
	SseEmitter indexSse(Model model) {

		this.emitter = new SseEmitter();
		this.emitter.onCompletion(() -> this.emitter = null);

		return emitter;
	}

	@Scheduled(fixedRate = 2000)
	void pushEvent() throws IOException {

		if (emitter == null) {
			return;
		}

		Map<String, Object> model = new HashMap<>();
		model.put("time", System.currentTimeMillis());

		TurboStreams streams = new TurboStreams()
				.replace("load").with("index :: load");

		emitter.send(hotwire.toSsePayload(streams, model));
	}
}
