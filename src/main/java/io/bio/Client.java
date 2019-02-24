package io.bio;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

/**
 * Description: bio客户端
 * User: MaMingRui
 * Email: mamr@broada.com
 * Date: 2019年02月21日 10:24
 * ModificationHistory: Who         When         What
 * ---------  --------     ---------------------------
 */
public class Client {
    public static void main(String[] args){
        try(
                Socket socket = new Socket("127.0.0.1", 9999);
                BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
                PrintWriter pw = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true);
        ){
            Scanner scanner = new Scanner(System.in);
            String message = null;
            while(true){
                System.out.println("进入本次循环");
                System.out.print("请输入想对服务端说的话: ");
                message = scanner.nextLine();
                if("exit".equals(message)){
                    System.out.println("客户端输入exit，主动断开连接");
                    break;
                }
                pw.println(message);
                System.out.println("准备进入下一次循环");
                System.out.println("我是客户端，我接收到的数据如下: " + br.readLine());
            }
        }catch (IOException e){
            e.printStackTrace();
        }
    }
}
