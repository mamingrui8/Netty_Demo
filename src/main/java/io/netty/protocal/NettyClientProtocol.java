package io.netty.protocal;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;

import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

/**
 * Description: TODO
 * User: MaMingRui
 * Email: mamr@broada.com
 * Date: 2019年01月16日 21:25
 * ModificationHistory: Who         When         What
 * ---------  --------     ---------------------------
 */
@SuppressWarnings("Duplicates")
public class NettyClientProtocol {

    // 处理请求和处理服务端响应的线程组
    private EventLoopGroup group = null;
    // 服务启动相关配置信息
    private Bootstrap bootstrap = null;

    public NettyClientProtocol(){
        init();
    }

    private void init(){
        group = new NioEventLoopGroup();
        bootstrap = new Bootstrap();
        // 绑定线程组
        bootstrap.group(group);
        // 设定通讯模式为NIO
        bootstrap.channel(NioSocketChannel.class);
    }

    /**
     *  无论是服务端还是客户端，handler处理器处理的是接收到的请求，也就是说，以下规则都是针对对方来定的
     */
    public ChannelFuture doRequest(String host, int port, final ChannelHandler... channelHandlers) throws InterruptedException{
        this.bootstrap.handler(new ChannelInitializer<SocketChannel>() {

            @Override
            protected void initChannel(SocketChannel ch) throws Exception {
                ch.pipeline().addLast(new StringDecoder(StandardCharsets.UTF_8));
                ch.pipeline().addLast(channelHandlers);
            }
        });
        ChannelFuture future = this.bootstrap.connect(host, port).sync();
        return future;
    }

    public void release(){
        this.group.shutdownGracefully();
    }

    public static void main(String[] args) {
        NettyClientProtocol client = null;
        ChannelFuture future = null;
        try{
            client = new NettyClientProtocol();

            future = client.doRequest("localhost", 9999, new NettyClientProtocolHandler());

            Scanner s = null;
            while(true){
                s = new Scanner(System.in);
                System.out.print("enter message send to server > ");
                String line = s.nextLine();
                line = NettyClientProtocolHandler.ProtocolParser.encodeString(line);
                /**
                 *  温馨提示: 这里一定不能直接使用Unpooled.copiedBuffer(line.getBytes(StandardCharset.UTF_8))
                 *  使用规定大小的字节数组作为中间参数来接收控制台入参的原因在于: byte[] bs = new byte[5]; 默认会创建包含5个0的byte数组。换句话说，如果我们在控制台输入一个字符"a"，则实际上发送给服务端的是 a 0 0 0 0 ,不足的位数自动被补上了0！
                 *  那么，使用 future.channel().writeAndFlush(Unpooled.copiedBuffer(line.getBytes(StandardCharsets.UTF_8)));  的坏处在于何处呢，让我们简单的分析一下:
                 *  1. 客户端在doRequest()的处理逻辑中看似规定了定长数据流的长度为5，可自己在传输时却未遵守诺言，随心所欲的传值。根据测试表明，客户端在传入了5个字节的数据后，服务端未能执行自定义处理器逻辑，反而是
                 */
                future.channel().writeAndFlush(Unpooled.copiedBuffer(line.getBytes(StandardCharsets.UTF_8)));
                TimeUnit.SECONDS.sleep(1);
            }
        }catch(Exception e){
            e.printStackTrace();
        }finally{
            if(null != future){
                try {
                    future.channel().closeFuture().sync();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            if(null != client){
                client.release();
            }
        }
    }
}
