package io.github.pigeonmuyz.pigeonai;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@ComponentScan(basePackages = {
        "io.github.pigeonmuyz.pigeonai.controller"
})
@SpringBootApplication
public class PigeonAiApplication {

    public static void main(String[] args) {
        SpringApplication.run(PigeonAiApplication.class, args);
    }

}
