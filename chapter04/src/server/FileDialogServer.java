package server;

import java.io.*;
import java.math.RoundingMode;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.util.Scanner;

public class FileDialogServer {
    public static ServerSocket msgserverSocket = null;
    public static ServerSocket fileserverSocket = null;

    public static void main(String[] args) {
        try {
            FileDialogServer server = new FileDialogServer();
            server.msgService();
            server.fileService();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void fileListPushToClient(PrintWriter pw) {
        String path = "d:/ftpserver"; // 给出服务器下载目录路径
        File filePath = new File(path);

        if (!filePath.exists()) { // 路径不存在则返回
            System.out.println("ftp下载目录不存在");
            return;
        }

        if (!filePath.isDirectory()) { // 如果不是一个目录就返回
            System.out.println("不是一个目录");
            return;
        }

        // 开始显示目录下的文件，不包括子目录
        String[] fileNames = filePath.list();
        File tempFile;

        // 格式化文件大小输出，不保留小数，不用四舍五入，有小数位就进1
        DecimalFormat formater = new DecimalFormat();
        formater.setMaximumFractionDigits(0);
        formater.setRoundingMode(RoundingMode.CEILING);

        for (String fileName : fileNames) {
            tempFile = new File(filePath, fileName);
            if (tempFile.isFile()) {
                pw.println(fileName + "  " + formater.format(tempFile.length() / (1024.0)) + "KB");
            }
        }
    }

    private PrintWriter getWriter(Socket socket) throws IOException {
        //获得输出流缓冲区的地址
        OutputStream socketOut = socket.getOutputStream();

        //将字符转为字节写入到socket
        return new PrintWriter(new OutputStreamWriter(socketOut, StandardCharsets.UTF_8), true);
    }

    private BufferedReader getReader(Socket socket) throws IOException {
        //获得输入流缓冲区的地址
        InputStream socketIn = socket.getInputStream();
        //读取字节数据返回字符串
        return new BufferedReader(new InputStreamReader(socketIn, StandardCharsets.UTF_8));
    }

    public void msgService() throws IOException {
        msgserverSocket = new ServerSocket(2021);
        System.out.println("Server is running on port 2021");
        Thread msgThread = new Thread(() -> {
            while (true) {
                Socket socket = null;
                try {
                    socket = msgserverSocket.accept();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                System.out.println("New client connected");
                try {
                    PrintWriter pw = getWriter(socket);
                    fileListPushToClient(pw);
                    BufferedReader br = getReader(socket);
                    String msg;
                    while ((msg = br.readLine()) != null) {
                        if ("bye".equals(msg)) {
                            break;
                        }
                        else{
                            System.out.println(msg);
                        }
                        // 处理其他消息
                    }
                } catch (Exception e) {
                    System.out.println("Error while handling client: " + e.getMessage());
                    e.printStackTrace();
                } finally {
                    try {
                        socket.close();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }, "msgThread");
        msgThread.start();
    }

    public void fileService() throws IOException {
        fileserverSocket = new ServerSocket(2020);
        System.out.println("fileServer is running on port 2020");
        Thread fileThread = new Thread(() -> {
            while (true) {
                Socket socket = null;
                try {
                    socket = fileserverSocket.accept();
                    System.out.println("New file client connected");
                    try {
                        PrintWriter pw = getWriter(socket);
                        BufferedReader br = getReader(socket);
                        String msg;
                        while ((msg = br.readLine()) != null) {
                            if (msg.startsWith("require ")) {
                                System.out.println(msg);
                                // 服务器请求文件
                                String fileName = msg.substring(8);
                                File requiredFile = new File("d:/ftpserver/" + fileName);
                                // 读取文件
                                Scanner sc = new Scanner(requiredFile, "UTF-8");
                                while (sc.hasNextLine()) { // 使用hasNextLine()确保换行符不会重复添加
                                    pw.println(sc.nextLine()); // 输出文件的内容,字节类型
                                }
                            }
                            System.out.println("文件没内容了,哥们");
                            // 处理其他消息
                            socket.close();
                        }
                    } catch (IOException e) {
                        System.out.println("Error while handling client: " + e.getMessage());
                        e.printStackTrace();
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                finally {
                    try {
                        socket.close();
                        System.out.println("socket关闭");
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        });
        fileThread.start();
    }
}