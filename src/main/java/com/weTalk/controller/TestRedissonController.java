package com.weTalk.controller;

import com.weTalk.dto.MessageSendDto;
import com.weTalk.entity.vo.ResponseVO;
import com.weTalk.websocket.MessageHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController("testRedissonController")
@RequestMapping("/redisson")
public class TestRedissonController extends ABaseController {

    @Resource
    private MessageHandler messageHandler;

    @RequestMapping("/testBroadcast")
    public ResponseVO testBroadcast() {
        MessageSendDto sendDto = new MessageSendDto();
        sendDto.setMessageContent("来自5050的消息" + System.currentTimeMillis());
        messageHandler.sendMessage(sendDto);
        return getSuccessResponseVO(null);
    }

}
