package io.github.pigeonmuyz.pigeonapi.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.pigeonmuyz.pigeonapi.Entity.TitleInfo;
import io.github.pigeonmuyz.pigeonapi.Entity.TitleInfoList;
import io.github.pigeonmuyz.pigeonapi.Entity.ZLibData;
import io.github.pigeonmuyz.pigeonapi.config.JXAPIConfig;
import io.github.pigeonmuyz.pigeonapi.helper.ZLibReader;
import jakarta.annotation.PostConstruct;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.Unmarshaller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api")
@EnableCaching
@EnableScheduling
public class ApiController {

    private Map<String, String> jobAliases = new HashMap<>();

    public static Map<String, String> serverAliases = new HashMap<>();

    private final RestTemplate restTemplate = new RestTemplate();

    private final Logger LOGGER = LoggerFactory.getLogger(ApiController.class);

    @Autowired
    private JdbcTemplate jdbcTemplate;
    private List<Map<String, Object>> result;
    /**
     * 初始化别名
     * @Author PigeonMuyz
     * @LastEdited 2024/7/23 14：13
     */
    @PostConstruct
    @Async
    @Scheduled(fixedDelay = 60000) //每60秒执行一次
    public void initAliases(){
        List<Map<String, Object>> rows = jdbcTemplate.queryForList("SELECT job_aliases, job_name FROM jobs_aliases");
        for (Map<String, Object> row : rows) {
            String alias = (String) row.get("job_aliases");
            String name = (String) row.get("job_name");
            jobAliases.put(alias, name);
        }
        LOGGER.debug("心法别称数量: {}", jobAliases.size());
        rows = jdbcTemplate.queryForList("SELECT server_aliases, server_name FROM server_aliases");
        for (Map<String, Object> row : rows) {
            String alias = (String) row.get("server_aliases");
            String name = (String) row.get("server_name");
            serverAliases.put(alias, name);
        }
        LOGGER.debug("服务器别称数量: {}", serverAliases.size());
    }

    @PostConstruct
    @Async
    @Scheduled(fixedDelay = 60000) //每60秒执行一次
    public void syncDatabase(){
        result = jdbcTemplate.queryForList("SELECT * FROM jxapi_usage WHERE type=0 or type=2");
        LOGGER.debug("接口数据库同步成功");
        LOGGER.debug("JSON数据类接口支持：{}", result.size());
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

    @GetMapping("/activate")
    public ResponseEntity<Map<String, Object>> activate(String masterId, String userId, @RequestParam(required = false) String server, Boolean isGroup, String type){
        Map<String, Object> response = new HashMap<>();
        response.put("time", System.currentTimeMillis());
        String tempSQL = "SELECT * FROM channel_bind WHERE userId = ?";
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(tempSQL, userId);
        int typeNum = 0;
        switch (type){
            case "微信群":
                typeNum = 0;
                break;
            case "微信用户":
                typeNum = 1;
                break;
            case "QQ群":
                typeNum = 2;
                break;
            case "QQ用户":
                typeNum = 3;
                break;
            case "官方群":
                typeNum = 4;
                break;
            default:
                typeNum = -1;
                break;
        }
        if (typeNum < 0) {
            response.put("code", "002");
            response.put("message", "该聊天不被支持使用");
            return ResponseEntity.ok(response);
        }
        // 激活判断
        if (rows.isEmpty()){
            int a = jdbcTemplate.update("INSERT INTO channel_bind(userid,type,isGroup,masterID,settings) VALUES(?,?,?,?,?)", userId, typeNum, isGroup, masterId,"{\"sender\": []}");
            response.put("code", "200");
            response.put("message", "success");
            return ResponseEntity.ok(response);
        }else {
            // 更换绑定服务器
            if (!server.equalsIgnoreCase("")){
                if (rows.get(0).get("masterID").toString().equalsIgnoreCase(masterId)){
                    String serverAlias = serverAliases.get(server);
                    if (!serverAlias.isEmpty()){
                        int a = jdbcTemplate.update("update channel_bind set server = ? where userId = ? and masterID = ?", serverAlias, userId, masterId);
                        response.put("code", "200");
                        response.put("message", "success");
                        return ResponseEntity.ok(response);
                    }else{
                        response.put("code", "200");
                        response.put("message", "找不到指定服务器");
                        return ResponseEntity.ok(response);
                    }
                }else {
                    response.put("code", "200");
                    response.put("message", "你又不是管理，你想干什么");
                    return ResponseEntity.ok(response);
                }
            }else {
                response.put("code", "200");
                response.put("message", "啊？");
                return ResponseEntity.ok(response);
            }
        }
    }

    public ResponseEntity<Map<String, Object>> allBindSettings(){
        return null;
    }

    //#region Nintendo

    @GetMapping("/titles")
    public String getNintendoTitles() {
        String url = "https://www.nintendo.co.jp/data/software/xml/switch.xml";

        // 创建 RestTemplate，设置 UTF-8 解码
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.getMessageConverters().add(0, new StringHttpMessageConverter(StandardCharsets.UTF_8));

        // 获取 XML 数据
        String xmlData = restTemplate.getForObject(url, String.class);

        try {
            // 将 XML 数据反序列化为 Java 对象
            JAXBContext jaxbContext = JAXBContext.newInstance(TitleInfoList.class);
            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            StringReader reader = new StringReader(xmlData);
            TitleInfoList titleInfoList = (TitleInfoList) unmarshaller.unmarshal(reader);

            // 获取现有数据
            Map<String, String> existingData = getExistingData();

            // 将新数据进行批量插入和更新
            batchUpsert(titleInfoList.getTitleInfo(), existingData);

            return "Data processed successfully!";
        } catch (Exception e) {
            e.printStackTrace();
            return "Error occurred: " + e.getMessage();
        }
    }

    // 查询数据库中现有的 initial_code 和 price
    private Map<String, String> getExistingData() {
        String query = "SELECT initial_code, price FROM nintendo_games_list";
        return jdbcTemplate.query(query, rs -> {
            Map<String, String> map = new HashMap<>();
            while (rs.next()) {
                map.put(rs.getString("initial_code"), rs.getString("price"));
            }
            return map;
        });
    }

    // 批量插入或更新数据库
    private void batchUpsert(List<TitleInfo> titles, Map<String, String> existingData) {
        String insertSql = "INSERT INTO nintendo_games_list " +
                "(initial_code, title_name, maker_name, price, sales_date, soft_type, platform_id, link_url, screenshot_img_url, locale_cn_name, locale_cn_maker_name, locale_cn_price) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE " +
                "title_name = VALUES(title_name), " +
                "maker_name = VALUES(maker_name), " +
                "sales_date = VALUES(sales_date), " +
                "soft_type = VALUES(soft_type), " +
                "platform_id = VALUES(platform_id), " +
                "link_url = VALUES(link_url), " +
                "screenshot_img_url = VALUES(screenshot_img_url), " +
                "locale_cn_name = VALUES(locale_cn_name), " +
                "locale_cn_maker_name = VALUES(locale_cn_maker_name), " +
                "locale_cn_price = VALUES(locale_cn_price)";

        for (TitleInfo title : titles) {
            String initialCode = title.getInitialCode();
            String newPrice = title.getPrice();

            // 检查现有数据中是否存在
            if (existingData.containsKey(initialCode)) {
                String existingPrice = existingData.get(initialCode);

                // 如果价格不同，则需要更新
                if (!existingPrice.equals(newPrice)) {
                    String updateSql = "UPDATE nintendo_games_list SET price = ?, title_name = ?, maker_name = ?, sales_date = ?, soft_type = ?, platform_id = ?, link_url = ?, screenshot_img_url = ?, locale_cn_name = ?, locale_cn_maker_name = ?, locale_cn_price = ? WHERE initial_code = ?";
                    jdbcTemplate.update(updateSql, newPrice, title.getTitleName(), title.getMakerName(), title.getSalesDate(), title.getSoftType(), title.getPlatformID(), title.getLinkURL(), title.getScreenshotImgURL(), title.getLocaleCNName(), title.getLocaleCNMakerName(), title.getLocaleCNPrice(), initialCode);
                }
            } else {
                // 不存在则插入
                jdbcTemplate.update(insertSql, initialCode, title.getTitleName(), title.getMakerName(), newPrice, title.getSalesDate(), title.getSoftType(), title.getPlatformID(), title.getLinkURL(), title.getScreenshotImgURL(), title.getLocaleCNName(), title.getLocaleCNMakerName(), title.getLocaleCNPrice());
            }
        }
    }
    //#endregion

    private final ZLibReader webPageReader = new ZLibReader();

    @GetMapping("/getbooks")
    public List<ZLibData> getBooks(@RequestParam String text) throws IOException {
        return webPageReader.getLinks(JXAPIConfig.zliburi+"/s/"+text+"?");
    }
}
