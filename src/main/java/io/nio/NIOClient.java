package io.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

/**
 * Description: NIO客户端
 * User: MaMingRui
 * Email: mamr@broada.com
 * Date: 2019年02月21日 15:24
 * ModificationHistory: Who         When         What
 * ---------  --------     ---------------------------
 */
public class NIOClient {
    public static void main(String[] args){
        //创建远端地址
        InetSocketAddress remoteAddress =new InetSocketAddress("localhost", 9999);
        //创建通道
        SocketChannel channel = null;
        //定义缓冲器
        ByteBuffer buffer =ByteBuffer.allocate(1024);

        try{
            //开启通道
            channel =SocketChannel.open();
            //把水管架设到指定的路径上，连接至Server
            channel.connect(remoteAddress);
            //监听控制台
            Scanner scanner = new Scanner(System.in);

            while(true){
                /*
                    写入
                 */
                System.out.print("请输入一些信息以发送给服务端吧：");
                String line = scanner.nextLine();
                if("exit".equals(line)){
                    break;
                }
                //将控制台输入的字符串写入到缓存中
                buffer.put(line.getBytes(StandardCharsets.UTF_8));
                //重置游标
                buffer.flip();
                //将缓存中的数据写入到channel中并发送给Server
                channel.write(buffer);
                buffer.clear();

                /*
                    读取
                 */
                //channel.read()方法是阻塞方法，当且仅当channel中获取到数据时才会返回
                int readLength = channel.read(buffer);
                if(readLength == -1) break;
                buffer.flip();
                byte[] bytes = new byte[buffer.remaining()];
                buffer.get(bytes);
                System.out.print("服务端说：" + new String(bytes, StandardCharsets.UTF_8));
                //清空buffer
                buffer.clear();
            }
        } catch (Exception e){
           e.printStackTrace();
        } finally {
            if(null != channel){
                try {
                    channel.close();
                }catch (IOException e){
                    e.printStackTrace();
                }
            }
        }
     }
}
