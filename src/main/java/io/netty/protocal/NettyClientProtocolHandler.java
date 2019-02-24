package io.netty.protocal;

import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.ReferenceCountUtil;

/**
 * Description: TODO
 * User: MaMingRui
 * Email: mamr@broada.com
 * Date: 2019年01月16日 21:25
 * ModificationHistory: Who         When         What
 * ---------  --------     ---------------------------
 */
@SuppressWarnings("Duplicates")
public class NettyClientProtocolHandler extends ChannelHandlerAdapter {
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg){
        try{
            String message = msg.toString();
            System.out.println("client receive protocol content: " + message);
            ProtocolParser.decodeString(message);
            if(null == message){
                System.out.println("client received nothing");
                return;
            }
        }finally{
            ReferenceCountUtil.release(msg);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception{

    }

    /**
     * 协议处理逻辑
     */
    static class ProtocolParser{
        /**
         * 根据协议解密数据
         */
        public static void decodeString(String protocolMessage){
            String[] k = protocolMessage.split("HEADBODY");
            String content_length = ((k[0].split("HEAD"))[1].split(":"))[1];
            String body = k[1].substring(0, k[1].length()-4);
            System.out.println("客户端返回的信息被解析后: content_length=" + content_length + ", body=" + body);
        }
        /**
         * 根据协议加密数据
         */
        public static String encodeString(String message){
            int content_length = message.length();
            return "HEADcontent-length:".concat(String.valueOf(content_length)).concat("HEADBODY").concat(message).concat("BODY");
        }
    }
}
