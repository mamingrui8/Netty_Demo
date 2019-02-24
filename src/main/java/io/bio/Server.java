package io.bio;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Description: bio服务端
 * User: MaMingRui
 * Email: mamr@broada.com
 * Date: 2019年02月21日 9:42
 * ModificationHistory: Who         When         What
 * ---------  --------     ---------------------------
 */
public class Server {
    public static void main(String[] args){
        int port = 9999;
        ServerSocket server =null;
        ExecutorService service = Executors.newFixedThreadPool(50);
        try{
            server = new ServerSocket(port);
            System.out.println("Server started!");
            Socket socket = server.accept(); //阻塞方法，直到和客户端的一条链路成功建立
            service.execute(new Handler(socket));
        } catch (Exception e){
            e.printStackTrace();
        } finally{
            if(null != server){
                try {
                    server.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    static class Handler implements Runnable{
        private Socket socket;

        public Handler(Socket socket){
            this.socket = socket;
        }

        @Override
        public void run() {
            try {
                BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
                PrintWriter writer =new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));
                String readMessage =null;
                while(true){
                    System.out.println("服务端正在读取数据...");
                    if((readMessage = reader.readLine()) == null){
                        System.out.println("没有接受到任何数据，故跳出循环！");
                        break;
                    }
                    writer.println("服务端接收到了以下数据: " + readMessage);
                    writer.flush();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }finally{
                if(null != socket){
                    try{
                        socket.close();
                    } catch (IOException e){
                       e.printStackTrace();
                    }
                }
            }
        }
    }
}
