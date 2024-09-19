package io.github.pigeonmuyz.pigeonwxbot.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.pigeonmuyz.pigeonwxbot.helper.WeChatHelper;
import io.github.pigeonmuyz.pigeonwxbot.tools.HttpTool;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;
import io.github.pigeonmuyz.pigeonwxbot.config.OpenFeignConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import org.apache.commons.lang3.StringEscapeUtils;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class WeChatHandler extends TextWebSocketHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(WeChatHandler.class);

    Map<String, Object> connectedUser = new HashMap<>();

    private OpenFeignConfig ofc;

    @Autowired
    public void setOpenFeignConfig(@Qualifier("io.github.pigeonmuyz.pigeonwxbot.config.OpenFeignConfig") OpenFeignConfig ofc) {
        this.ofc = ofc;
    }

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        // 传入的消息
        String payload = message.getPayload();
        // 初始化ObjectMapper对象
        ObjectMapper mapper = new ObjectMapper();
        // 定义JsonNode对象
        JsonNode rootNode;
        // 定义键值对存储
        Map<String,Object> requestBody;
        // WeChat类型
        Boolean isGroup = true;
        // WeChat ID
        String userId = "";
        // WeChat Master ID；
        String masterId = "";
        // 用户真实地址
//        LOGGER.info(String.valueOf(session.getRemoteAddress().getHostString()));
//        LOGGER.info("收到消息："+payload);
        // 判断是否为JSON
        if (isValidJson(payload)){
            // 初始化rootNode对象
            rootNode = mapper.readTree(payload);
            //#region 识别用户
            if (rootNode.get("detail_type").asText().equals("group")){
                isGroup = true;
                userId = rootNode.get("group_id").asText();
                masterId = rootNode.get("user_id").asText();
            }else if (rootNode.get("detail_type").asText().equals("private")){
                isGroup = false;
                userId = rootNode.get("user_id").asText();
                masterId = rootNode.get("user_id").asText();
            }
            //#endregion
            //#region 识别消息类别
            // 临时键值对
            Map<String,Object> tempMap;
            // 临时消息
            String temp;
            try{
                switch (rootNode.get("type").asText()){
                    case "message":
                        //#region 处理消息指令
                        String[] commands;
                        if (payload.contains("的")) {
                            commands = rootNode.get("alt_message").asText().split("的");
                            if (commands.length > 1){
                                String tempCommand = commands[1];
                                commands[1] = commands[0];
                                commands[0] = tempCommand;
                            }
                        } else {
                            commands = rootNode.get("alt_message").asText().split(" ");
                        }
                        switch (commands[0]){
                            //#region 日常
                            case "日常":
                                requestBody = ofc.getJson("日常","","0").getBody();
                                if (Integer.parseInt(requestBody.get("code").toString()) == 200 && requestBody.get("data") != null){
                                    tempMap = (Map<String, Object>) requestBody.get("data");
                                    temp =  "【PVE日常】\\n"
                                            + "秘境日常：" + tempMap.get("war").toString() + "\\n"
                                            + "公共日常：" + ((List<String>)tempMap.get("team")).get(0) + "\\n"
                                            + "【PVP日常】\\n"
                                            + "矿车：跨服•烂柯山\\n"
                                            + "战场：" + tempMap.get("battle").toString() + "\\n"
                                            + "【PVX日常】\\n"
                                            + (tempMap.get("draw") != null  ? "美人图：" + tempMap.get("draw").toString()+"\\n" : "美人图：无\\n")
                                            + "门派事件：" + tempMap.get("school").toString() + "\\n"
                                            + String.format("福源宠物：%s;%s;%s\\n", ((List<String>)tempMap.get("luck")).get(0), ((List<String>)tempMap.get("luck")).get(1), ((List<String>)tempMap.get("luck")).size() == 3 ? ((List<String>)tempMap.get("luck")).get(2):"")
                                            + String.format("家园声望：%s;%s;%s\\n", ((List<String>)tempMap.get("card")).get(0), ((List<String>)tempMap.get("card")).get(1), ((List<String>)tempMap.get("card")).size() == 3 ? ((List<String>)tempMap.get("card")).get(2):"" )
                                            + "【PVE周常】\\n"
                                            + "五人秘境：" + ((List<String>)tempMap.get("team")).get(1) + "\\n"
                                            + "十人秘境：" + ((List<String>)tempMap.get("team")).get(2) + "\\n"
                                            + "【今天是" + tempMap.get("date").toString() + " 星期" + tempMap.get("week").toString() +"】";
                                    WeChatHelper.sendMessage("http://"+session.getRemoteAddress().getHostString()+":8000/",userId,isGroup, "text", temp);
                                }else{
                                    WeChatHelper.sendMessage("http://"+session.getRemoteAddress().getHostString()+":8000/",userId,isGroup, "text", "碰到错误了，请反馈给渡渡鸟吧");
                                }
                                break;
                            //#endregion
                            //#region 百战
                            case "百战":
                                requestBody = ofc.getJson("百战","","1").getBody();
                                if (Integer.parseInt(requestBody.get("code").toString()) == 200 && requestBody.get("data") != null){
                                    tempMap = (Map<String, Object>) requestBody.get("data");
                                    WeChatHelper.sendMessage("http://"+session.getRemoteAddress().getHostString()+":8000/",userId,isGroup, "image", tempMap.get("url").toString());
                                }else{
                                    WeChatHelper.sendMessage("http://"+session.getRemoteAddress().getHostString()+":8000/",userId,isGroup, "text", "碰到错误了，请反馈给渡渡鸟吧");
                                }
                                break;
                            //#endregion
                            //#region 公告
                            case "公告":
                                requestBody = ofc.getJson("维护公告","","1").getBody();
                                if (Integer.parseInt(requestBody.get("code").toString()) == 200 && requestBody.get("data") != null){
                                    tempMap = (Map<String, Object>) requestBody.get("data");
                                    WeChatHelper.sendMessage("http://"+session.getRemoteAddress().getHostString()+":8000/",userId,isGroup, "image", tempMap.get("url").toString());
                                }else{
                                    WeChatHelper.sendMessage("http://"+session.getRemoteAddress().getHostString()+":8000/",userId,isGroup, "text", "碰到错误了，请反馈给渡渡鸟吧");
                                }
                                break;
                            //#endregion
                            //#region 装备
                            case "装备":
                                if (commands.length <= 1) {
                                    return;
                                }
                                requestBody = ofc.getJson("装备",String.format("{\"server\": \"%s\", \"name\": \"%s\"}", "飞龙在天", commands[1]),"1").getBody();
                                LOGGER.info(requestBody.get("code").toString());
                                if (Integer.parseInt(requestBody.get("code").toString()) == 200 && requestBody.get("data") != null){
                                    tempMap = (Map<String, Object>) requestBody.get("data");
                                    WeChatHelper.sendMessage("http://"+session.getRemoteAddress().getHostString()+":8000/",userId,isGroup, "image", tempMap.get("url").toString());
                                }else{
                                    WeChatHelper.sendMessage("http://"+session.getRemoteAddress().getHostString()+":8000/",userId,isGroup, "text", "碰到错误了，请反馈给渡渡鸟吧");
                                }
                                break;
                            //#endregion
                            //#region 烟花
                            case "烟花":
                                if (commands.length <= 1) {
                                    return;
                                }
                                requestBody = ofc.getJson("烟花",String.format("{\"server\": \"%s\", \"name\": \"%s\"}", "飞龙在天", commands[1]),"1").getBody();
                                if (Integer.parseInt(requestBody.get("code").toString()) == 200 && requestBody.get("data") != null){
                                    tempMap = (Map<String, Object>) requestBody.get("data");
                                    WeChatHelper.sendMessage("http://"+session.getRemoteAddress().getHostString()+":8000/",userId,isGroup, "image", tempMap.get("url").toString());
                                }else{
                                    WeChatHelper.sendMessage("http://"+session.getRemoteAddress().getHostString()+":8000/",userId,isGroup, "text", "碰到错误了，请反馈给渡渡鸟吧");
                                }
                                break;
                            //#endregion
                            //#region 奇遇
                            case "奇遇":
                                if (commands.length <= 1) {
                                    return;
                                }
                                requestBody = ofc.getJson("奇遇",String.format("{\"server\": \"%s\", \"name\": \"%s\"}", "飞龙在天", commands[1]),"1").getBody();
                                if (Integer.parseInt(requestBody.get("code").toString()) == 200 && requestBody.get("data") != null){
                                    tempMap = (Map<String, Object>) requestBody.get("data");
                                    WeChatHelper.sendMessage("http://"+session.getRemoteAddress().getHostString()+":8000/",userId,isGroup, "image", tempMap.get("url").toString());
                                }else{
                                    WeChatHelper.sendMessage("http://"+session.getRemoteAddress().getHostString()+":8000/",userId,isGroup, "text", "碰到错误了，请反馈给渡渡鸟吧");
                                }
                                break;
                            //#endregion
                            //#region 云从社
                            case "云从社":
                                requestBody = ofc.getJson("行侠","{\"name\": 云从社}","0").getBody();
                                if (Integer.parseInt(requestBody.get("code").toString()) == 200 && requestBody.get("data") != null){
                                    List<Map<String,Object>> tempList = (List<Map<String,Object>>)requestBody.get("data");
                                    tempMap = tempList.get(0);
                                    temp = "【正在进行中的事件】\\n" +
                                            "事件："+tempMap.get("desc").toString()+"\\n" +
                                            "地点："+tempMap.get("map_name").toString()+" • "+tempMap.get("site").toString()+"\\n" +
                                            "事件："+tempMap.get("time").toString()+"\\n";
                                    tempMap = tempList.get(1);
                                    temp = temp +
                                            "【将要开始的事件】\\n" +
                                            "事件："+tempMap.get("desc").toString()+"\\n" +
                                            "地点："+tempMap.get("map_name").toString()+" • "+tempMap.get("site").toString()+"\\n" +
                                            "事件："+tempMap.get("time").toString();
                                    WeChatHelper.sendMessage("http://"+session.getRemoteAddress().getHostString()+":8000/",userId,isGroup, "text", temp);
                                }else{
                                    WeChatHelper.sendMessage("http://"+session.getRemoteAddress().getHostString()+":8000/",userId,isGroup, "text", "碰到错误了，请反馈给渡渡鸟吧");
                                }
                                break;
                            //#endregion
                            //#region 鸽子牌AI
                            case "不懂就问":
                                if (commands.length <= 1) {
                                    return;
                                }
                                rootNode = mapper.readTree(HttpTool.getData("http://localhost:65510/ai/pigeon?question="+commands[1]));
                                if (rootNode.get("code").asInt() == 200){
                                    WeChatHelper.sendMessage("http://"+session.getRemoteAddress().getHostString()+":8000/",userId,isGroup, "text", StringEscapeUtils.escapeJava(rootNode.get("data").asText()));
                                }else{
                                    WeChatHelper.sendMessage("http://"+session.getRemoteAddress().getHostString()+":8000/",userId,isGroup, "text", "你丑到我了，我不想理你");
                                }
                                break;
                            //#endregion
                            //#region 激活
                            case "出来吧皮卡丘！":
                                ofc.activate(masterId,userId,"",isGroup, isGroup? "微信群":"微信用户");
                                WeChatHelper.sendMessage("http://"+session.getRemoteAddress().getHostString()+":8000/",userId,isGroup,"text","ご主人様，恭喜您领养成功");
                                break;
                            //#endregion
                            //#region
                            case "绑定":
                                if (commands.length <= 1){
                                    return;
                                }
                                ofc.activate(masterId,userId,commands[1],isGroup, isGroup? "微信群":"微信用户");
                                break;
                            //#endregion
                            //#region 百战精耐
                            case "精耐":
                                if (commands.length <= 1) {
                                    return;
                                }
                                requestBody = ofc.getJson("百战精耐",String.format("{\"server\": \"%s\", \"name\": \"%s\"}","飞龙在天", commands[1]),"0").getBody();
                                if (Integer.parseInt(requestBody.get("code").toString()) == 200 && requestBody.get("data") != null){
                                    tempMap = (Map<String, Object>) requestBody.get("data");
                                    temp = tempMap.get("roleName").toString() + "\\n"+
                                            "精：" + tempMap.get("gameEnergy").toString() + "\\n" +
                                            "耐：" + tempMap.get("gameStamina").toString();
                                    WeChatHelper.sendMessage("http://"+session.getRemoteAddress().getHostString()+":8000/",userId,isGroup, "text", temp);
                                }else{
                                    WeChatHelper.sendMessage("http://"+session.getRemoteAddress().getHostString()+":8000/",userId,isGroup, "text", "碰到错误了，请反馈给渡渡鸟吧");
                                }
                                break;
                            //#endregion
                            case "吃什么":
                                break;
                            case "复读":
                                WeChatHelper.sendMessage("http://"+session.getRemoteAddress().getHostString()+":8000/",userId,isGroup, "text", commands[1]);
                                break;
                            default:
                                break;
                        }
                        //#endregion
                        break;
                    case "meta":
                        switch (rootNode.get("detail_type").asText()){
                            case "connect":
                                // 机器人初次连接事件
                                if (!connectedUser.containsKey(session.getRemoteAddress().getHostString())){
                                    LOGGER.info("机器人："+session.getRemoteAddress().getHostString()+"正在尝试连接");
                                }
                                break;
                            case "status_update":
                                // 状态更新事件
                                if (!connectedUser.containsKey(session.getRemoteAddress().getHostString()) && !connectedUser.containsValue(rootNode.get("status").get("bots").get(0).get("self").get("user_id").asText())){
                                    LOGGER.info("-------机器人连接成功------");
                                    LOGGER.info("机器人："+rootNode.get("status").get("bots").get(0).get("self").get("user_id").asText());
                                    LOGGER.info("实际地址："+session.getRemoteAddress().getHostString());
                                    LOGGER.info("------------------------");

                                }
                                break;
                        }
                        break;
                }
            }catch (Exception e){
                WeChatHelper.sendMessage("http://"+session.getRemoteAddress().getHostString()+":8000/",userId,isGroup, "text", "出毛病了，快找人来修！");

            }
            //#endregion
        }


//        System.out.println(requestBody.get("data"));;
    }

    /**
     * 判断是否为JSON的方法
     * @param json 需要判断JSON文本
     * @return 是否为JSON
     */
    boolean isValidJson(String json) {
        try{
            ObjectMapper mapper = new ObjectMapper();
            JsonNode rootNode = mapper.readTree(json);
            if (rootNode == null){
                return false;
            }
        }catch (Exception e){
            return false;
        }
        return true;
    }

}
