package example.todomvc;

import java.time.Instant;
import java.util.UUID;

import org.springframework.data.annotation.Id;

public class Todo {

	private @Id UUID id;
	private String title;
	private boolean completed;
	private Instant created;

	public Todo(String title) {

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

	public Todo toggleCompletion() {

		this.completed = !completed;

		return this;
	}
}
