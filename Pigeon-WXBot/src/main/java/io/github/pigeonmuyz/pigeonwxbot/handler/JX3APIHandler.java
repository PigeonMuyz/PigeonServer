package io.github.pigeonmuyz.pigeonwxbot.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.pigeonmuyz.pigeonwxbot.config.DataConfig;
import io.github.pigeonmuyz.pigeonwxbot.helper.WeChatHelper;
import io.github.pigeonmuyz.pigeonwxbot.tools.HttpTool;
import jakarta.websocket.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.commons.lang3.StringEscapeUtils;

import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

@ClientEndpoint
public class JX3APIHandler {
    private final static Logger LOGGER = LoggerFactory.getLogger(JX3APIHandler.class);
    private static String SERVER_URI;
    public JX3APIHandler(String SERVER_URI){
        this.SERVER_URI = SERVER_URI;
    }
    private Session session;

    @OnOpen
    public void onOpen(Session session) {
        this.session = session;
    }

    @OnMessage
    public void onMessage(String message) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode jn = mapper.readTree(message);
        LOGGER.info(message);
        int action = jn.get("action").asInt();

        if (action != 0) {
            Map<Integer, String> messages = new HashMap<>();

            switch (action) {
                case 10000:
                    LOGGER.info("JX3API 连接成功");
                    break;
                case 2001:
                    if (jn.get("data").get("server").asText().equals("飞龙在天")) {
                        if (jn.get("data").get("status").asInt() == 0) {
                            messages.put(action, "开始维护辣！");
                        } else {
                            messages.put(action, "开服辣！快冲啊！");
                        }
                    }
                    break;
//                case 2002:
//                    messages.put(action, String.format(
//                            "【%s】\\n" +
//                                    "标题：%s\\n" +
//                                    "链接：%s\\n" +
//                                    "日期：%s"
//                            , jn.get("data").get("type").asText()
//                            , jn.get("data").get("title").asText()
//                            , jn.get("data").get("url").asText()
//                            , jn.get("data").get("date").asText()));
//                    break;
//                case 2003:
//                    messages.put(action, String.format(
//                            "有版本更新！！！\\n" +
//                                    "旧版本：%s\\n" +
//                                    "新版本：%s\\n" +
//                                    "补丁大小：%s"
//                            , jn.get("data").get("old_version").asText()
//                            , jn.get("data").get("new_version").asText()
//                            , jn.get("data").get("package_size").asText()));
//                    break;
//                case 2004:
//                    messages.put(action, String.format(
//                            "来自 %s吧 的%s\\n" +
//                                    "标题：%s\\n" +
//                                    "链接：%s\\n" +
//                                    "日期：%s\\n"
//                            , jn.get("data").get("name").asText()
//                            , jn.get("data").get("subclass").asText()
//                            , jn.get("data").get("title").asText()
//                            , jn.get("data").get("url").asText()
//                            , jn.get("data").get("date").asText()));
//                    break;
                case 2005:
                case 2006:
                    // 处理其他操作
                    break;
                default:

                    WeChatHelper.sendMessage("http://"+DataConfig.botAddress+":8000/","carol0774",false, "text", StringEscapeUtils.escapeJava(message));
                    break;
            }

            DataConfig.channelBinds.stream()
                    .filter(Objects::nonNull)
                    .forEach(channelBind -> {
                        try {
                            JsonNode userSettings = new ObjectMapper().readTree(channelBind.getSettings());
                            if (userSettings.has(String.valueOf(action)) && userSettings.get(String.valueOf(action)).asBoolean(false)) {
                                String resultMessage = messages.get(action);
                                if (resultMessage != null && !resultMessage.isEmpty()) {
                                    WeChatHelper.sendMessage("http://"+DataConfig.botAddress+":8000/",channelBind.getUserId(),channelBind.getIsGroup() == 1, "text", resultMessage);
                                    LOGGER.info("消息对象目标：{}，消息推送成功", channelBind.getUserId());
                                }
                            }
                        } catch (JsonProcessingException e) {
                            LOGGER.error("读取JSON失败");
                        }
                    });
        }
    }

    @OnClose
    public void onClose(Session session, CloseReason reason) throws IOException {
        LOGGER.error("连接丢失，正在尝试重连");
        reconnect();
    }

    @OnError
    public void onError(Session session, Throwable throwable) throws IOException {
        LOGGER.error("发生错误，正在尝试重连");
        reconnect();
    }

    public void connectToServer() throws IOException {
        WebSocketContainer container = ContainerProvider.getWebSocketContainer();
        try {
            LOGGER.info("正在连接JX3API");
            container.connectToServer(this, new URI(SERVER_URI));
        } catch (Exception e) {
            e.printStackTrace();
            reconnect();
        }
    }

    private void reconnect() throws IOException {
        int maxRetries = 6;  // 最大重试次数
        int retryCount = 0;
        long waitTime = 2000;  // 初始等待时间为2秒

        while (retryCount < maxRetries) {
            try {
                LOGGER.info("开始尝试重连JX3API，重试次数: {}", retryCount + 1);
                Thread.sleep(waitTime); // 重连前等待
                connectToServer();
                break; // 成功连接后退出循环
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (Exception e) {
                LOGGER.error("重连失败，错误: {}", e.getMessage());
                retryCount++;
                waitTime *= 2;  // 每次重试后等待时间加倍
            }
        }

        if (retryCount == maxRetries) {
            String url = "https://api.day.app/3LrVUYJfUDvE9Up8Qxt7CD/Pigeon WXBot服务警告/WSS服务暴毙，赶紧来修?group=PigeonServer";
            HttpTool.getData(url);
            LOGGER.error("超过最大重试次数，停止重连");
        }
    }

}
