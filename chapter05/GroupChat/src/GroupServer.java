import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GroupServer {
    private final int port; // 服务器监听端口号
    private final ServerSocket serverSocket; //定义服务器套接字

    // 创建线程池
    private final ExecutorService executorService = Executors.newCachedThreadPool();

    // 线程安全的set集合
    public static CopyOnWriteArraySet<Socket> socketset = new CopyOnWriteArraySet<>();

    public GroupServer() throws IOException {
        port = 8080; // 例如，使用8080作为默认端口
        serverSocket = new ServerSocket(port);
        System.out.println("服务器启动监听在 " + port + " 端口");
    }

    public static void main(String[] args) throws IOException {
        GroupServer server = new GroupServer();
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

    private void sendToAllMembers(String msg, String hostAddress) throws IOException {
        PrintWriter pw;
        OutputStream out;
        for (Socket tempSocket : socketset) {
            out = tempSocket.getOutputStream();
            pw = new PrintWriter(
                    new OutputStreamWriter(out, StandardCharsets.UTF_8), true);
            pw.println(hostAddress + " 发言：" + msg);
        }
    }


    class ThreadHandler implements Runnable {
        private final Socket socket;

        public ThreadHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            //本地服务器控制台显示客户端连接的用户信息
            System.out.println("New connection accepted： " + socket.getInetAddress().getHostAddress());
            try {
                BufferedReader br = getReader(socket);//定义字符串输入流
                PrintWriter pw = getWriter(socket);//定义字符串输出流
                //客户端正常连接成功，则发送服务器的欢迎信息，然后等待客户发送信息
                pw.println("From 服务器：欢迎使用本服务！");
                String msg;
                //此处程序阻塞，每次从输入流中读入一行字符串
                while ((msg = br.readLine()) != null) {
                    //如果客户发送的消息为"bye"，就结束通信
                    if (msg.equalsIgnoreCase("bye")) {
                        //向输出流中输出一行字符串,远程客户端可以读取该字符串
                        pw.println("From服务器：服务器断开连接，结束服务！");
                        System.out.println("客户端离开");
                        //向输出流中输出一行字符串,远程客户端可以读取该字符串
                        break; //结束循环
                    }
                    sendToAllMembers(msg, socket.getInetAddress().getHostAddress());
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    socket.close(); //关闭socket连接及相关的输入输出流
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    //单客户版本，即每一次只能与一个客户建立通信连接
    public void Service() {
        while (true) {
            Socket socket = null;
            try {
                //此处程序阻塞等待，监听并等待客户发起连接，有连接请求就生成一个套接字。
                socket = serverSocket.accept(); // 从请求队列取一个socket请求
                socketset.add(socket);
                System.out.println("添加socket" + socket);
                Thread t = new Thread(new ThreadHandler(socket));
                executorService.execute(t);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}