package client;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class FileDataClient {
    private final Socket dataSocket;
    private final PrintWriter pw; // 定义字符输出流
    private final BufferedInputStream bir; // 定义字符输入流

    public FileDataClient(String ip, String port) throws IOException {
        dataSocket = new Socket(ip, Integer.parseInt(port));
        // 得到网络输出字节流地址，并封装成网络输出字符流
        // 设置最后一个参数为true，表示自动flush数据
        OutputStream socketOut = dataSocket.getOutputStream();
        pw = new PrintWriter(new OutputStreamWriter(socketOut, StandardCharsets.UTF_8), true);

        // 得到网络输入字节流地址
        InputStream socketIn = dataSocket.getInputStream();
        bir = new BufferedInputStream(socketIn);
    }

    public void getFile(File saveFile) throws IOException {
        if (dataSocket != null) {
            FileOutputStream fileOut = new FileOutputStream(saveFile); // 新建本地空文件
            byte[] buf = new byte[1024]; // 用来缓存接收的字节数据

            // (2)向服务器发送请求的文件名，字符串读写功能
            pw.println("require " + saveFile.getName());
            pw.flush(); // 确保数据发送到服务器

            // (3)接收服务器的数据文件，字节读写功能
            int size;
            // 这里服务器端必须退出输出流,要不然会一直读取
            // 直接使用dataSocket.getInputStream()也可以
            while ((size = bir.read(buf)) != -1) { // 读一块到缓存，读取结束返回-1
                fileOut.write(buf, 0, size); // 写一块到文件
                System.out.println("读取到的数据大小" + size);
            }
            System.out.println("getfile函数结束了");
            fileOut.flush(); // 关闭前将缓存的数据全部推出
            fileOut.close(); // 关闭文件输出流
        }
    }
}