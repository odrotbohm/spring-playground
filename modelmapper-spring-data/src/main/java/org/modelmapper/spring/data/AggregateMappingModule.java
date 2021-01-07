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

import lombok.Getter;

import java.util.*;

import org.modelmapper.Converter;
import org.modelmapper.MappingException;
import org.modelmapper.ModelMapper;
import org.modelmapper.spi.ErrorMessage;
import org.modelmapper.spi.Mapping;
import org.modelmapper.spi.MappingContext;
import org.springframework.core.convert.ConversionService;
import org.springframework.data.mapping.PersistentEntity;
import org.springframework.data.repository.support.DefaultRepositoryInvokerFactory;
import org.springframework.data.repository.support.Repositories;
import org.springframework.data.repository.support.RepositoryInvoker;
import org.springframework.data.repository.support.RepositoryInvokerFactory;
import org.springframework.lang.Nullable;
import org.springframework.plugin.core.PluginRegistry;
import org.springframework.util.Assert;

/**
 * ModelMapper module to allow the mapping of identifiers of variable kinds into aggregate references loadable via
 * Spring Data repositories. Failed aggregate resolution will be reported via an
 * {@link AggregateReferenceMappingException} unless the type has a dedicated {@link NullHandling} registered.
 *
 * @author Oliver Drotbohm
 */
public class AggregateMappingModule implements org.modelmapper.Module {

	private final RepositoryInvokerFactory invokerFactory;
	private final Repositories repositories;
	private final ConversionService conversions;

	private final List<AggregateIdentifierProcessor> processors;
	private final List<Class<?>> exclusions;
	private final Map<Class<?>, NullHandling> nullHandling;
	private NullHandling defaultNullHandling;

	/**
	 * Creates a new {@link AggregateMappingModule} from the given {@link Repositories} and {@link ConversionService}.
	 *
	 * @param repositories must not be {@literal null}.
	 * @param conversions must not be {@literal null}.
	 */
	public AggregateMappingModule(Repositories repositories, ConversionService conversions) {

		Assert.notNull(repositories, "Repositories must not be null!");
		Assert.notNull(conversions, "ConversionService must not be null!");

		this.invokerFactory = new DefaultRepositoryInvokerFactory(repositories);
		this.repositories = repositories;
		this.conversions = conversions;
		this.processors = new ArrayList<>();
		this.exclusions = new ArrayList<>();
		this.nullHandling = new HashMap<>();
		this.defaultNullHandling = NullHandling.THROW_EXCEPTION;
	}

	/**
	 * Registers an {@link IdentifierProcessor} to be used to pre- and post-process identifiers during conversions.
	 *
	 * @param processor must not be {@literal null}.
	 * @return
	 */
	public AggregateMappingModule register(AggregateIdentifierProcessor processor) {

		Assert.notNull(processor, "IdentifierProcessor must not be null!");

		this.processors.add(processor);

		return this;
	}

	/**
	 * Excludes the given domain type from the aggregate resolution mapping.
	 *
	 * @param type must not be {@literal null}.
	 * @return
	 */
	public AggregateMappingModule exclude(Class<?> type) {

		Assert.notNull(type, "Type must not be null!");

		this.exclusions.add(type);

		return this;
	}

	/**
	 * Registers a dedicated {@link NullHandling} for the given type. For all other types the default {@link NullHandling}
	 * will be used.
	 *
	 * @param type must not be {@literal null}.
	 * @param nullHandling must not be {@literal null}.
	 * @return will never be {@literal null}.
	 * @see #defaultNullHandling(NullHandling)
	 */
	public AggregateMappingModule nullHandling(Class<?> type, NullHandling nullHandling) {

		Assert.notNull(type, "Type must not be null!");
		Assert.notNull(nullHandling, "NullHandling must not be null!");

		this.nullHandling.put(type, nullHandling);

		return this;
	}

	/**
	 * Registers the given {@link NullHandling} as the one to be applied by default. Defaults to
	 * {@link NullHandling#THROW_EXCEPTION}.
	 *
	 * @param nullHandling must not be {@literal null}.
	 * @return will never be {@literal null}.
	 */
	public AggregateMappingModule defaultNullHandling(NullHandling nullHandling) {

		Assert.notNull(nullHandling, "Null handling must not be null!");

		this.defaultNullHandling = nullHandling;

		return this;
	}

	/*
	 * (non-Javadoc)
	 * @see org.modelmapper.Module#setupModule(org.modelmapper.ModelMapper)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void setupModule(ModelMapper mapper) {

		PluginRegistry<AggregateIdentifierProcessor, Class<?>> registry = PluginRegistry.of(processors);

		@SuppressWarnings("rawtypes")
		Converter converter = new Converter<Object, Object>() {

			@Override
			@Nullable
			public Object convert(MappingContext<Object, Object> context) {

				Class<?> destinationType = context.getDestinationType();
				PersistentEntity<?, ?> information = repositories.getPersistentEntity(destinationType);
				Object source = context.getSource();

				if (source == null) {
					return handleNull(null, context);
				}

				// Pre-process source identifier
				Object identifier = registry.getPluginFor(destinationType)
						.map(it -> it.preProcessIdentifier(source, destinationType))
						.orElse(source);

				// Convert into domain identifier
				Object domainId = Arrays.asList(UUID.class, String.class).contains(identifier.getClass())
						? conversions.convert(identifier, information.getRequiredIdProperty().getType())
						: source;

				// Translate into aggregate instance
				RepositoryInvoker invoker = invokerFactory.getInvokerFor(destinationType);

				Object result = Optional.ofNullable(domainId)
						.flatMap(it -> invoker.invokeFindById(it))
						.orElse(null);

				return result == null ? handleNull(domainId, context) : result;
			}

			@Nullable
			private Object handleNull(@Nullable Object id, MappingContext<?, ?> context) {

				Class<?> destinationType = context.getDestinationType();
				NullHandling handling = Optional.ofNullable(nullHandling.get(destinationType)).orElse(defaultNullHandling);

				if (handling.equals(NullHandling.THROW_EXCEPTION)) {

					Class<?> type = context.getParent().getSourceType();

					boolean isCollection = Collection.class.isAssignableFrom(type);
					MappingContext<?, ?> mappingContext = isCollection ? context.getParent() : context;
					Mapping mapping = mappingContext.getMapping();

					throw new AggregateReferenceMappingException(mapping.getPath(), id, destinationType);
				}

				return null;
			}
		};

		@SuppressWarnings("rawtypes")
		Converter toStringOrUuidConverter = new Converter<Object, Object>() {

			@Override
			@Nullable
			public Object convert(MappingContext<Object, Object> context) {

				Object source = context.getSource();
				Class<?> sourceType = context.getSourceType();
				Class<?> targetType = context.getDestinationType();

				Object id = repositories.getPersistentEntity(source.getClass())
						.getIdentifierAccessor(source)
						.getRequiredIdentifier();

				Object processed = registry.getPluginFor(sourceType)
						.map(it -> it.postProcessIdentifier(id, sourceType, targetType))
						.orElse(id);

				return targetType.isInstance(processed)
						? processed
						: conversions.convert(processed, targetType);
			}
		};

		repositories.forEach(type -> {

			if (exclusions.stream().anyMatch(type::isAssignableFrom)) {
				return;
			}

			PersistentEntity<?, ?> information = repositories.getPersistentEntity(type);
			Class<?> domainIdType = information.getRequiredIdProperty().getType();
			Set<Class<?>> idTypes = new HashSet<Class<?>>();

			idTypes.addAll(Arrays.asList(UUID.class, String.class));
			idTypes.add(domainIdType);

			registry.getPluginFor(type)
					.map(AggregateIdentifierProcessor::getAdditionalIdentifierTypes)
					.ifPresent(idTypes::addAll);

			idTypes.forEach(it -> {

				mapper.addConverter(converter, it, information.getType());
				mapper.addConverter(toStringOrUuidConverter, information.getType(), it);
			});
		});
	}

	/**
	 * A {@link MappingException} being thrown in case no aggregate can be resolved for a given source value.
	 *
	 * @author Oliver Drotbohm
	 */
	@Getter
	public static class AggregateReferenceMappingException extends MappingException {

		private static final long serialVersionUID = 2554385939537893357L;

		private final String path;
		private final Object source;
		private final Class<?> targetType;
		private final String message;

		public AggregateReferenceMappingException(String path, @Nullable Object source, Class<?> targetType) {

			super(Arrays.asList(new ErrorMessage(message(targetType, source))));

			this.path = cleanUp(path);
			this.source = source;
			this.targetType = targetType;
			this.message = message(targetType, source);
		}

		private static String message(Class<?> targetType, @Nullable Object source) {
			return String.format("Invalid %s reference %s!", targetType.getName(), source);
		}

		private static String cleanUp(String path) {
			return !path.contains(".") ? path : path.substring(0, path.indexOf('.'));
		}
	}

	public enum NullHandling {

		/**
		 * Failed aggregate lookups result in {@literal null} being used instead.
		 */
		RETURN_NULL,

		/**
		 * Failed aggregate lookups cause an {@link AggregateReferenceMappingException} be registered.
		 */
		THROW_EXCEPTION;
	}
}
