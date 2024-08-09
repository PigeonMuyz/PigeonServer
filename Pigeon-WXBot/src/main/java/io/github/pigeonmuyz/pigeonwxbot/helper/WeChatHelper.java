package io.github.pigeonmuyz.pigeonwxbot.helper;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.pigeonmuyz.pigeonwxbot.tools.HttpTool;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

public class WeChatHelper {

    private final static Logger log = LogManager.getLogger(WeChatHelper.class);

    /**
     * 上传文件
     *
     * @return 文件id
     */
    private static String getFileId(String wechatRollbackUrl, String url){
        try {
            String fileName = url.contains("?") ? url.substring(url.lastIndexOf("/") + 1, url.indexOf("?")) : url.substring(url.lastIndexOf("/") + 1);
            JsonNode jn = new ObjectMapper().readTree(HttpTool.postData(wechatRollbackUrl, String.format("{\"action\":\"upload_file\",\"params\":{\"type\":\"url\",\"name\":\"%s\",\"url\":\"%s\"}}", fileName, url)));
            log.debug("图片上传成功");
            return jn.get("data").get("file_id").asText();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 发送消息
     * @param wechatId
     * @param isGroup
     * @param messageType
     * @param message
     */
    public static void sendMessage(String wechatRollbackUrl,String wechatId, Boolean isGroup, String messageType, String message){
        log.debug(wechatId,isGroup,messageType,message);
        try {
            switch (messageType){
                case "image":
                    String fileIdTemp = getFileId(wechatRollbackUrl, message);
                    log.debug("ImageID: "+fileIdTemp);
                    if (isGroup){
                        HttpTool.postData(wechatRollbackUrl,String.format("{\"action\":\"send_message\",\"params\":{\"detail_type\":\"group\",\"group_id\":\"%s\",\"message\":[{\"type\":\"image\",\"data\":{\"file_id\":\"%s\"}}]}}",wechatId,fileIdTemp));
                    }else{
                        HttpTool.postData(wechatRollbackUrl,String.format("{\"action\":\"send_message\",\"params\":{\"detail_type\":\"private\",\"user_id\":\"%s\",\"message\":[{\"type\":\"image\",\"data\":{\"file_id\":\"%s\"}}]}}",wechatId,fileIdTemp));
                    }
                    break;
                case "text":
                    if (isGroup){
                        HttpTool.postData(wechatRollbackUrl,String.format("{\"action\":\"send_message\",\"params\":{\"detail_type\":\"group\",\"group_id\":\"%s\",\"message\":[{\"type\":\"text\",\"data\":{\"text\":\"%s\"}}]}}",wechatId,message));
                    }else{
                        HttpTool.postData(wechatRollbackUrl,String.format("{\"action\":\"send_message\",\"params\":{\"detail_type\":\"private\",\"user_id\":\"%s\",\"message\":[{\"type\":\"text\",\"data\":{\"text\":\"%s\"}}]}}",wechatId,message));
                    }
                    break;
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
