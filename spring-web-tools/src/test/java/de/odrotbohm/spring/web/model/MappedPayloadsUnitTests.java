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
package de.odrotbohm.spring.web.model;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import de.odrotbohm.spring.web.model.MappedPayloads.MappedErrors;
import de.odrotbohm.spring.web.model.MappedPayloads.MappedPayload;
import lombok.Data;
import lombok.Value;

import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;

/**
 * Unit tests for {@link MappedPayloads}.
 *
 * @author Oliver Drotbohm
 */
@ExtendWith(MockitoExtension.class)
public class MappedPayloadsUnitTests {

	@Mock Errors errors;

	@Test
	void createsBadRequestIfErrorsPresent() {

		when(errors.hasErrors()).thenReturn(true);

		assertThat(MappedPayloads.of(errors)
				.onValidGet(() -> ResponseEntity.status(HttpStatus.I_AM_A_TEAPOT).build()).getStatusCode())
						.isEqualTo(HttpStatus.BAD_REQUEST);
	}

	@Test
	void appliesErrorHandlerOnMappedError() {

		when(errors.hasErrors()).thenReturn(true);

		ResponseEntity<?> reference = ResponseEntity.status(HttpStatus.I_AM_A_TEAPOT).build();

		assertThat(MappedPayloads.of(errors)
				.onErrors(it -> reference)
				.onValidGet(() -> ResponseEntity.ok().build()))
						.isEqualTo(reference);

		assertThat(MappedPayloads.of(errors)
				.onErrors(() -> reference)
				.onValidGet(() -> ResponseEntity.ok().build()))
						.isEqualTo(reference);
	}

	@Test
	void rejectsFieldByMethodReference() {

		createPayload(new Payload())
				.rejectField("someField", "error.code")
				.peekErrors(errors -> {
					assertThat(errors.hasFieldErrors("someField")).isTrue();
				});
	}

	@Test
	@SuppressWarnings("unchecked")
	void doesNotInvokeMapHandlerIfErrors() {

		Function<Payload, Payload> function = mock(Function.class);
		when(function.apply(any())).then(it -> it.getArgument(0));

		BiFunction<Payload, Errors, Payload> biFunction = mock(BiFunction.class);
		when(biFunction.apply(any(), any())).then(it -> it.getArgument(0));

		Function<Payload, Optional<Payload>> flatFunction = mock(Function.class);
		when(flatFunction.apply(any())).then(it -> Optional.of(it.getArgument(0)));

		MappedPayload<Payload> payload = createPayload(new Payload())
				.map(function)
				.map(biFunction)
				.flatMap(flatFunction);

		verify(function, times(1)).apply(any());
		verify(biFunction, times(1)).apply(any(), any());
		verify(flatFunction, times(1)).apply(any());

		payload = payload.rejectField("someField", "error.code")
				.map(function)
				.map(biFunction)
				.flatMap(flatFunction);

		verify(function, times(1)).apply(any());
		verify(biFunction, times(1)).apply(any(), any());
		verify(flatFunction, times(1)).apply(any());

		payload = payload
				.alwaysMap(function)
				.alwaysMap(biFunction)
				.alwaysFlatMap(flatFunction);

		verify(function, times(2)).apply(any());
		verify(biFunction, times(2)).apply(any(), any());
		verify(flatFunction, times(2)).apply(any());
	}

	@Test
	@SuppressWarnings("unchecked")
	void invokesMapIfNoErrorsPresent() {

		Function<Payload, Payload> expected = mock(Function.class);

		createPayload(new Payload())
				.map(expected)
				.concludeWithoutContent();

		verify(expected, times(1)).apply(any());
	}

	@Test
	void isAbsentOnOptionalEmpty() {
		assertAbsence(createPayload(null));
	}

	@Test
	void isAbsentOnNotFoundGuardTrue() {
		assertAbsence(createPayload(new Payload()).notFoundIf(true));
	}

	@Test
	void isAbsentOnNotFoundPredicate() {
		assertAbsence(createPayload(new Payload()).notFoundIf(it -> it != null));
	}

	@Test
	void notAbsentOnNotFoundGuardFalse() {
		assertPresence(createPayload(new Payload()).notFoundIf(false));
	}

	@Test
	void notAbsentOnFailingNotFoundPredicate() {
		assertPresence(createPayload(new Payload()).notFoundIf(it -> it == null));
	}

	@Test
	@SuppressWarnings("unchecked")
	void usesCustomErrorHandlerFunction() {

		Function<Errors, ResponseEntity<?>> expected = mock(Function.class);
		when(expected.apply(any())).thenReturn(ResponseEntity.noContent().build());

		MappedPayload<Payload> payload = createPayload(new Payload())
				.rejectField("someField", "error.code");

		payload.onErrors(expected)
				.concludeWithoutContent();

		verify(expected, times(1)).apply(any());

		payload.onErrors(() -> expected.apply(null))
				.concludeWithoutContent();

		verify(expected, times(2)).apply(any());
	}

	@Test
	@SuppressWarnings("unchecked")
	void usesCustomErrorHandlerSupplier() {

		Supplier<ResponseEntity<?>> expected = mock(Supplier.class);
		when(expected.get()).thenReturn(ResponseEntity.noContent().build());

		createPayload(new Payload())
				.rejectField("someField", "error.code")
				.onErrors(expected)
				.concludeWithoutContent();

		verify(expected).get();
	}

	@Test
	@SuppressWarnings("unchecked")
	void concludesIfNoErrors() {

		Payload source = new Payload();
		MappedPayload<?> payload = createPayload(source);

		Function<Object, ResponseEntity<?>> function = mock(Function.class);
		payload.concludeIfValid(function);
		verify(function, times(1)).apply(source);

		BiFunction<Object, Errors, ResponseEntity<?>> biFunction = mock(BiFunction.class);
		payload.concludeIfValid(biFunction);
		verify(biFunction, times(1)).apply(eq(source), any());

		BiFunction<Object, MappedErrors, ResponseEntity<?>> selfBiFunction = mock(BiFunction.class);
		payload.concludeSelfIfValid(selfBiFunction);
		verify(selfBiFunction, times(1)).apply(eq(source), any());
	}

	@Test
	void rejectsNullPayload() {

		assertThatIllegalArgumentException()
				.isThrownBy(() -> MappedPayloads.of((Object) null, errors));
		assertThatIllegalArgumentException()
				.isThrownBy(() -> MappedPayloads.of(null, errors));
		assertThatCode(() -> MappedPayloads.of(new Payload(), errors))
				.doesNotThrowAnyException();
	}

	@Test
	@SuppressWarnings("unchecked")
	void peeksOnErrors() {

		Consumer<? super Payload> consumer = mock(Consumer.class);
		BiConsumer<? super Payload, Errors> biConsumer = mock(BiConsumer.class);

		MappedPayload<Payload> payload = createPayload(new Payload())
				.peek(consumer)
				.peek(biConsumer);

		verify(consumer, times(1)).accept(any());
		verify(biConsumer, times(1)).accept(any(), any());

		payload = payload.rejectField("someField", "error.code")
				.peek(consumer)
				.peek(biConsumer);

		verify(consumer, times(1)).accept(any());
		verify(biConsumer, times(1)).accept(any(), any());

		payload.alwaysPeek(consumer)
				.alwaysPeek(biConsumer);

		verify(consumer, times(2)).accept(any());
		verify(biConsumer, times(2)).accept(any(), any());
	}

	@TestFactory
	Stream<DynamicTest> rejectsFields() {

		String field = "someField";
		String errorCode = "error.code";
		String message = "default message";

		Stream<Rejection> source = Stream.of(
				Rejection.of("…by name.", it -> it.rejectField(field, errorCode), true),
				Rejection.of("…by name with default message.",
						it -> it.rejectField(field, errorCode, message), true),
				Rejection.of("…by boolean (true) guarded name.", it -> it.rejectField(true, field, errorCode), true),
				Rejection.of("…by boolean (false) guarded name.", it -> it.rejectField(false, field, errorCode),
						false),
				Rejection.of("…by predicate (true) guarded name.", it -> it.rejectField(foo -> true, field, errorCode), true),
				Rejection.of("…by predicate (false) guarded name.", it -> it.rejectField(foo -> false, field, errorCode),
						false));

		return DynamicTest.stream(source, Rejection::getTitle, Rejection::test);
	}

	@Test
	@SuppressWarnings("unchecked")
	void createMappedPayloadFromMappedErrors() {

		Function<Payload, ResponseEntity<?>> handler = mock(Function.class);
		Payload payload = new Payload();

		MappedPayloads.of(errors).with(payload).concludeIfValid(handler);

		verify(handler).apply(payload);
	}

	@Test
	@SuppressWarnings("unchecked")
	void rejectFieldWithCustomErrorHandler() {

		Function<Errors, ResponseEntity<?>> handler = mock(Function.class);
		when(handler.apply(any())).thenReturn(ResponseEntity.noContent().build());

		Errors errors = new BeanPropertyBindingResult(new Payload(), "payload");

		MappedPayloads.of(errors)
				.rejectField(true, "someField", "error.code", handler)
				.onValidGet(() -> null);

		verify(handler).apply(errors);

		assertThat(errors.hasFieldErrors("someField")).isTrue();
	}

	@Test
	@SuppressWarnings("unchecked")
	void rejectsOnAbsence() {

		Function<Errors, ResponseEntity<?>> errorHandler = mock(Function.class);
		when(errorHandler.apply(any())).thenReturn(ResponseEntity.ok(null));

		createPayload(new Payload())
				.notFoundIf(true)
				.onErrors(errorHandler)
				.onAbsenceReject("someField", "error.code")
				.concludeWithoutContent();

		ArgumentCaptor<Errors> captor = ArgumentCaptor.forClass(Errors.class);
		verify(errorHandler, times(1)).apply(captor.capture());

		assertThat(captor.getValue().hasFieldErrors("someField")).isTrue();
	}

	@Value(staticConstructor = "of")
	static class Rejection {

		String title;
		Function<MappedPayload<Payload>, MappedPayload<Payload>> rejector;
		boolean expected;

		@SuppressWarnings("unchecked")
		void test() {

			Function<Errors, ResponseEntity<?>> handler = mock(Function.class);
			MappedPayload<Payload> payload = createPayload(new Payload()).onErrors(handler);

			if (expected) {

				when(handler.apply(any())).thenReturn(ResponseEntity.noContent().build());
				rejector.apply(payload).concludeWithoutContent();

				ArgumentCaptor<Errors> captor = ArgumentCaptor.forClass(Errors.class);
				verify(handler).apply(captor.capture());
				assertThat(captor.getValue().hasFieldErrors("someField"));

			} else {

				rejector.apply(payload).concludeWithoutContent();
				verify(handler, times(0)).apply(any());
			}
		}
	}

	private static <T> MappedPayload<T> createPayload(T payload) {
		return MappedPayloads.of(Optional.ofNullable(payload), new BeanPropertyBindingResult(payload, "payload"));
	}

	private static void assertAbsence(MappedPayload<?> payload) {
		assertPresence(payload, true);
	}

	private static void assertPresence(MappedPayload<?> payload) {
		assertPresence(payload, false);
	}

	@SuppressWarnings("unchecked")
	private static void assertPresence(MappedPayload<?> payload, boolean absence) {

		Supplier<ResponseEntity<?>> expected = mock(Supplier.class);
		when(expected.get()).thenReturn(ResponseEntity.noContent().build());

		Supplier<ResponseEntity<?>> unexpected = mock(Supplier.class);

		payload.onAbsence(absence ? expected : unexpected)
				.onValidGet(absence ? unexpected : expected);

		verify(expected, times(1)).get();
		verify(unexpected, times(0)).get();
	}

	@Data
	static class Payload {
		String someField;
	}
}
