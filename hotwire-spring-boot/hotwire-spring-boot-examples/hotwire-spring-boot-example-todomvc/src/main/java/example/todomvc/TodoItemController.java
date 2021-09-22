package example.todomvc;

import de.odrotbohm.spring.hotwire.webmvc.Hotwire;
import de.odrotbohm.spring.hotwire.webmvc.TurboStreams;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/")
class TodoItemController {

	private final TodoItemRepository repository;

	TodoItemController(TodoItemRepository repository) {
		this.repository = repository;
	}

	// Classic SSR

	@GetMapping
	String index(Model model, @RequestParam Optional<String> filter) {

		prepareTodos(model, filter);

		model.addAttribute("item", new TodoItemFormData(""));

		return "index";
	}

	@PostMapping
	String addNewTodoItem(@Valid @ModelAttribute("item") TodoItemFormData formData) {

		repository.save(new TodoItem(formData.title()));

		return "redirect:/";
	}

	@DeleteMapping("/{todo}")
	String deleteTodo(@PathVariable TodoItem todo) {

		repository.save(todo.complete());

		return "redirect:/";
	}

	// Hotwire optimizations

	@GetMapping(produces = Hotwire.TURBO_STREAM_VALUE)
	TurboStreams turboIndex(Model model, @RequestParam Optional<String> filter) {

		prepareTodos(model, filter);

		return new TurboStreams()
				.replace("todos").withinTemplate("index")
				.replace("foot").withinTemplate("index");
	}

	/**
	 * An optimized variant of {@link #addNewTodoItem(TodoItemFormData)}. We explicitly bind to the
	 * {@value Hotwire.TURBO_STREAM_VALUE} media type to distinguish Hotwire requests. We then perform the normal insert
	 * and then return two {@link TurboStreams} for the parts of the page that need updates by rendering the corresponding
	 * fragments of the template.
	 *
	 * @param formData
	 * @param model
	 * @return
	 */
	@PostMapping(produces = Hotwire.TURBO_STREAM_VALUE)
	TurboStreams optimizedAddTodoItem(@Valid @ModelAttribute("item") TodoItemFormData formData,
			@RequestParam Optional<String> filter, Model model) {

		repository.save(new TodoItem(formData.title()));

		prepareTodos(model, filter);

		return new TurboStreams()
				.replace("todos").withinTemplate("index")
				.replace("foot").withinTemplate("index");
	}

	@DeleteMapping(path = "/{todo}", produces = Hotwire.TURBO_STREAM_VALUE)
	TurboStreams turboDeleteTodo(@PathVariable TodoItem todo, @RequestParam Optional<String> filter, Model model) {

		repository.save(todo.complete());

		model.addAttribute("item", todo);

		prepareReferenceData(model);

		var todoId = "todo-" + todo.getId();
		var streams = new TurboStreams();

		return filter
				.map(it -> it.equals("active")
						? streams.remove(todoId)
						: streams.replace(todoId).with("fragments :: todoItem"))
				.orElse(streams)
				.replace("foot").withinTemplate("index");
	}

	// Helpers

	private void prepareTodos(Model model, Optional<String> filter) {

		model.addAttribute("todos", todos(filter));
		model.addAttribute("filter", filter.orElse(""));

		prepareReferenceData(model);
	}

	private void prepareReferenceData(Model model) {

		model.addAttribute("numberOfIncomplete", repository.findByCompleted(false).toList().size());
		model.addAttribute("numberOfTodos", repository.findAll().toList().size());
	}

	private List<TodoItemDto> todos(Optional<String> filter) {

		// Needed due to https://bugs.eclipse.org/bugs/show_bug.cgi?id=576093
		var defaulted = filter.orElse("");

		var todos = switch (defaulted) {
			case "active" -> repository.findByCompleted(false);
			case "completed" -> repository.findByCompleted(true);
			default -> repository.findAll();
		};

		return todos.map(it -> new TodoItemDto(it.getId(), it.getTitle(), it.isCompleted())).toList();
	}

	public record TodoItemFormData(@NotBlank String title) {}

	public record TodoItemDto(UUID id, String title, boolean completed) {}
}
