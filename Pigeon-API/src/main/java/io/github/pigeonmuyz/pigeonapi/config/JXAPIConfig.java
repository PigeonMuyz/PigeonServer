package io.github.pigeonmuyz.pigeonapi.config;

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
public class JXAPIConfig {
    private static final Logger logger = LoggerFactory.getLogger(JXAPIConfig.class);
    @Value("${user.api-config-id}")
    public String apiconfigid;
    public static String tokenv1 = "";
    public static String tokenv2 = "";
    public static String ticket = "";
    public static int scale = 2;
    public static String botname= "";
    public static String datauri= "";
    public static String imageuri= "";

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @PostConstruct
    @Scheduled(fixedDelay = 7200000) //每2小时执行一次
    @Async
    public void getJXAPIConfig(){
        try{
            logger.info("Start Init JXAPIConfig");
            Map<String, Object> result = jdbcTemplate.queryForMap("SELECT * FROM jxapiconfig WHERE id="+apiconfigid);
            tokenv1 = result.get("tokenv1").toString();
            tokenv2 = result.get("tokenv2").toString();
            ticket = result.get("ticket").toString();
            scale = (int) result.get("scale");
            botname = result.get("botname").toString();
            datauri = result.get("datauri").toString();
            imageuri = result.get("imageuri").toString();
            logger.info("TokenV1："+tokenv1);
            logger.info("TokenV2："+tokenv2);
            logger.info("Ticket："+ticket);
            logger.info("Scale："+scale);
            logger.info("BotName："+botname);
            logger.info("DataUri："+datauri);
            logger.info("ImageUri："+imageuri);
        }catch (Exception e){
            logger.error("JXAPIConfig Init Failed!!!");
            logger.error(e.getMessage());
        }
    }
}
