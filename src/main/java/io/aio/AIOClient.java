package io.aio;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import java.util.concurrent.ExecutionException;

/**
 * Description: AIO客户端
 * User: MaMingRui
 * Email: mamr@broada.com
 * Date: 2019年01月06日 18:58
 * ModificationHistory: Who         When         What
 * ---------  --------     ---------------------------
 */
public class AIOClient {
    //客户端通道
    private AsynchronousSocketChannel channel;

    public AIOClient(String host, int port){
        init(host, port);
    }

    /**
     * 长时间连接，重复调用init()还是实现断线重连的功能
     * @param host
     * @param port
     */
    private void init(String host, int port){
        try{
            //开启通道
            channel = AsynchronousSocketChannel.open();
            //发起请求，建立连接
            channel.connect(new InetSocketAddress(host, port));
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    private void write(String line){
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        buffer.put(line.getBytes(StandardCharsets.UTF_8));
        buffer.flip();
        this.channel.write(buffer);
        buffer.clear();
    }

    private void read(){
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        //read()方法是异步方法，和AIOServerHandler类似，这里也可以多用一个get()阻塞方法，等待服务端数据的返回
        try {
            this.channel.read(buffer).get();
            buffer.flip();
            System.out.println("from Server:  " + new String(buffer.array(),"UTF-8"));
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    private void doDestory(){
        try {
            this.channel.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        AIOClient client = new AIOClient("localhost", 9999);
        try{
            System.out.print("please enter the message to Server >");
            Scanner scanner = new Scanner(System.in);
            String line = scanner.nextLine();
            client.write(line);
            client.read();
        }finally{
            client.doDestory();
        }
    }
}
