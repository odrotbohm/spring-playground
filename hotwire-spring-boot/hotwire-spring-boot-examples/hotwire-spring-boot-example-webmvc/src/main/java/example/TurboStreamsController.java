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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import de.odrotbohm.spring.hotwire.webmvc.annotation.TurboStreamPostMapping;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

/**
 * @author Oliver Drotbohm
 */
@Controller
class TurboStreamsController {

	private final List<Long> times = new ArrayList<>();

	@GetMapping(path = "/")
	String index(Model model) {

		model.addAttribute("times", times);

		return "index";
	}

	@PostMapping(path = "/", produces = MediaType.TEXT_HTML_VALUE)
	String indexStreamFull(Model model) {

		now();

		model.addAttribute("times", times);

		return "index";
	}

	@TurboStreamPostMapping(path = "/")
	TurboStreams indexStream(Model model) {

		model.addAttribute("times", Arrays.asList(now()));

		return new TurboStreams()
				.append("pings").with("index :: ping");
	}

	private long now() {

		times.add(System.currentTimeMillis());

		return times.get(times.size() - 1);
	}
}
