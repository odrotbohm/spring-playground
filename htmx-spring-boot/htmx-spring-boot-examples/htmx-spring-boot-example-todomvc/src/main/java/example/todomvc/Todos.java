package example.todomvc;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Sort;
import org.springframework.data.repository.Repository;
import org.springframework.data.util.Streamable;

public interface Todos extends Repository<Todo, UUID> {

	Optional<Todo> findById(UUID id);

	Todo save(Todo item);

	Todo delete(Todo item);

	Streamable<Todo> findAll(Sort sort);

	Streamable<Todo> findByCompleted(boolean completed, Sort sort);
}
