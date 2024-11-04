import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ChatServer {
    private final int port;
    private final ServerSocket serverSocket;
    private final ExecutorService executorService = Executors.newCachedThreadPool();
    public static ConcurrentHashMap<String, Socket> hashmap = new ConcurrentHashMap<>();

    public ChatServer() throws IOException {
        port = 8888;
        serverSocket = new ServerSocket(port);
        System.out.println("服务器启动监听在 " + port + " 端口");
    }

    public static void main(String[] args) throws IOException {
        ChatServer server = new ChatServer();
        System.out.println("服务器将监听端口号: " + server.port);
        server.Service();
    }

    private PrintWriter getWriter(Socket socket) throws IOException {
        OutputStream socketOut = socket.getOutputStream();
        return new PrintWriter(new OutputStreamWriter(socketOut, StandardCharsets.UTF_8), true);
    }

    private BufferedReader getReader(Socket socket) throws IOException {
        InputStream socketIn = socket.getInputStream();
        return new BufferedReader(new InputStreamReader(socketIn, StandardCharsets.UTF_8));
    }

    private void sendToAllMembers(String name, String msg) throws IOException {
        PrintWriter pw;
        OutputStream out;
        for (Map.Entry<String, Socket> entry : hashmap.entrySet()) {
            Socket tempSocket = entry.getValue();
            out = tempSocket.getOutputStream();
            pw = new PrintWriter(new OutputStreamWriter(out, StandardCharsets.UTF_8), true);
            pw.println(name + " 发言：" + msg);
        }
    }

    class ThreadHandler implements Runnable {
        private final Socket socket;

        public ThreadHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            System.out.println("New connection accepted： " + socket.getInetAddress().getHostAddress());
            try {
                PrintWriter pw = getWriter(socket);
                BufferedReader br = getReader(socket);
                pw.println("请输入用户名和学号，中间使用|分割");
                String no_name = br.readLine();
                // 为什么要加\\?因为|在正则表达式中有特殊含义，需要转义
                String no = no_name.split("\\|")[0];
                String name = no_name.split("\\|")[1];
                hashmap.put(no_name, socket);
                pw.println("From 服务器：欢迎使用本服务！");
                String msg;
                while ((msg = br.readLine()) != null) {
                    if (msg.equalsIgnoreCase("bye")) {
                        pw.println("From服务器：服务器断开连接，结束服务！");
                        System.out.println("客户端" + no_name + "离开");
                        break;
                    }
                    sendToAllMembers(no_name, msg);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void Service() {
        while (true) {
            Socket socket = null;
            try {
                socket = serverSocket.accept();
                System.out.println("添加socket" + socket);
                Thread t = new Thread(new ThreadHandler(socket));
                executorService.execute(t);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
