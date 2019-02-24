package io.netty.delimiter;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;

import java.nio.charset.StandardCharsets;

/**
 * Description: 特殊分隔符
 * User: MaMingRui
 * Email: mamr@broada.com
 * Date: 2019年01月15日 22:33
 * ModificationHistory: Who         When         What
 * ---------  --------     ---------------------------
 */
@SuppressWarnings("Duplicates")
public class NettyServerDelimiter {
    // 监听线程组，监听客户端请求
    private EventLoopGroup acceptorGroup = null;
    // 处理客户端相关操作线程组，负责处理与客户端的数据通讯
    private EventLoopGroup clientGroup = null;
    // 服务启动相关配置信息
    private ServerBootstrap bootstrap = null;
    public NettyServerDelimiter(){
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
    public ChannelFuture doAccept(int port) throws InterruptedException{
        bootstrap.childHandler(new ChannelInitializer<SocketChannel>() {

            @Override
            protected void initChannel(SocketChannel ch) throws Exception {
                //数据分隔符， 我们定义的数据分隔符一定要是一个ByteBuf类型的数据对象！
                ByteBuf delimiterBuf = Unpooled.copiedBuffer("$MMR_SERVER$".getBytes());
                ChannelHandler[] acceptorHandlers = new ChannelHandler[3];

                /**
                 *  DelimiterBasedFrameDecoder同样继承了ChannelInboundHandlerAdaptor,所以也是一个处理器。
                 *  它是一个用于处理固定标记符号的Handler。注意，这里Handler没有@Sharable注解 修饰
                 *  换句话说，DelimiterBasedFrameDecoder没有并发处理的能力，因此必须每次初始化通道时，创建一个新的处理器对象！！！
                 *
                 *  使用特殊分隔符解决粘包问题时，要像定长数据流一样，定义每个数据包的长度。 因为: netty建议数据包有最大长度...
                 *  这其实就和http网络请求一样，请求体按道理说应该是无穷大，但往往网站对比如附件上传的大小是有要求的，因为要考虑到网络传输的稳定性等因素，如果数据包过大，需要考虑是否支持断点续传等因素，麻烦且危险。
                 */
                acceptorHandlers[0] = new DelimiterBasedFrameDecoder(1024, delimiterBuf);
                // 字符串解码器Handler，会自动处理channelRead方法的msg参数，将ByteBuf类型的数据转换为字符串对象
                acceptorHandlers[1] = new StringDecoder(StandardCharsets.UTF_8);
                acceptorHandlers[2] = new NettyServerDelimiterHandler();
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
        NettyServerDelimiter server = null;
        try{
            server = new NettyServerDelimiter();

            future = server.doAccept(9999);
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
