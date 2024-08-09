package io.github.pigeonmuyz.pigeonwxbot.config;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class DataConfig {
    private static final Logger logger = LoggerFactory.getLogger(DataConfig.class);
    @Value("${user.api-config-id}")
    public String apiconfigid;
    public static String wsuri = "";

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @PostConstruct
    @Scheduled(fixedDelay = 7200000) //每2小时执行一次
    @Async
    public void getJXAPIConfig() {
        try {
            logger.info("Start Init DataConfig");
            Map<String, Object> result = jdbcTemplate.queryForMap("SELECT * FROM jxapiconfig WHERE id=" + apiconfigid);
            wsuri = result.get("wsuri").toString();
            logger.info("JX3APi WSS服务地址：" + wsuri);
        } catch (Exception e) {
            logger.error("JXAPIConfig Init Failed!!!");
            logger.error(e.getMessage());
        }
    }
}