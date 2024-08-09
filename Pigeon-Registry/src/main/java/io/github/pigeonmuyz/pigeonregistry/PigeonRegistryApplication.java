package io.github.pigeonmuyz.pigeonregistry;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

@SpringBootApplication
@EnableEurekaServer
public class PigeonRegistryApplication {

    public static void main(String[] args) {
        SpringApplication.run(PigeonRegistryApplication.class, args);
    }

}
