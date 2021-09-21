package example.todomvc;

import de.odrotbohm.spring.hotwire.webmvc.Hotwire;
import de.odrotbohm.spring.hotwire.webmvc.TurboStreams;

import java.util.List;
import java.util.UUID;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/")
class TodoItemController {

	private final TodoItemRepository repository;

	TodoItemController(TodoItemRepository repository) {
		this.repository = repository;
	}

	@GetMapping
	String index(Model model) {

		model.addAttribute("item", new TodoItemFormData(""));
		model.addAttribute("todos", todos());

		return "index";
	}

	@PostMapping
	String addNewTodoItem(@Valid @ModelAttribute("item") TodoItemFormData formData) {

		repository.save(new TodoItem(formData.title()));

		return "redirect:/";
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
	TurboStreams optimizedAddTodoItem(@Valid @ModelAttribute("item") TodoItemFormData formData, Model model) {

		repository.save(new TodoItem(formData.title()));

		model.addAttribute("todos", todos());

		// Render the todos fragment
		var streams = new TurboStreams()
				.replace("todos").withinTemplate("index");

		// If it was the first todo, also render the footer
		return todos().size() == 1
				? streams.replace("footer").withinTemplate("index")
				: streams;
	}

	private List<TodoItemDto> todos() {

		return repository.findAll()
				.map(it -> new TodoItemDto(it.getId(), it.getTitle(), it.isCompleted()))
				.toList();
	}

	public record TodoItemFormData(@NotBlank String title) {}

	public record TodoItemDto(UUID id, String title, boolean completed) {}
}
