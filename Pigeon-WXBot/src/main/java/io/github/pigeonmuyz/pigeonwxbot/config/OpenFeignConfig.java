package io.github.pigeonmuyz.pigeonwxbot.config;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

@Component
@FeignClient("Pigeon-API")
public interface OpenFeignConfig {
    @GetMapping("/api/jx3api")
    ResponseEntity<Map<String, Object>> getJson(@RequestParam("keyword") String keyword,
                                                @RequestParam(value = "json", required = false) String json,
                                                @RequestParam("type") String type);
    @GetMapping("/api/activate")
    ResponseEntity<Map<String, Object>> activate(@RequestParam("masterId") String masterId,
                                                @RequestParam(value = "userId") String userId,
                                                @RequestParam(value = "server", required = false) String server,
                                                @RequestParam(value = "isGroup") Boolean isGroup,
                                                @RequestParam("type") String type);
}

