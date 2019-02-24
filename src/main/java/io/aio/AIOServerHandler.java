package io.aio;

import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

/**
 * 这里属于父子线程 轮询操作
 */
public class AIOServerHandler implements CompletionHandler<AsynchronousSocketChannel, AIOServer> {  //这里之所以写两个泛型，主要是用于规范completed和failed这两个方法中的参数类型

    /**
     * 业务处理逻辑。 一旦客户端发起请求，服务端监听成功后，应该做些什么。
     * 一定要实现的逻辑: 为下一次客户端请求开启监听
     * @param result：和客户端直接建立关联的通道。
     *                无论是 BIO、NIO还是AIO，一旦连接建立，服务端和客户端是平等的。
     *                result中有通道中所有相关的数据。比如: 1.OS操作系统中准备好的盛放了从客户端读取的数据的缓存  2. 等待返回给客户端的数据的缓存
     * @param attachment
     */
    @Override
    public void completed(AsynchronousSocketChannel result, AIOServer attachment) {
        System.out.println("服务端收到了来自客户端的数据");
        doRead(result);
        //处理下一次的客户端请求，类似递归逻辑
        attachment.getServerChannel().accept(attachment, this);
        //读取数据。  毕竟刚才已经开启了对客户端的监听了，现在不用管客户端是否会连接到服务端了，趁着这段时间，服务端可以去读取刚才交互时，客户端发送过来的数据。

    }

    /**
     * 异常处理逻辑，当服务端代码出现异常时，做什么事情
     * @param exc
     * @param attachment
     */
    @Override
    public void failed(Throwable exc, AIOServer attachment) {
        exc.printStackTrace();
    }


    /**
     * 读取数据   (读取客户端发送给服务端的数据)
     */
    private void doRead(final AsynchronousSocketChannel channel){
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        /*
         * 异步读取操作， read(Buffer destination, A attachment, CompletionHandler<Integer, ? super A> handler)
         * 1. destination: 目的地， 是处理客户端传递数据的中转缓存，可以不使用
         * 2. attachment: 处理客户端传递数据的对象。 通常使用Buffer来处理
         * 3. handler: 处理逻辑
         */
        channel.read(buffer, buffer, new CompletionHandler<Integer, ByteBuffer>() {
            /**
             * 业务逻辑: 读取客户端传入的数据
             * @param result
             * @param attachment 在completed()方法正式执行时，OS已经将客户端请求的数据写入到了Buffer中了。
             *                   但是未进行复位！因此使用前一定一定要复位！
             */
            @Override
            public void completed(Integer result, ByteBuffer attachment) {
                System.out.println("开始进行读取操作了！， 本次客户端传递的数据对象attachment的大小为: " + attachment.capacity());
                //重置游标！
                attachment.flip();
                System.out.println("from client: " + new String(attachment.array(), StandardCharsets.UTF_8));  //此处不能用attachment.get()， 因为这样只能得到bufffer中游标位置的一个数据。这里需要的是把所有数据全部取出来
                doWrite(channel);
            }

            @Override
            public void failed(Throwable exc, ByteBuffer attachment) {
                System.out.println(String.format("服务端读取数据时报错了，attachment的长度为: %d,错误原因如下: %s", attachment.remaining(), exc.toString()));
            }
        });
    }

    /**
     * 写入数据  (服务端返回给客户端信息)
     * 真实的项目中，服务器返回的结果应该根据客户端的请求数据计算得到。而不是等待控制台输入。
     */
    private void doWrite(AsynchronousSocketChannel channel){
        try {
            ByteBuffer buffer = ByteBuffer.allocate(1024);
            System.out.print("enter message to client >");
            Scanner scanner = new Scanner(System.in);
            String line = scanner.nextLine();
            buffer.put(line.getBytes(StandardCharsets.UTF_8));
            //复位
            buffer.flip();
            //write()是异步操作，由OS来完成。但我们可以增加使用get()方法，实现阻塞，等待OS的写操作完成。
            channel.write(buffer);

            //channel.write(buffer).get(); //调用了get()方法表示服务端线程阻塞，等待写操作完成。
        } catch (Exception e){
            e.printStackTrace();
        }
//        catch (InterruptedException e) {
//            e.printStackTrace();
//        } catch (ExecutionException e) {
//            e.printStackTrace();
//        }
    }
}
