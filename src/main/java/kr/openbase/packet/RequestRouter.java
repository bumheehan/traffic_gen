package kr.openbase.packet;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

@Configuration
public class RequestRouter {

    HeapDB db;

    public RequestRouter(HeapDB db) {
	this.db = db;
    }

    @Bean
    public RouterFunction<ServerResponse> index() {
	return RouterFunctions.route(RequestPredicates.GET("/").and(RequestPredicates.accept(MediaType.TEXT_HTML)),
		s -> ServerResponse.ok().contentType(MediaType.TEXT_HTML).render("index"));
    }

    @Bean
    public RouterFunction<ServerResponse> request(RequestHandler handler) {
	return RouterFunctions
		.route(RequestPredicates.POST("/req").and(RequestPredicates.accept(MediaType.APPLICATION_JSON)),
			request -> request.bodyToMono(SendRequest.class).flatMap(req -> {
			    db.appendReq(req);
			    return ServerResponse.ok().build();
			}).switchIfEmpty(ServerResponse.notFound().build()))
		.andRoute(RequestPredicates.POST("/req/list").and(RequestPredicates.accept(MediaType.APPLICATION_JSON)),
			request -> request.bodyToMono(String.class).flatMap(req -> {
			    db.appendReq(req);
			    return ServerResponse.ok().build();
			}).switchIfEmpty(ServerResponse.notFound().build()))

		.andRoute(
			RequestPredicates.DELETE("/req/{id}").and(RequestPredicates.accept(MediaType.APPLICATION_JSON)),
			request -> {
			    db.deleteReq(Integer.parseInt(request.pathVariable("id")));
			    return ServerResponse.ok().build();
			})
		.andRoute(RequestPredicates.GET("/req").and(RequestPredicates.accept(MediaType.APPLICATION_JSON)),
			s -> ServerResponse.ok().bodyValue(db.getReqList()))
		.andRoute(RequestPredicates.GET("/result").and(RequestPredicates.accept(MediaType.APPLICATION_JSON)),
			s -> ServerResponse.ok().bodyValue(db.getList()));
    }

}