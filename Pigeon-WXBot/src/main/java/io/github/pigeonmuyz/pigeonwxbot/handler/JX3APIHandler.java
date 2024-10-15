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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;

@ClientEndpoint
public class JX3APIHandler {
    private final static Logger LOGGER = LoggerFactory.getLogger(JX3APIHandler.class);
    private static String SERVER_URI;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor(); // 单线程执行重连任务

    private Session session;
    private int retryCount = 0;
    private final int maxRetries = 6;

    public JX3APIHandler(String SERVER_URI){
        this.SERVER_URI = SERVER_URI;
    }

    @OnOpen
    public void onOpen(Session session) {
        this.session = session;
        retryCount = 0;  // 成功连接后重置重连计数
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
                case 2004:
                    messages.put(action, String.format(
                            "来自 %s吧 的%s\\n" +
                                    "标题：%s\\n" +
                                    "链接：%s\\n" +
                                    "日期：%s\\n"
                            , jn.get("data").get("name").asText()
                            , jn.get("data").get("class").asText()
                            , jn.get("data").get("title").asText()
                            , jn.get("data").get("url").asText()
                            , jn.get("data").get("date").asText()));
                    break;
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
        retryConnection();
    }

    @OnError
    public void onError(Session session, Throwable throwable) throws IOException {
        LOGGER.error("发生错误，正在尝试重连");
        retryConnection();
    }

    public void connectToServer() throws IOException {
        WebSocketContainer container = ContainerProvider.getWebSocketContainer();
        try {
            LOGGER.info("正在连接JX3API");
            container.connectToServer(this, new URI(SERVER_URI));
        } catch (Exception e) {
            LOGGER.error("连接失败，错误: {}", e.getMessage());
            retryConnection();
        }
    }

    private void retryConnection() throws IOException {
        if (retryCount < maxRetries) {
            executorService.submit(() -> {
                try {
                    LOGGER.info("开始尝试重连JX3API，重试次数: {}", retryCount + 1);
                    String url = "https://api.day.app/3LrVUYJfUDvE9Up8Qxt7CD/Pigeon WXBot服务警告/"+"开始尝试重连JX3API，重试次数: "+(retryCount + 1)+"?group=PigeonServer";
                    HttpTool.getData(url);
                    Thread.sleep(2000 * (long) Math.pow(2, retryCount));  // 指数递增的等待时间
                    connectToServer();
                    retryCount++;
                } catch (InterruptedException | IOException e) {
                    LOGGER.error("重连失败，错误: {}", e.getMessage());
                }
            });
        } else {
            String url = "https://api.day.app/3LrVUYJfUDvE9Up8Qxt7CD/Pigeon WXBot服务警告/WSS服务暴毙，赶紧来修?group=PigeonServer";
            LOGGER.error("超过最大重试次数，停止重连");
            HttpTool.getData(url);
        }
    }
}
