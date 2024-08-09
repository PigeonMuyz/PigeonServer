package io.github.pigeonmuyz.pigeonapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@ComponentScan(
        basePackages = {
//				"io.github.pigeonmuyz.apiservice.service",
//				"io.github.pigeonmuyz.apiservice.tools",
                "io.github.pigeonmuyz.pigeonapi.controller",
                "io.github.pigeonmuyz.pigeonapi.config",
//				"io.github.pigeonmuyz.apiservice.websocket",
//				"io.github.pigeonmuyz.apiservice.filter"
        })
@SpringBootApplication
public class PigeonApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(PigeonApiApplication.class, args);
    }

}
