package io.github.pigeonmuyz.pigeonapi.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.pigeonmuyz.pigeonapi.config.JXAPIConfig;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api")
@EnableCaching
public class JxapiController {

    private Map<String, String> jobAliases = new HashMap<>();

    public static Map<String, String> serverAliases = new HashMap<>();

    private final RestTemplate restTemplate = new RestTemplate();

    private final Logger LOGGER = LoggerFactory.getLogger(JxapiController.class);

    @Autowired
    private JdbcTemplate jdbcTemplate;
    private List<Map<String, Object>> result;
    /**
     * 初始化别名
     * @Author PigeonMuyz
     * @LastEdited 2024/7/23 14：13
     */
    @PostConstruct
    private void initAliases(){
        List<Map<String, Object>> rows = jdbcTemplate.queryForList("SELECT job_aliases, job_name FROM jobs_aliases");
        for (Map<String, Object> row : rows) {
            String alias = (String) row.get("job_aliases");
            String name = (String) row.get("job_name");
            jobAliases.put(alias, name);
        }
        LOGGER.info("心法别称数量: {}", jobAliases.size());
        rows = jdbcTemplate.queryForList("SELECT server_aliases, server_name FROM server_aliases");
        for (Map<String, Object> row : rows) {
            String alias = (String) row.get("server_aliases");
            String name = (String) row.get("server_name");
            serverAliases.put(alias, name);
        }
        LOGGER.info("服务器别称数量: {}", serverAliases.size());
    }

    @PostConstruct
    @Async
    @Scheduled(fixedDelay = 60) //每10分钟执行一次
    public void syncDatabase(){
        result = jdbcTemplate.queryForList("SELECT * FROM jxapi_usage WHERE type=0 or type=2");
        LOGGER.info("接口数据库同步成功");
        LOGGER.info("JSON数据类接口支持：{}", result.size());
    }

    @GetMapping("/jx3api")
    public ResponseEntity<Map<String, Object>> getJson(String keyword, @RequestParam(required = false) String json, String type) throws JsonProcessingException {
        Map<String, Object> response = new HashMap<>();
        response.put("time", System.currentTimeMillis());

        Optional<Map<String, Object>> optionalResult = result.stream()
                .filter(item -> item.containsKey("name") && keyword.equals(item.get("name")))
                .findFirst();

        if (optionalResult.isPresent()) {
            Map<String, Object> matchingItem = optionalResult.get();
            response.put("code", HttpStatus.OK.value());
            response.put("message", "success");
            ObjectMapper mapper = new ObjectMapper();
            JsonNode dataNode = mapper.readTree("");
            if (json != null ){
                dataNode = mapper.readTree(json);
            }
            // 创建一个Map来存储所有的参数和它们的值
            Map<String, String> paramMap = new HashMap<>();
            paramMap.put("token", Integer.parseInt(matchingItem.get("isv2").toString()) == 0 ? JXAPIConfig.tokenv1 : JXAPIConfig.tokenv2);
            paramMap.put("ticket", JXAPIConfig.ticket);
            paramMap.put("browser", String.valueOf(JXAPIConfig.scale));
            paramMap.put("nickname", JXAPIConfig.botname);
            paramMap.put("server", dataNode.has("server") ? serverAliases.get(dataNode.get("server").asText()) : null);
            paramMap.put("name", dataNode.has("name") ? dataNode.get("name").asText() : null);
            paramMap.put("num", dataNode.has("num") ? dataNode.get("num").asText() : null);
            paramMap.put("limit", dataNode.has("limit") ? dataNode.get("limit").asText() : null);
            paramMap.put("map", dataNode.has("map") ? dataNode.get("map").asText() : null);
            paramMap.put("role", dataNode.has("role") ? dataNode.get("role").asText() : null);
            paramMap.put("mode", dataNode.has("mode") ? dataNode.get("mode").asText() : null);
            paramMap.put("table", dataNode.has("table") ? dataNode.get("table").asText() : null);
            paramMap.put("keyword", dataNode.has("keyword") ? dataNode.get("keyword").asText() : null);
            paramMap.put("school", dataNode.has("school") ? dataNode.get("school").asText() : null);
            paramMap.put("season", dataNode.has("season") ? dataNode.get("season").asText() : null);
            paramMap.put("roleid", dataNode.has("roleid") ? dataNode.get("roleid").asText() : null);
            paramMap.put("this_time", dataNode.has("this_time") ? dataNode.get("this_time").asText() : null);
            paramMap.put("that_time", dataNode.has("that_time") ? dataNode.get("that_time").asText() : null);
            paramMap.put("column", dataNode.has("column") ? dataNode.get("column").asText() : null);
            paramMap.put("uin", dataNode.has("uin") ? dataNode.get("uin").asText() : null);
            paramMap.put("subclass", dataNode.has("subclass") ? dataNode.get("subclass").asText() : null);
            paramMap.put("cache", "0");

            // 获取数据库中的scheme
            String dbScheme = type.equals("0") ? matchingItem.get("urlScheme").toString() : matchingItem.get("imageScheme").toString();

            // 构建URL
            StringBuilder schemeBuilder = new StringBuilder();
            for (String param : dbScheme.split("&")) {
                String key = param.split("=")[0].replaceAll("#", "");
                if (paramMap.containsKey(key) && paramMap.get(key) != null) {
                    schemeBuilder.append(key).append("=").append(paramMap.get(key)).append("&");
                }
            }

            // 删除最后一个"&"
            if (!schemeBuilder.isEmpty()) {
                schemeBuilder.deleteCharAt(schemeBuilder.length() - 1);
            }
            // 最终请求地址
            String url = String.format("%s%s?%s", type.equals("0") ? JXAPIConfig.datauri : JXAPIConfig.imageuri, matchingItem.get("url"), schemeBuilder.toString());
            LOGGER.info(url);
            JsonNode rootNode = mapper.readTree(restTemplate.getForObject(url, String.class));
            response.put("data",rootNode.get("data"));
            System.out.println(rootNode);
            System.out.println("Found matching item: " + matchingItem);
        } else {
            response.put("code", "001");
            response.put("message", "error");
            response.put("data", null);
        }
        return ResponseEntity.ok(response);
    }

}
