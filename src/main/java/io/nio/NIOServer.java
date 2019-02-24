package io.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Scanner;

/**
 * Description: NIO服务端
 * User: MaMingRui
 * Email: mamr@broada.com
 * Date: 2019年02月21日 11:27
 * ModificationHistory: Who         When         What
 * ---------  --------     ---------------------------
 */
public class NIOServer implements Runnable{
    //多路复用器，又叫选择器，用于注册通道
    private Selector selector;
    //定义两个缓存，分别用于write和read
    private ByteBuffer writeBuffer = ByteBuffer.allocate(1024); //创建了一个容量为1024个字节的缓冲区
    private ByteBuffer readBuffer = ByteBuffer.allocate(1024);

    public static void main(String[] args){
        new Thread(new NIOServer(9999)).start();
    }

    public NIOServer(int port){
        init(port);
    }

    private void init(int port){
        System.out.println("服务端准备启动，端口号: " + port + "...");
        try{
            //开启多路复用器
            this.selector = Selector.open();
            //开启服务通道
            ServerSocketChannel serverSocketChannel =ServerSocketChannel.open();
            //设置阻塞模式
            serverSocketChannel.configureBlocking(false);
            //绑定端口 InetSocketAddress: 端口+地址类型
            serverSocketChannel.bind(new InetSocketAddress(port));
            //把通道注册到多路复用器上，并标记当前服务通道的状态
            /*
                把当前通道注册哪种类型的事件上
                SelectionKey有四个常量来标识四种不同类型的事件，分别是:
                OP_ACCEPT： 一个ServerSocketChannel准备好接收(处理)新进入的连接
                OP_READ:  可以往当前通道写入数据
                OP_WRITE: 当前通道内有数据可读
                OP_CONNECT: 一条channel成功的连接到对端client服务器
             */
            serverSocketChannel.register(this.selector, SelectionKey.OP_ACCEPT);
            System.out.println("服务器启动完毕...");
    } catch (IOException e){
           e.printStackTrace();
        }
    }

    @Override
    public void run() {
        while(true){
            try{
                //此方法和scanner.readLine()很类似，都是阻塞方法。
                //如果没有准备好的socket,那么select()方法将会被阻塞一段时间并返回0
                //如果底层已有socket准备好，那么selector.select()将会返回socket的个数。而且selectedKeys方法会返回socket对应的事件(connect,accept,read or write)
                int readyChannels = this.selector.select();
                if (readyChannels == 0) continue;
                //返回已选中的通道标记的集合, 注意: 集合中保存的是通道的标记，相当于通道的ID。
                Iterator<SelectionKey> keys = this.selector.selectedKeys().iterator();
                while(keys.hasNext()){
                    SelectionKey key = keys.next();
                    //将本次要处理的通道从列表中删除，下次循环时，根据新的通道列表再次执行必要的业务逻辑
                    keys.remove();
                    //通道有效才进行下一步处理
                    if(key.isValid()){
                        //通道处于阻塞状态 - 对应上方事件 OP_CONNECT
                        try{
                            if(key.isAcceptable()){
                                accept(key);
                            }
                        } catch (CancelledKeyException e){
                            //通道出现了异常，因此断开连接
                           key.cancel();
                        }

                        //通道处于可读状态
                        try{
                            if(key.isReadable()){
                                read(key);
                            }
                        } catch (CancelledKeyException e){
                           key.cancel();
                        }

                        //通道处于可写状态
                        try{
                            if(key.isWritable()){
                                write(key);
                            }
                        } catch (CancelledKeyException e){
                           key.cancel();
                        }
                    }
                }
            } catch (IOException e){
               e.printStackTrace();
            }
        }
    }

    /**
     *  处于阻塞状态时的操作
     */
    private void accept(SelectionKey key){
        try{
            //获取到之前注册到Selector的channel
            ServerSocketChannel selectableChannel = (ServerSocketChannel)key.channel();
            //阻塞方法，当客户端发起请求后返回。此通道和客户端一一对应。
            SocketChannel channel = selectableChannel.accept();
            //将该通道设置为非阻塞
            channel.configureBlocking(false);

            //重新设置通道标记状态，将该通道标记为可读。
            //客户端与服务端的这条channel的状态从"accept"转变成"read"意味着此时客户端再次发送请求，selector有能力从channel中读取数据至缓冲区
            channel.register(this.selector, SelectionKey.OP_READ);
        } catch (IOException e){
           e.printStackTrace();
        }
    }

    /**
     *  读操作
     */
    private void read(SelectionKey key){
        try{
            //读写操作之前都需要清空缓存
            this.readBuffer.clear();
            //获取通道
            SocketChannel channel = (SocketChannel)key.channel();
            //将通道中的数据读取到缓存中。通道中的数据就是客户端发送给服务端的数据
            int readLength = channel.read(readBuffer);
            if(readLength == -1){
                //客户端没有发送任何的数据至服务端，因此服务端只好选择关闭通道
                key.channel().close();
                //断开与客户端之间的连接
                key.cancel();
                return;
            }
            /*
                Buffer通过游标来读写数据，游标信息在操作后不会归零，因此如果贸然访问Buffer，有可能产生数据不一致的后果。
                而flip()就是重置游标的方法。
             */
            this.readBuffer.flip();
            //readBuffer.remaining() 获取buffer实际有效长度
            byte[]datas =new byte[readBuffer.remaining()];
            //将buffer保存到字节数组中
            readBuffer.get(datas);
            System.out.println("从IP: " + channel.getRemoteAddress() + "的客户端而来，该客户端说: " + new String(datas, StandardCharsets.UTF_8));

            //最后，再次对通道的状态进行注册。也即，每次的通道状态仅能使用一次
            //本次把通道标记为"write"操作
            channel.register(this.selector, SelectionKey.OP_WRITE);
        } catch (IOException e){
           try{
                key.channel().close();
                key.cancel();
           } catch (Exception e1){
              e1.printStackTrace();
           }
        }
    }

    /**
     *  写操作
     */
    private void write(SelectionKey key){
        //清空缓存
        this.writeBuffer.clear();
        //获取通道
        SocketChannel channel = (SocketChannel)key.channel();
        //监控控制台输入
        Scanner scanner = new Scanner(System.in);

        try{
            System.out.print("请输入一些信息以发送给客户端吧：");
            String line = scanner.nextLine();
            //将控制台输入的字符穿写入至Buffer中，写入的数据是一个字节数组
            writeBuffer.put(line.getBytes(StandardCharsets.UTF_8));
            writeBuffer.flip();
            channel.write(writeBuffer);

            channel.register(this.selector, SelectionKey.OP_READ);
        } catch (IOException e){
           e.printStackTrace();
        }
    }
}
