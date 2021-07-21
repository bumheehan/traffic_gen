package kr.openbase.packet;

import java.net.URI;
import java.time.Duration;
import java.time.LocalTime;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;

import io.netty.channel.ChannelOption;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;
import reactor.netty.http.client.HttpClient;

@Service
public class RequestHandler {

    private static final Logger log = LoggerFactory.getLogger(RequestHandler.class);

    private HeapDB db;

    public RequestHandler(HeapDB db) {
	this.db = db;

    }

    private WebClient.Builder builder;

    @PostConstruct
    public void init() throws Exception {
	SslContext sslContext = SslContextBuilder.forClient().trustManager(InsecureTrustManagerFactory.INSTANCE)
		.build();
	HttpClient httpClient = HttpClient.create().option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 1000)
		.responseTimeout(Duration.ofMillis(1000))
		.doOnConnected(conn -> conn.addHandlerLast(new ReadTimeoutHandler(5000, TimeUnit.MILLISECONDS))
			.addHandlerLast(new WriteTimeoutHandler(5000, TimeUnit.MILLISECONDS)))
		.secure(t -> t.sslContext(sslContext));
	builder = WebClient.builder().clientConnector(new ReactorClientHttpConnector(httpClient));
    }

    @Scheduled(cron = "0 * * * * *") // cron에 따라 실행
    public void send() {
	long now = System.currentTimeMillis() / 1000;
	log.info("schedule tasks using cron jobs - " + now);
	for (SendRequest req : db.getReqList()) {
	    send(req);
	}
    }

    public void send(SendRequest request) {

	String host = request.getHost();
	URI uri = UriComponentsBuilder.fromHttpUrl(host).build().toUri();
	Integer cnt = request.getHourlyCnt().get(LocalTime.now().getHour());
	int randCnt = request.getErrPer() / 100 * cnt;
	if (Math.random() > 0.5) {
	    cnt += randCnt;
	} else {
	    cnt -= randCnt;
	}
	if (request.isRun()) {
	    log.info("이전 스케쥴러 아직 마무리 안됨 다음 스케쥴러에서 진행, host : ", host);
	}
	request.setRun(true);
	log.info("요청 시작 , host : {}, 반복횟수 : {}", host, cnt);
	Flux.range(0, cnt).parallel().runOn(Schedulers.boundedElastic()).flatMap(s -> {
	    WebClient client = builder.build();
	    return client.method(request.getMethod()).uri(uri).exchangeToMono(response -> {
		response.releaseBody();
		if (response.statusCode().is2xxSuccessful() || response.statusCode().is3xxRedirection()
			|| response.statusCode().is4xxClientError() || response.statusCode().is5xxServerError()) {
		    return Mono.just(response.rawStatusCode());
		} else {
		    return Mono.just(100);
		}
	    }).onErrorResume(ex -> {
		return Mono.just(100);
	    });
	}).map(s -> {
	    db.updateData(host, s);
	    return Mono.empty();
	}).doOnComplete(() -> request.setRun(false)).subscribe();

    }

}
