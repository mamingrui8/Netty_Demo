package io.aio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Description: AIO服务端
 * User: MaMingRui
 * Email: mamr@broada.com
 * Date: 2019年01月06日 18:58
 * ModificationHistory: Who         When         What
 * ---------  --------     ---------------------------
 */
public class AIOServer {
    //线程池，用于提高服务端效率
    private ExecutorService service;

    //服务端通道
    private AsynchronousServerSocketChannel serverChannel;

    public AIOServer(int port){
        init(9999);
    }

    private void init(int port){
        try{
            System.out.println("server starting at port : " + port + "...");
            //创建一个定长的线程池
            service = Executors.newFixedThreadPool(6);  //我的电脑CPU是6核
            //创建服务端通道
            serverChannel = AsynchronousServerSocketChannel.open();
            //绑定端口。此时，服务器已启动成功，但未能监听任何的请求。
            serverChannel.bind(new InetSocketAddress(port));
            System.out.println("server started.");
            /*
                开启监听
                accept(A attachment, CompletionHandler<AsynchronousSocketChannel,? super A> handler)
                AIO开发中，监听是一个类似递归的监听操作。每次监听到客户端请求后 ，都需要处理逻辑去开启下一次的监听操作。   (简言之，监听的开启 并非是一劳永逸的)
                下一次的监听，需要服务器的资源继续支持。
             */
            serverChannel.accept(this, new AIOServerHandler()); //传递this是为了传递 this.serverChannel 这个通道
            try{
                TimeUnit.SECONDS.sleep(Integer.MAX_VALUE);
            }catch (InterruptedException e){
                e.printStackTrace();
            }
        }catch(IOException e){
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        new AIOServer(9999);
    }

    public ExecutorService getService() {
        return service;
    }

    public void setService(ExecutorService service) {
        this.service = service;
    }

    public AsynchronousServerSocketChannel getServerChannel() {
        return serverChannel;
    }

    public void setServerChannel(AsynchronousServerSocketChannel serverChannel) {
        this.serverChannel = serverChannel;
    }
}
