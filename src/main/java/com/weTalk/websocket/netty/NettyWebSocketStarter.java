package com.weTalk.websocket.netty;

import com.weTalk.config.AppConfig;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

@Component
public class NettyWebSocketStarter implements Runnable{

    public static final Logger logger = LoggerFactory.getLogger(NettyWebSocketStarter.class);

    private static EventLoopGroup bossGroup = new NioEventLoopGroup(1);

    private static EventLoopGroup workGroup = new NioEventLoopGroup();

    @Resource
    private HandlerHeartBeat handlerHeartBeat;

    @Resource
    private HandlerWebSocket handlerWebSocket;

    @Resource
    private AppConfig appConfig;

    /**
     * 自定义一个启动方法 其实就是main里面的方法
     * 在Spring里创建一个新线程异步调用这个方法
     * <p>
     * 因为该方法里是死循环一直不停地监听连接
     * 所以要创建一个新线程异步调用这个方法启动Netty服务
     */
    @Override
    public void run() {
        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(bossGroup, workGroup);
            serverBootstrap.channel(NioServerSocketChannel.class).handler(new LoggingHandler(LogLevel.DEBUG))
                    .childHandler(new ChannelInitializer() {
                        @Override
                        protected void initChannel(Channel channel) {
                            ChannelPipeline pipeline = channel.pipeline();
                            //设置几个重要的处理器
                            //对http协议的支持，使用http的编码器，解码器
                            pipeline.addLast(new HttpServerCodec());
                            //聚合解码 httpRequest/httpContent/lastHttpContent 到 fullHttpRequest
                            //保证接收的http请求的完整性
                            pipeline.addLast(new HttpObjectAggregator(64 * 1024));

                            //心跳 Long readerIdleTime, Long writeIdleTime, Long allIdleTime, TimeUnit unit
                            //readerIdleTime 读超时时间 即测试端一定时间内未接收到被测试端消息
                            //writeIdleTime 写超时时间 即测试端一定时间内想被测试端发送消息
                            //allIdleTime 所有类型的超时时间
                            //设置心跳规则
                            pipeline.addLast(new IdleStateHandler(6, 0, 0, TimeUnit.SECONDS));
                            //处理心跳
                            pipeline.addLast(handlerHeartBeat);
                            //将http协议升级为ws协议， 对WebSocket支持
                            pipeline.addLast(new WebSocketServerProtocolHandler("/ws", null, true, 64 * 1024, true, true, 10000L));
                            pipeline.addLast(handlerWebSocket);
                        }
                    });

            ChannelFuture channelFuture = serverBootstrap.bind(appConfig.getWsPort()).sync();
            channelFuture.channel().closeFuture().sync();
            logger.info("Netty服务启动成功！端口号为:{}", appConfig.getWsPort());
        } catch (Exception e) {
            logger.error("启动Netty失败", e);
        } finally {
            bossGroup.shutdownGracefully();
            workGroup.shutdownGracefully();
        }
    }

    @PreDestroy
    public void close() {
        bossGroup.shutdownGracefully();
        workGroup.shutdownGracefully();
    }


    /**
     * 下面是以main方法启动
     *
     * 我们要将它交给Spring管理，用Spring启动
     */
//    public static void main(String[] args) {
//        try {
//            ServerBootstrap serverBootstrap = new ServerBootstrap();
//            serverBootstrap.group(bossGroup, workGroup);
//            serverBootstrap.channel(NioServerSocketChannel.class).handler(new LoggingHandler(LogLevel.DEBUG))
//                    .childHandler(new ChannelInitializer() {
//                        @Override
//                        protected void initChannel(Channel channel) {
//                            ChannelPipeline pipeline = channel.pipeline();
//                            //设置几个重要的处理器
//                            //对http协议的支持，使用http的编码器，解码器
//                            pipeline.addLast(new HttpServerCodec());
//                            //聚合解码 httpRequest/httpContent/lastHttpContent 到 fullHttpRequest
//                            //保证接收的http请求的完整性
//                            pipeline.addLast(new HttpObjectAggregator(64 * 1024));
//
//                            //心跳 Long readerIdleTime, Long writeIdleTime, Long allIdleTime, TimeUnit unit
//                            //readerIdleTime 读超时时间 即测试端一定时间内未接收到被测试端消息
//                            //writeIdleTime 写超时时间 即测试端一定时间内想被测试端发送消息
//                            //allIdleTime 所有类型的超时时间
//                            //设置心跳规则
//                            pipeline.addLast(new IdleStateHandler(6, 0, 0, TimeUnit.SECONDS));
//                            //处理心跳
//                            pipeline.addLast(new HandlerHeartBeat());
//                            //将http协议升级为ws协议， 对WebSocket支持
//                            pipeline.addLast(new WebSocketServerProtocolHandler("/ws", null, true, 64 * 1024, true, true, 10000L));
//                            pipeline.addLast(new HandlerWebSocket());
//                        }
//                    });
//
//            ChannelFuture channelFuture = serverBootstrap.bind(5051).sync();
//            channelFuture.channel().closeFuture().sync();
//            logger.info("Netty启动成功");
//        } catch (Exception e) {
//            logger.error("启动Netty失败", e);
//        } finally {
//            bossGroup.shutdownGracefully();
//            workGroup.shutdownGracefully();
//        }
//    }
}
