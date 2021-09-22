package example.todomvc;

import java.time.Instant;
import java.util.UUID;

import org.springframework.data.annotation.Id;

class TodoItem {

	private @Id UUID id;
	private String title;
	private boolean completed;
	private Instant created;

	TodoItem(String title) {

		this.id = UUID.randomUUID();
		this.title = title;
		this.completed = false;
		this.created = Instant.now();
	}

	public UUID getId() {
		return id;
	}

	public String getTitle() {
		return title;
	}

	public boolean isCompleted() {
		return completed;
	}

	public Instant getCreated() {
		return created;
	}

	public TodoItem complete() {

		this.completed = true;

		return this;
	}
}
