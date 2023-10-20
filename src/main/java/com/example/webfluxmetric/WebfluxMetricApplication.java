package com.example.webfluxmetric;

import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import io.micrometer.observation.ObservationRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
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
import java.util.Arrays;
import java.util.List;


@SpringBootApplication
@RequiredArgsConstructor
public class WebfluxMetricApplication {
	public static void main(String[] args) {
		SpringApplication.run(WebfluxMetricApplication.class, args);
	}

	@Bean
	public RouterFunction routerFunction(UserService service) {
		return RouterFunctions
				.route()
				.GET("/hello", this::sayHello)
				.path("/user", builder -> builder
						.GET("", request -> ServerResponse.ok().body(service.getUsers(), User.class))
						.GET("/{id}", request -> ServerResponse.ok().body(service.getUserById(request.pathVariable("id")), User.class))
				)
				.build();
	}

	private Mono<ServerResponse> sayHello(ServerRequest request) {
		var name = request.queryParam("name").orElse("UNKNOWN");
		return ServerResponse.ok().bodyValue("Hello " + name);
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

@Service
@RequiredArgsConstructor
class UserService {
	private final UserClient userClient;
	private final MeterRegistry registry;
	private final ObservationRegistry observationRegistry;

	private static final List<String> db = new ArrayList<>(30);
	public Flux<User> getUsers() {
		registry.counter("custom.users.counter", Tags.empty()).increment();
		return userClient.getUsers();
	}

	public Mono<User> getUserById(String id) {

		return userClient.getUserById(id)
				.doOnNext(user -> db.removeAll(db.stream().toList()))
				.doOnNext(user -> registry.counter("custom.user.counter", Tags.empty()).increment())
				.doOnNext(user -> {
					Arrays.stream(user.name().split("")).toList().stream().filter(StringUtils::hasText).forEach(db::add);
					registry.gauge("custom.user.gauge", Tags.empty(), db, List::size);
				});
//				.name("")
//				.tag("","")
//				.tap(registry);
	}
}