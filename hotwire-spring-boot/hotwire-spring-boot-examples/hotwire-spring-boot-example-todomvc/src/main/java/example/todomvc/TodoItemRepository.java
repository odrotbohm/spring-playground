package example.todomvc;

import org.springframework.data.repository.Repository;
import org.springframework.data.util.Streamable;

interface TodoItemRepository extends Repository<TodoItem, Long> {

	TodoItem save(TodoItem item);

	Streamable<TodoItem> findAll();
}
