package kr.openbase.traffic;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class TrafficApplication {

    public static void main(String[] args) {
	SpringApplication.run(TrafficApplication.class, args);
    }

}
