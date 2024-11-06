import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.*;
import java.nio.charset.StandardCharsets;

public class HTTPSClient {
    // 定义SSL套接字
    private final SSLSocket sslSocket;
    // 定义SSL套接字工厂
    private final SSLSocketFactory sslSocketFactory;
    private final PrintWriter pw; // 定义字符输出流
    private final BufferedReader br; // 定义字符输入流

    public HTTPSClient(String ip, String port) throws IOException {
        // 创建工厂对象()使用静态方法getDefault()获取默认的SSL套接字工厂
        sslSocketFactory = (SSLSocketFactory) SSLSocketFactory.getDefault();
        // 创造SSL套接字对象
        sslSocket = (SSLSocket) sslSocketFactory.createSocket(ip, Integer.parseInt(port));
        pw = new PrintWriter(new OutputStreamWriter(sslSocket.getOutputStream(), StandardCharsets.UTF_8), true);
        br = new BufferedReader(new InputStreamReader(sslSocket.getInputStream(), StandardCharsets.UTF_8));
        // 开始SSL握手
        sslSocket.startHandshake();
    }

    public void send(String message) {
        pw.println(message);
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

    public boolean isConnected() {
        return sslSocket.isConnected();
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
            if (sslSocket != null) {
                sslSocket.close(); // 关闭Socket连接
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
