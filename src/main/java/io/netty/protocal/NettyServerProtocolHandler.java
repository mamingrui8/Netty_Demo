package io.netty.protocal;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;

import java.nio.charset.StandardCharsets;

/**
 * Description: 粘包---协议---服务端处理逻辑
 * User: MaMingRui
 * Email: mamr@broada.com
 * Date: 2019年01月16日 21:26
 * ModificationHistory: Who         When         What
 * ---------  --------     ---------------------------
 */
@ChannelHandler.Sharable
public class NettyServerProtocolHandler extends ChannelHandlerAdapter {
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg){
        String message = msg.toString();
        System.out.println("server receive protocol content: " + message); //服务端接收到的客户端依照协议内容发送而来的数据
        ProtocolParser parser = new ProtocolParser();
        parser.decodeString(message);

        String returnMsg = "收到信息了，你别再发了！";
        returnMsg = parser.encodeString(returnMsg);
        ctx.writeAndFlush(Unpooled.copiedBuffer(returnMsg.getBytes(StandardCharsets.UTF_8))); //再次强调，这里必须发送ByteBuf，如果直接发送String,则会发送失败
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception{
        ctx.close();
        System.out.println(cause.toString());
    }

    /**
     * 协议处理逻辑
     */
    static class ProtocolParser{
        /**
         * 根据协议解密数据
         */
        public void decodeString(String protocolMessage){
            String[] k = protocolMessage.split("HEADBODY");
            String content_length = ((k[0].split("HEAD"))[1].split(":"))[1];
            String body = k[1].substring(0, k[1].length()-4);
            System.out.println("客户端返回的信息被解析后: content_length=" + content_length + ", body=" + body);
        }
        /**
         * 根据协议加密数据
         */
        public String encodeString(String message){
            int content_length = message.length();
            return "HEADcontent-length:".concat(String.valueOf(content_length)).concat("HEADBODY").concat(message).concat("BODY");
        }
    }
}
