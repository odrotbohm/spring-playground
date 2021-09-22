/*
 * Copyright 2021 the original author or authors.
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
package example.todomvc;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.map.repository.config.EnableMapRepositories;

/**
 * @author Oliver Drotbohm
 */
@SpringBootApplication
@EnableMapRepositories
public class TodoMvc {

	public static void main(String[] args) throws Exception {

		var context = SpringApplication.run(TodoMvc.class, args);
		var repository = context.getBean(TodoItemRepository.class);

		repository.save(new TodoItem("Completed").complete());
		repository.save(new TodoItem("Incomplete"));
	}
}
