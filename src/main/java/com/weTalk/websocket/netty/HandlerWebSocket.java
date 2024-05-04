package com.weTalk.websocket.netty;

import com.weTalk.dto.TokenUserInfoDto;
import com.weTalk.redis.RedisComponent;
import com.weTalk.utils.StringTools;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
@ChannelHandler.Sharable
public class HandlerWebSocket extends SimpleChannelInboundHandler<TextWebSocketFrame> {

    public static final Logger logger = LoggerFactory.getLogger(HandlerWebSocket.class);

    @Resource
    private RedisComponent redisComponent;

    /**
     * 在通道就绪后调用，一般用来做初始化
     *
     * @param ctx
     * @throws Exception
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        logger.info("有新的连接加入......");
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        logger.info("有连接断开......");
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame textWebSocketFrame) throws Exception {
        Channel channel = ctx.channel();
        logger.info("收到消息{}", textWebSocketFrame.text());
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof WebSocketServerProtocolHandler.HandshakeComplete) {
            WebSocketServerProtocolHandler.HandshakeComplete complete = (WebSocketServerProtocolHandler.HandshakeComplete) evt;
            String url = complete.requestUri();
            String token = getToken(url);
            logger.info("url{}", url);
            if (null == token) {
                ctx.channel().close();
                return;
            }
            TokenUserInfoDto tokenUserInfoDto = redisComponent.getTokenUserInfoDto(token);
            if (null == tokenUserInfoDto) {
                ctx.channel().close();
                return;
            }

        }
        super.userEventTriggered(ctx, evt);
    }

    /**
     * 获取请求路径里的Token  url: /ws?token=123
     *
     * @param url
     * @return
     */
    private String getToken(String url) {
        if (StringTools.isEmpty(url) || url.indexOf("?") == -1) {
            return null;
        }
        String[] queryParams = url.split("\\?");
        //queryParams = {"/ws", "token=123"}
        if (queryParams.length != 2) {
            return null;
        }
        String[] params = queryParams[1].split("=");
        //params = {"token", "123"}
        if (params.length != 2) {
            return null;
        }
        return params[1];
    }

}
