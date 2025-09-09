package com.springboot.sb_chatgpt.dto;

import org.apache.logging.log4j.message.Message;

import java.util.List;

public record ChatGPTRequest(String model, List<Message> messages) {

    public static record Message (String role,String content){}
}
