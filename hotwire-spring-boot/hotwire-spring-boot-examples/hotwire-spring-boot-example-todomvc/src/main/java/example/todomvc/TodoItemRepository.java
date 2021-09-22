package example.todomvc;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.TypedSort;
import org.springframework.data.repository.Repository;
import org.springframework.data.util.Streamable;

interface TodoItemRepository extends Repository<TodoItem, UUID> {

	Optional<TodoItem> findById(UUID id);

	TodoItem save(TodoItem item);

	default Streamable<TodoItem> findAll() {
		return findAll(TypedSort.sort(TodoItem.class).by(TodoItem::getCreated));
	}

	Streamable<TodoItem> findAll(Sort sort);

	Streamable<TodoItem> findByCompleted(boolean completed);
}
