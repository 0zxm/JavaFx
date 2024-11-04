package client;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class FileDialogClient {
    private final Socket socket; // 定义套接字
    private final PrintWriter pw; // 定义字符输出流
    private final BufferedReader br; // 定义字符输入流

    public FileDialogClient(String ip, String port) throws IOException {
        // 主动向服务器发起连接，实现TCP的三次握手过程
        // 如果不成功，则抛出错误信息，其错误信息交由调用者处理
        socket = new Socket(ip, Integer.parseInt(port));

        // 得到网络输出字节流地址，并封装成网络输出字符流
        // 设置最后一个参数为true，表示自动flush数据
        OutputStream socketOut = socket.getOutputStream();
        pw = new PrintWriter(new OutputStreamWriter(socketOut, StandardCharsets.UTF_8), true);

        // 得到网络输入字节流地址，并封装成网络输入字符流
        InputStream socketIn = socket.getInputStream();
        br = new BufferedReader(new InputStreamReader(socketIn, StandardCharsets.UTF_8));
    }

    public void send(String msg) {
        // 输出字符流，由Socket调用系统底层函数，经网卡发送字节流
        pw.println(msg);
    }

    public String receive() {
        String msg = null;
        try {
            // 从网络输入字符流中读信息，每次只能接收一行信息
            // 如果不够一行（无行结束符），则该语句阻塞等待
            msg = br.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return msg;
    }

    // 实现close方法以关闭socket连接及相关的输入输出流
    public void close() {
        try {
            if (pw != null) {
                pw.close(); // 关闭PrintWriter会先flush再关闭底层流
            }
            if (br != null) {
                br.close(); // 关闭BufferedReader
            }
            if (socket != null) {
                socket.close(); // 关闭Socket连接
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}