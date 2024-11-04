import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class TCPServer {
    private final int port; // 服务器监听端口号
    private final ServerSocket serverSocket; //定义服务器套接字

    public TCPServer() throws IOException {
        Scanner scanner = new Scanner(System.in); // 创建一个Scanner对象来读取标准输入
        System.out.println("请输入服务器监听的端口号:");
        if (scanner.hasNextInt()) { // 检查是否有下一个输入项并且是一个整数
            port = scanner.nextInt(); // 读取整数并赋值给port
        } else {
            System.out.println("输入错误，请输入一个有效的整数端口号。");
            // 这里可以根据需要处理错误情况，比如使用默认值或者退出程序
            port = 8080; // 例如，使用8080作为默认端口号
        }
        scanner.close(); // 关闭scanner对象

        serverSocket = new ServerSocket(port);
        System.out.println("服务器启动监听在 " + port + " 端口");
    }

    public static void main(String[] args) throws IOException {
        TCPServer server = new TCPServer();
        System.out.println("服务器将监听端口号: " + server.port);
        server.Service();
    }

    private PrintWriter getWriter(Socket socket) throws IOException {
        //获得输出流缓冲区的地址
        OutputStream socketOut = socket.getOutputStream();

        //网络流写出需要使用flush，这里在PrintWriter构造方法中直接设置为自动flush
        return new PrintWriter(
                new OutputStreamWriter(socketOut, StandardCharsets.UTF_8), true);

    }

    private BufferedReader getReader(Socket socket) throws IOException {
        //获得输入流缓冲区的地址
        InputStream socketIn = socket.getInputStream();
        return new BufferedReader(
                new InputStreamReader(socketIn, StandardCharsets.UTF_8));
    }

    //单客户版本，即每一次只能与一个客户建立通信连接
    public void Service() {
        while (true) {
            Socket socket = null;
            try {
                //此处程序阻塞等待，监听并等待客户发起连接，有连接请求就生成一个套接字。
                socket = serverSocket.accept();

                //本地服务器控制台显示客户端连接的用户信息
                System.out.println("New connection accepted： " + socket.getInetAddress().getHostAddress());
                BufferedReader br = getReader(socket);//定义字符串输入流
                PrintWriter pw = getWriter(socket);//定义字符串输出流
                //客户端正常连接成功，则发送服务器的欢迎信息，然后等待客户发送信息
                pw.println("From 服务器：欢迎使用本服务！");

                String msg = null;
                //此处程序阻塞，每次从输入流中读入一行字符串
                while ((msg = br.readLine()) != null) {
                    //如果客户发送的消息为"bye"，就结束通信
                    if (msg.equals("bye")) {
                        //向输出流中输出一行字符串,远程客户端可以读取该字符串
                        pw.println("From服务器：服务器断开连接，结束服务！");
                        System.out.println("客户端离开");
                        break; //结束循环
                    }
                    //向输出流中输出一行字符串,远程客户端可以读取该字符串
                    pw.println("From服务器：" + msg);

                }
            } catch (IOException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            } finally {
                try {
                    if (socket != null)
                        socket.close(); //关闭socket连接及相关的输入输出流
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }
}