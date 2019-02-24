package io.netty.delimiter;

/**
 * Description: 粘包问题解决方案——数据分隔符
 * User: MaMingRui
 * Email: mamr@broada.com
 * Date: 2019年01月15日 22:22
 * ModificationHistory: Who         When         What
 * ---------  --------     ---------------------------
 */

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;

import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

@SuppressWarnings("Duplicates")
public class NettyClientDelimiter {
    // 处理请求和处理服务端响应的线程组
    private EventLoopGroup group = null;
    // 服务启动相关配置信息
    private Bootstrap bootstrap = null;

    public NettyClientDelimiter(){
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
     *  核心部分
     */
    public ChannelFuture doRequest(String host, int port) throws InterruptedException{
        this.bootstrap.handler(new ChannelInitializer<SocketChannel>() {

            @Override
            protected void initChannel(SocketChannel ch) throws Exception {
                //数据分隔符， 我们定义的数据分隔符一定要是一个ByteBuf类型的数据对象！
                ByteBuf delimiterBuf = Unpooled.copiedBuffer("$MMR_CLIENT$".getBytes());
                ChannelHandler[] handlers = new ChannelHandler[3];

                /**
                 *  DelimiterBasedFrameDecoder同样继承了ChannelInboundHandlerAdaptor,所以也是一个处理器。
                 *  它是一个用于处理固定标记符号的Handler。注意，这里Handler没有@Sharable注解 修饰 (换句话说，)
                 *
                 */
                handlers[0] = new DelimiterBasedFrameDecoder(1024, delimiterBuf);
                // 字符串解码器Handler，会自动处理channelRead方法的msg参数，将ByteBuf类型的数据转换为字符串对象
                handlers[1] = new StringDecoder(StandardCharsets.UTF_8);
                handlers[2] = new NettyClientDelimiterHandler();

                ch.pipeline().addLast(handlers);
            }
        });
        ChannelFuture future = this.bootstrap.connect(host, port).sync();
        return future;
    }

    public void release(){
        this.group.shutdownGracefully();
    }

    public static void main(String[] args) {
        NettyClientDelimiter client = null;
        ChannelFuture future = null;
        try{
            client = new NettyClientDelimiter();

            future = client.doRequest("localhost", 9999);

            Scanner s = null;
            while(true){
                s = new Scanner(System.in);
                System.out.print("enter message send to server > ");
                String line = s.nextLine();
                future.channel().writeAndFlush(Unpooled.copiedBuffer(line.getBytes()));
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
