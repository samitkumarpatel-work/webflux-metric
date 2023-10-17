package com.example.webfluxmetric;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.binder.MeterBinder;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Dictionary;
import java.util.List;
import java.util.Queue;


@SpringBootApplication
@RequiredArgsConstructor
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
	public MeterBinder queueSize(Queue queue) {
		return (registry) -> Gauge.builder("queueSize", queue::size).register(registry);
	}


	private final UserClient userClient;
	private final MeterRegistry registry;
	private final List<String> list = new ArrayList<>();

	private Mono<ServerResponse> apiCallById(ServerRequest request) {
		var id = request.pathVariable("id");
		System.out.println("list size: " + list.size());
		registry.gauge("custom.apicallbyid.gauge", Tags.empty(), list.size());
		registry.counter("custom.apicallbyid.counter", Tags.empty()).increment();

//		registry.summary("custom.apicallbyid.summary", Tags.empty()).record(Integer.parseInt(id));
//		registry.timer("custom.apicallbyid.timer", Tags.empty()).record(() -> Integer.parseInt(id));

		return userClient
				.getUserById(id)
				.flatMap(ServerResponse.ok()::bodyValue);
	}

	private Mono<ServerResponse> apiCall(ServerRequest request) {
		return ServerResponse.ok().body(userClient.getUsers(), User.class);
	}

	private Mono<ServerResponse> hello(ServerRequest request) {
		var name = request.queryParam("name").orElse("UNKNOWN");
		registry.gauge("custom.hello.gauge", Tags.empty(), name.length());
		return ServerResponse.ok().bodyValue("Hello " + name);
	}
}

@Configuration
class ClientConfiguration {
	@Bean
	UserClient userClient(WebClient.Builder webClient) {
		var webClientbuilder = webClient
				.baseUrl("https://jsonplaceholder.typicode.com")
				//Enable error handling
				//.defaultStatusHandler(HttpStatusCode::isError, resp -> resp.bodyToMono(String.class).flatMap(body -> Mono.error(new RuntimeException(body))))
				.build();


		WebClientAdapter clientAdapter = WebClientAdapter.forClient(webClientbuilder);
		HttpServiceProxyFactory factory = HttpServiceProxyFactory.builder(clientAdapter).build();
		return factory.createClient(UserClient.class);
	}
}


record User(String id, String name, String username, String email, String phone, String website) {}

@HttpExchange(url = "/users")
interface UserClient {
	@GetExchange
	Flux<User> getUsers();
	@GetExchange(url = "/{id}")
	Mono<User> getUserById(@PathVariable String id);
}