package io.netty.protocal;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.string.StringDecoder;

import java.nio.charset.StandardCharsets;

/**
 * Description: 说穿了，就是为处理器增加了一个处理逻辑——使用协议发送和解析接收到的数据包
 * User: MaMingRui
 * Email: mamr@broada.com
 * Date: 2019年01月16日 21:25
 * ModificationHistory: Who         When         What
 * ---------  --------     ---------------------------
 */
@SuppressWarnings("Duplicates")
public class NettyServerProtocol {
    // 监听线程组，监听客户端请求
    private EventLoopGroup acceptorGroup = null;
    // 处理客户端相关操作线程组，负责处理与客户端的数据通讯
    private EventLoopGroup clientGroup = null;
    // 服务启动相关配置信息
    private ServerBootstrap bootstrap = null;
    public NettyServerProtocol(){
        init();
    }
    private void init(){
        acceptorGroup = new NioEventLoopGroup();
        clientGroup = new NioEventLoopGroup();
        bootstrap = new ServerBootstrap();
        // 绑定线程组
        bootstrap.group(acceptorGroup, clientGroup);
        // 设定通讯模式为NIO
        bootstrap.channel(NioServerSocketChannel.class);
        // 设定缓冲区大小
        bootstrap.option(ChannelOption.SO_BACKLOG, 1024);
        // SO_SNDBUF发送缓冲区，SO_RCVBUF接收缓冲区，SO_KEEPALIVE开启心跳监测（保证连接有效）
        bootstrap.option(ChannelOption.SO_SNDBUF, 16*1024)
                .option(ChannelOption.SO_RCVBUF, 16*1024)
                .option(ChannelOption.SO_KEEPALIVE, true);
    }
    public ChannelFuture doAccept(int port, final ChannelHandler... acceptorHandlers) throws InterruptedException{

        bootstrap.childHandler(new ChannelInitializer<SocketChannel>() {

            @Override
            protected void initChannel(SocketChannel ch) throws Exception {
                ch.pipeline().addLast(new StringDecoder(StandardCharsets.UTF_8));
                ch.pipeline().addLast(acceptorHandlers);
            }
        });
        ChannelFuture future = bootstrap.bind(port).sync();
        return future;
    }
    public void release(){
        this.acceptorGroup.shutdownGracefully();
        this.clientGroup.shutdownGracefully();
    }

    public static void main(String[] args){
        ChannelFuture future = null;
        NettyServerProtocol server = null;
        try{
            server = new NettyServerProtocol();

            future = server.doAccept(9999, new NettyServerProtocolHandler());
            System.out.println("server started.");
            future.channel().closeFuture().sync();
        }catch(InterruptedException e){
            e.printStackTrace();
        }finally{
            if(null != future){
                try {
                    future.channel().closeFuture().sync();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            if(null != server){
                server.release();
            }
        }
    }
}
