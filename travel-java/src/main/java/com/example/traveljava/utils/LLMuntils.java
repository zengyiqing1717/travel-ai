package com.example.traveljava.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import okhttp3.*;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class LLMuntils {

    private final String apiKey;
    private final String baseUrl;
    private final String model;
    private final OkHttpClient client;
    private final ObjectMapper objectMapper;

    public LLMuntils(String apiKey, String baseUrl, String model) {
        this.apiKey = apiKey;
        this.baseUrl = baseUrl;
        this.model = model;
        this.objectMapper = new ObjectMapper();
        this.client = new OkHttpClient.Builder()
                .connectTimeout(60, TimeUnit.SECONDS)
                .readTimeout(120, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .build();
    }

    /**
     * 聊天接口（支持 system 提示）
     */
    public String chat(String systemPrompt, String userPrompt) {
        String requestBody = buildRequestBody(systemPrompt, userPrompt, false);
        Request request = new Request.Builder()
                .url(baseUrl + "/v1/chat/completions")   // 完整路径
                .addHeader("Content-Type", "application/json")
                .addHeader("Authorization", "Bearer " + apiKey)
                .post(RequestBody.create(requestBody, MediaType.parse("application/json; charset=utf-8")))
                .build();

        try (Response response = client.newCall(request).execute()) {
            // 1. 判断响应是否成功（修正：取反）
            if (!response.isSuccessful()) {
                String errorBody = response.body() != null ? response.body().string() : "";
                throw new IOException("LLM调用异常，HTTP " + response.code() + "，详情：" + errorBody);
            }
//            System.out.println(response.body().string());
            String responseBody = response.body().string();
            // 2. 解析并返回内容（修正：调用 extractContent）
            return extractContent(responseBody);

        } catch (IOException e) {
            throw new RuntimeException("LLM请求失败", e);
        }
    }

    /**
     * 提取 AI 回复内容
     */
    private String extractContent(String responseBody) throws IOException {
        JsonNode root = objectMapper.readTree(responseBody);
        JsonNode choices = root.path("choices");
        if (choices.isArray() && choices.size() > 0) {
            JsonNode message = choices.get(0).path("message");
            if (message.has("content")) {
                return message.get("content").asText();
            }
        }
        return "";
    }

    /**
     * 构建请求体（使用 ObjectMapper 保证 JSON 格式正确）
     */
    private String buildRequestBody(String systemPrompt, String userPrompt, boolean stream) {
        ObjectNode root = objectMapper.createObjectNode();
        root.put("model", model);
        root.put("stream", stream);
        root.put("temperature", 0.7);   // 可调

        ArrayNode messages = objectMapper.createArrayNode();

        // 添加 system 消息（如果提供）
        if (systemPrompt != null && !systemPrompt.trim().isEmpty()) {
            ObjectNode systemMsg = objectMapper.createObjectNode();
            systemMsg.put("role", "system");
            systemMsg.put("content", systemPrompt);
            messages.add(systemMsg);
        }

        // 添加 user 消息（必填）
        ObjectNode userMsg = objectMapper.createObjectNode();
        userMsg.put("role", "user");
        userMsg.put("content", userPrompt);
        messages.add(userMsg);

        root.set("messages", messages);

        try {
            return objectMapper.writeValueAsString(root);
        } catch (IOException e) {
            throw new RuntimeException("构建JSON请求体失败", e);
        }
    }

    public String chatStream(String systemPrompt, String userPrompt, Consumer<String> callback) throws IOException {
        String requestBody = buildRequestBody(systemPrompt, userPrompt, true);
        //创建一个请求
        Request request = new Request.Builder()
                .url(baseUrl + "/chat/completions")
                .addHeader("Content-Type", "application/json")
                .addHeader("Authorization", "Bearer " + apiKey)
                .addHeader("Accept", "text/event-stream")
                .post(RequestBody.create(requestBody, MediaType.parse("application/json; charset=utf-8")))
                .build();

        //记录完整的内容
        StringBuilder fullContent = new StringBuilder();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("LLM调用异常" + response.code());
            }
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(response.body().byteStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    // data:你好
                    // data:
                    if (line.startsWith("data: ")){
                        //截取字符串
                        String data = line.substring(6);
                        if ("[DONE]".equals(data)) {
                            break;
                        }
                        String content = parseStreamContent(data);
                        if (content != null && !content.isEmpty()){
                            fullContent.append(content);
                            if (callback != null) {
                                callback.accept(content);
                            }
                        }
                    }
                }
            }
        }
        return fullContent.toString();
    }
    private String parseStreamContent(String data) {
        try {
            JsonNode root = objectMapper.readTree(data);
            JsonNode choices = root.path("choices");
            if (choices.isArray() && choices.size() > 0) {
                JsonNode delta = choices.get(0).path("delta");
                return delta.path("content").asText("");
            }
        } catch (Exception e) {
            System.out.println("解析流式数据失败: {}" + e.getMessage());
        }
        return null;
    }
}