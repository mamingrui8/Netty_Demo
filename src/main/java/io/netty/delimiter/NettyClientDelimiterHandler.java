package io.netty.delimiter;


import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.ReferenceCountUtil;

/**
 * Description: TODO
 * User: MaMingRui
 * Email: mamr@broada.com
 * Date: 2019年01月15日 22:31
 * ModificationHistory: Who         When         What
 * ---------  --------     ---------------------------
 */
public class NettyClientDelimiterHandler extends ChannelHandlerAdapter {
    //msg本质上(底层)还是一个ByteBuf，只不过由于codec的StringUtil()的帮助，为ByteBuf的toString()加入了合适的Charset,因此能够直接toSting()成字符串  此处的msg带缓存！
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        try{
            String message = msg.toString();
            System.out.println("from server : " + message);
        }finally{
            // 用于释放缓存。避免内存溢出
            ReferenceCountUtil.release(msg);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        System.out.println("client exceptionCaught method run...");
        // cause.printStackTrace();
        ctx.close();
    }
}
