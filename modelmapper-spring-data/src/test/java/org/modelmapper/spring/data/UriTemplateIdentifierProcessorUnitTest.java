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
package org.modelmapper.spring.data;

import static org.assertj.core.api.Assertions.*;

import java.net.URI;
import java.util.UUID;

import org.junit.jupiter.api.Test;

/**
 * @author Oliver Drotbohm
 */
class UriTemplateIdentifierProcessorUnitTest {

	private final String ID_STRING = "9ebd9beb-5af6-4c69-9b79-f04b472d1bec";
	private final UUID ID = UUID.fromString(ID_STRING);
	private final String VALID_URI_STRING = "/symptoms/" + ID_STRING;
	private final URI VALID_URI = URI.create(VALID_URI_STRING);

	@Test
	void doesNotTransformStringIdIfConversionIsLimitedToUri() {

		AggregateIdentifierProcessor processor = new UriTemplateIdentifierProcessor()
				.register(Symptom.class, "/symptoms/{id}")
				.setOnlyApplyToUri(true);

		assertThat(processor.preProcessIdentifier(VALID_URI, Symptom.class)).isEqualTo(ID_STRING);
		assertThat(processor.preProcessIdentifier("foobar", Symptom.class)).isEqualTo("foobar");

		assertThat(processor.postProcessIdentifier(ID, Symptom.class, String.class)).isEqualTo(ID);
		assertThat(processor.postProcessIdentifier(ID, Symptom.class, URI.class)).isEqualTo(VALID_URI);
		assertThat(processor.postProcessIdentifier(ID_STRING, Symptom.class, String.class)).isEqualTo(ID_STRING);
		assertThat(processor.postProcessIdentifier(ID_STRING, Symptom.class, URI.class)).isEqualTo(VALID_URI);
	}

	@Test
	void transformsStringIdIfConversionByDefault() {

		AggregateIdentifierProcessor processor = new UriTemplateIdentifierProcessor()
				.register(Symptom.class, "/symptoms/{id}");

		assertThat(processor.preProcessIdentifier(VALID_URI, Symptom.class)).isEqualTo(ID_STRING);
		assertThat(processor.preProcessIdentifier(VALID_URI_STRING, Symptom.class)).isEqualTo(ID_STRING);

		assertThat(processor.postProcessIdentifier(ID, Symptom.class, String.class)).isEqualTo(VALID_URI);
		assertThat(processor.postProcessIdentifier(ID, Symptom.class, URI.class)).isEqualTo(VALID_URI);
		assertThat(processor.postProcessIdentifier(ID_STRING, Symptom.class, String.class)).isEqualTo(VALID_URI);
		assertThat(processor.postProcessIdentifier(ID_STRING, Symptom.class, URI.class)).isEqualTo(VALID_URI);
	}
}
