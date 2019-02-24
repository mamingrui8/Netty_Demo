package io.netty.fixlength;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.nio.charset.StandardCharsets;

/**
 * Description: TODO
 * User: MaMingRui
 * Email: mamr@broada.com
 * Date: 2019年01月14日 22:06
 * ModificationHistory: Who         When         What
 * ---------  --------     ---------------------------
 */
@SuppressWarnings("Duplicates")
public class NettyServerFixLengthHandler extends ChannelHandlerAdapter {

    // 业务处理逻辑
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        String message = msg.toString();
        System.out.println("from client : " + message.trim());
        String line = "ok ";

        ctx.writeAndFlush(Unpooled.copiedBuffer(line.getBytes(StandardCharsets.UTF_8)));
    }


    // 异常处理逻辑
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        System.out.println("server exceptionCaught method run...");
        // cause.printStackTrace();
        ctx.close();
    }
}
