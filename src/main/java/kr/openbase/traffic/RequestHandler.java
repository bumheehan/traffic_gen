package kr.openbase.traffic;

import java.net.URI;
import java.time.Duration;
import java.time.LocalTime;
import java.util.concurrent.TimeUnit;
import javax.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import reactor.netty.http.client.HttpClient;

@Service
public class RequestHandler {

  private static final Logger log = LoggerFactory.getLogger(RequestHandler.class);

  private HeapDB db;

  public RequestHandler(HeapDB db) {
    this.db = db;

  }

  private WebClient client;

  @PostConstruct
  public void init() throws Exception {
    SslContext sslContext =
        SslContextBuilder.forClient().trustManager(InsecureTrustManagerFactory.INSTANCE).build();
    HttpClient httpClient = HttpClient.create().option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 1000)
        .responseTimeout(Duration.ofMillis(1000))
        .doOnConnected(
            conn -> conn.addHandlerLast(new ReadTimeoutHandler(5000, TimeUnit.MILLISECONDS))
                .addHandlerLast(new WriteTimeoutHandler(5000, TimeUnit.MILLISECONDS)))
        .secure(t -> t.sslContext(sslContext));
    client =
        WebClient.builder().clientConnector(new ReactorClientHttpConnector(httpClient)).build();
  }

  @Scheduled(cron = "0/5 * * * * *") // cron에 따라 실행
  public void send() {
    long now = System.currentTimeMillis() / 1000;
    log.info("schedule tasks using cron jobs - " + now);
    for (SendRequest req : db.getReqList()) {
      send(req);
    }
  }

  private int cal(double[] coeff) {
    Integer cnt = LocalTime.now().getMinute() + LocalTime.now().getHour() * 60;
    int retVal = (int) (Math.pow(cnt, 3) * coeff[3] + Math.pow(cnt, 2) * coeff[2]
        + Math.pow(cnt, 1) * coeff[1] + coeff[0]);
    if (retVal < 0) {
      return 0;
    } else {
      return retVal;
    }
  }

  public void send(SendRequest request) {

    String host = request.getHost();
    URI uri = UriComponentsBuilder.fromHttpUrl(host).build().toUri();

    int cnt = cal(request.getCoeff());

    int randCnt = request.getErrPer() / 100 * cnt;
    if (Math.random() > 0.5) {
      cnt += randCnt;
    } else {
      cnt -= randCnt;
    }
    if (request.isRun()) {
      log.info("이전 스케쥴러 아직 마무리 안됨 다음 스케쥴러에서 진행, host : {}", host);
      return;
    }
    int duration = 60000 / cnt;
    request.setRun(true);
    log.info("요청 시작 , host : {}, 반복횟수 : {}, duration : {}", host, cnt, duration);
    Flux.range(0, cnt).delayElements(Duration.ofMillis(duration)).flatMap(s -> {
      return client.method(request.getMethod()).uri(uri).exchangeToMono(response -> {
        response.releaseBody();
        if (response.statusCode().is2xxSuccessful() || response.statusCode().is3xxRedirection()
            || response.statusCode().is4xxClientError()
            || response.statusCode().is5xxServerError()) {
          return Mono.just(response.rawStatusCode());
        } else {
          return Mono.just(100);
        }
      }).onErrorResume(ex -> {
        return Mono.just(100);
      });
    }).flatMap(s -> {
      db.updateData(host, s);
      return Mono.empty();
    }).doOnComplete(() -> request.setRun(false)).subscribe();

  }

}
