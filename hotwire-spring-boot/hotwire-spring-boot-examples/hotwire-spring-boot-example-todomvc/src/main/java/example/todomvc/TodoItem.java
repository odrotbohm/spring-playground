package example.todomvc;

import java.util.UUID;

import org.springframework.data.annotation.Id;

class TodoItem {

	private @Id UUID id;
	private String title;
	private boolean completed;

	TodoItem(String title) {

		this.id = UUID.randomUUID();
		this.title = title;
		this.completed = false;
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
}
