package com.example.webfluxmetric;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.support.WebClientAdapter;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static org.springframework.util.StringUtils.hasText;

@SpringBootApplication
public class WebfluxMetricApplication {

	public static void main(String[] args) {
		SpringApplication.run(WebfluxMetricApplication.class, args);
	}

	@Bean
	public RouterFunction routerFunction() {
		return RouterFunctions
				.route()
				.GET("/hello", this::hello)
				.GET("/apicall", this::apiCall)
				.GET("/apicall/{id}", this::apiCallById)
				.build();
	}

	@Bean
	UserService userClient() {
		WebClient webClient = WebClient
				.builder()
				.baseUrl("https://jsonplaceholder.typicode.com")
				//Enable error handling
				//.defaultStatusHandler(HttpStatusCode::isError, resp -> resp.bodyToMono(String.class).flatMap(body -> Mono.error(new RuntimeException(body))))
				.build();

		WebClientAdapter clientAdapter = WebClientAdapter.forClient(webClient);
		HttpServiceProxyFactory factory = HttpServiceProxyFactory.builder(clientAdapter).build();
		return factory.createClient(UserService.class);
	}

	private Mono<ServerResponse> apiCallById(ServerRequest request) {
		var id = request.pathVariable("id");
		return userClient()
				.getUserById(id)
				.flatMap(ServerResponse.ok()::bodyValue);
	}

	private Mono<ServerResponse> apiCall(ServerRequest request) {
		return ServerResponse.ok().body(userClient().getUsers(), User.class);
	}

	private Mono<ServerResponse> hello(ServerRequest request) {
		var name = request.queryParam("name").orElse("UNKNOWN");
		return ServerResponse.ok().bodyValue("Hello " + name);
	}
}


record User(String id, String name, String username, String email, String phone, String website) {}
@HttpExchange(url = "/users")
interface UserService {
	@GetExchange
	Flux<User> getUsers();
	@GetExchange(url = "/{id}")
	Mono<User> getUserById(@PathVariable String id);
}