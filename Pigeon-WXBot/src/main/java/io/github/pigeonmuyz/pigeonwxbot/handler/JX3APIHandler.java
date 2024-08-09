package io.github.pigeonmuyz.pigeonwxbot.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.websocket.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;

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
        if (jn.get("action") != null){
            switch (jn.get("action").asInt()) {
                case 10000:
                    LOGGER.info("JX3API 连接成功");
                    break;
                default:
                    break;
            }
        }
    }

    @OnClose
    public void onClose(Session session, CloseReason reason) {
        LOGGER.error("连接丢失，正在尝试重连");
        reconnect();
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        LOGGER.error("发生错误，正在尝试重连");
        reconnect();
    }

    public void connectToServer() {
        WebSocketContainer container = ContainerProvider.getWebSocketContainer();
        try {
            LOGGER.info("正在连接JX3API");
            container.connectToServer(this, new URI(SERVER_URI));
        } catch (Exception e) {
            e.printStackTrace();
            reconnect();
        }
    }

    private void reconnect() {
        while (true) {
            try {
                LOGGER.info("开始尝试重连JX3API");
                Thread.sleep(2000); // 重连前等待5秒
                connectToServer();
                break; // 成功连接后退出循环
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
