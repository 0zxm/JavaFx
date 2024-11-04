import java.io.*;
import java.net.*;

public class SimpleHttp {
    public static void main(String[] args) {
        String hostname = "www.example.com"; // 服务器域名
        int port = 80; // 端口号

        try {
            // 创建Socket连接
            Socket socket = new Socket(hostname, port);
            System.out.println("连接到服务器: " + hostname + " 在端口: " + port);

            // 发送请求
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            out.println("GET / HTTP/1.1");
            out.println("Host: " + hostname);
            out.println("Connection: Close");
            out.println();

            // 接收响应
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String responseLine;
            while ((responseLine = in.readLine()) != null) {
                System.out.println(responseLine);
            }

            // 关闭连接
            in.close();
            out.close();
            socket.close();
        } catch (IOException e) {
            System.err.println("连接失败: " + e.getMessage());
        }
    }
}