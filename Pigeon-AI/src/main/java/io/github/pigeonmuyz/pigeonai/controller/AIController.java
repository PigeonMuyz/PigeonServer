package io.github.pigeonmuyz.pigeonai.controller;


import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.ollama.api.OllamaApi;
import org.springframework.ai.ollama.api.OllamaOptions;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/ai")
public class AIController {
    public OllamaApi api = new OllamaApi("http://pigeon-server-ubuntu:11434");
    public OllamaChatModel client;
    public AIController(){

    }

    /**
     * 调用Ollama中的Gemma:2b模型
     * @param question 问题
     * @return 结果
     */
    @GetMapping("/gemma")
    Map<String, Object> gemmaQuestion(@RequestParam(required = true) String question){
        client = new OllamaChatModel(api, new OllamaOptions().withModel("gemma:2b"));
        return getStringObjectMap(question);
    }

    /**
     * 调用Ollama中的phi模型
     * @param question 问题
     * @return 结果
     */
    @GetMapping("/phi")
    Map<String, Object> phiQuestion(@RequestParam(required = true) String question){
        client = new OllamaChatModel(api, new OllamaOptions().withModel("phi"));
        return getStringObjectMap(question);
    }

    /**
     * 调用Ollama中的llama2模型
     * @param question 问题
     * @return 结果
     */
    @GetMapping("/llama2")
    Map<String, Object> llama2eQuestion(@RequestParam(required = true) String question){
        client = new OllamaChatModel(api, new OllamaOptions().withModel("llama2"));
        return getStringObjectMap(question);
    }

    /**
     * 调用Ollama中的PigeonKing:1模型
     * @param question 问题
     * @return 结果
     */
    @GetMapping("/pigeon")
    Map<String, Object> pigeonQuestion(@RequestParam(required = true) String question){
        client = new OllamaChatModel(api, new OllamaOptions().withModel("pigeonking:1"));
        return getStringObjectMap(question);
    }

    private synchronized Map<String, Object> getStringObjectMap(@RequestParam(required = true) String question) {
        Map<String, Object> response = new HashMap<>();
        response.put("code", HttpStatus.OK.value());
        response.put("message", "success");
        response.put("data",client.call(question));
        response.put("time", System.currentTimeMillis());
        return response;
    }
}
