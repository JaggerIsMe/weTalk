package com.weTalk.websocket.netty;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
@ChannelHandler.Sharable
public class HandlerHeartBeat extends ChannelDuplexHandler {

    public static final Logger logger = LoggerFactory.getLogger(HandlerHeartBeat.class);

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent e = (IdleStateEvent) evt;
            if (e.state() == IdleState.READER_IDLE) {
                logger.info("心跳超时");
                ctx.close();
            } else if (e.state() == IdleState.READER_IDLE) {
                ctx.writeAndFlush("heart");
            }
        }
    }
}
