package io.github.pigeonmuyz.pigeonwxbot;

import io.github.pigeonmuyz.pigeonwxbot.config.DataConfig;
import io.github.pigeonmuyz.pigeonwxbot.handler.JX3APIHandler;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

@EnableFeignClients
@SpringBootApplication
@ComponentScan(basePackages = {"io.github.pigeonmuyz.pigeonwxbot.handler", "io.github.pigeonmuyz.pigeonwxbot.config"})
public class PigeonWxBotApplication {

    public static void main(String[] args) {
        SpringApplication.run(PigeonWxBotApplication.class, args);
    }
    @Bean
    public CommandLineRunner run() {
        return args -> {
            JX3APIHandler client = new JX3APIHandler(DataConfig.wsuri);
            client.connectToServer();
        };
    }
}
