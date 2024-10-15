package io.github.pigeonmuyz.pigeonwxbot.config;

import io.github.pigeonmuyz.pigeonwxbot.entity.BlackList;
import io.github.pigeonmuyz.pigeonwxbot.entity.ChannelBind;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
@EnableScheduling
public class DataConfig {
    private static final Logger logger = LoggerFactory.getLogger(DataConfig.class);
    @Value("${user.api-config-id}")
    public String apiconfigid;
    public static String wsuri = "";

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public static List<ChannelBind> channelBinds = new ArrayList<>();

    public static List<BlackList> blackList = new ArrayList<>();

    public static String botAddress = "";

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


    @PostConstruct
    @Async
    @Scheduled(fixedDelay = 60000) // 60秒更新一次
    public void updateLocalSettings(){
        String tempSQL = "SELECT * FROM channel_bind";
        channelBinds = jdbcTemplate.query(tempSQL, new BeanPropertyRowMapper<>(ChannelBind.class));
    }

    @PostConstruct
    @Async
    @Scheduled(fixedDelay = 60000) // 60秒更新一次
    public void updateBlackSettings(){
        String tempSQL = "SELECT * FROM blacklist";
        blackList = jdbcTemplate.query(tempSQL, new BeanPropertyRowMapper<>(BlackList.class));
    }
}