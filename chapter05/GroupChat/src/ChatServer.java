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
    // public static ConcurrentHashMap<HashMap<String, String>, Socket> hashmap = new ConcurrentHashMap<>();
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

    private void sendToAllMembers(String no_name, String msg, int message_type) throws IOException {
        PrintWriter pw;
        OutputStream out;
        for (Map.Entry<String, Socket> entry : hashmap.entrySet()) {
            if (entry.getKey().equals(no_name)) {
                continue;
            }
            Socket tempSocket = entry.getValue();
            out = tempSocket.getOutputStream();
            pw = new PrintWriter(new OutputStreamWriter(out, StandardCharsets.UTF_8), true);
            if (message_type == 0) { // 聊天消息
                pw.println(no_name + " 发言：" + msg);
            } else if (message_type == 1) { // 系统消息
                pw.println(msg);
            }

        }
    }

    private void listAllMembers(Socket socket) throws IOException {
        PrintWriter pr = getWriter(socket);
        for (Map.Entry<String, Socket> entry : hashmap.entrySet()) {
            pr.println(entry.getKey());
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
                pw.println("请输入用户名和学号，中间使用-分割");
                String no_name = br.readLine();
                // 正则的分割符是\|，所以这里要用\\|
                while (no_name.split("-").length < 2) {
                    pw.println("请输入正确的用户名和学号，中间使用-分割");
                    no_name = br.readLine();
                }
                // 为什么要加\\?因为|在正则表达式中有特殊含义，需要转义
                String name = no_name.split("-")[0];
                String no = no_name.split("-")[1];
                hashmap.put(no_name, socket);
                pw.println("clearScreen");
                pw.println("no_name:" + no_name);
                pw.println("From 服务器：已成功登录！");
                pw.println("From 服务器：默认是发送给全体用户的广播信息");
                pw.println("From 服务器：如果要发送私聊信息， 使用【学号1|学号2&私聊信息】方式给指定用户发送，例如发送【20181111111|20182222222&这是我发给你们的私聊信息】");
                pw.println("From 服务器：发送 #在线用户# 能获得所有在线用户的列表信息");

                // 处理消息
                String msg;
                while ((msg = br.readLine()) != null) {
                    if (msg.equalsIgnoreCase("bye")) {
                        pw.println("From服务器：服务器断开连接，结束服务！");
                        hashmap.remove(no_name);
                        System.out.println("客户端" + no_name + "离开");
                        sendToAllMembers(no_name, "系统消息：-------" + no_name + "离开-------", 1);
                        break;
                    } else if (msg.equals("#在线用户#")) {
                        listAllMembers(socket);
                        continue;
                    }
                    if (msg.matches("^【[0-9]*\\|[0-9]*&.*】$")) {
                        // 私聊消息
                        System.out.println("私聊消息：" + msg);
                        String[] split = msg.split("\\|");
                        String from_no = split[0].substring(1);
                        System.out.println("from_no：" + from_no);
                        if (!from_no.equals(no)) {
                            System.out.println(no);
                            pw.println("From服务器：学号1必须是自己的学号！");
                            continue;
                        }
                        String to_no = split[1].split("&")[0];
                        System.out.println("to_no：" + to_no);
                        String content = split[1].split("&")[1].substring(1, split[1].length() - 1);
                        System.out.println("content：" + content);
                        for (Map.Entry<String, Socket> entry : hashmap.entrySet()) {
                            if (entry.getKey().endsWith(to_no)) {
                                Socket tempSocket = entry.getValue();
                                System.out.println("Socket" + tempSocket);
                                PrintWriter tempPw = getWriter(tempSocket);
                                tempPw.println("From " + no_name + "：" + content);
                            }
                        }
                        continue;
                    }
                    sendToAllMembers(no_name, msg, 0);
                }

            } catch (
                    IOException e) {
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
            Socket socket;
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
