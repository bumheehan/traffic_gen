package kr.openbase.packet;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class PacketApplication {

    public static void main(String[] args) {
	SpringApplication.run(PacketApplication.class, args);
    }

}
