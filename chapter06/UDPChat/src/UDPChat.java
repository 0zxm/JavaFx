import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class UDPChat {
    private final int port = 8118;
    private DatagramSocket socket;
    public InetAddress broadcastAddress; // 广播地址

    private Thread refreshThread; // 接收线程
    byte[] inBuff = new byte[512]; // 512字节 = 512B
    byte[] outBuff = new byte[512];

    // 创建一个数组
    private final HashSet<String> onlineUsers = new HashSet<>();

    public UDPChat() {
        try {
            socket = new DatagramSocket(port);
            socket.setBroadcast(true);
            broadcastAddress = InetAddress.getByName("255.255.255.255");
//            startRefreshThread(onlineUsers);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void send(String msg, int type, InetAddress address) {
        try {
            if (type == 1) { // 群播
                outBuff = ("From/" + InetAddress.getLocalHost().toString() + " " + "20221003xxx xx " + msg).getBytes(StandardCharsets.UTF_8);
                DatagramPacket outPacket = new DatagramPacket(outBuff, outBuff.length, broadcastAddress, port);
                socket.send(outPacket);
            } else if (type == 2) { // 单播
                System.out.println("单播消息：" + msg);
                outBuff = ("单播消息：" + msg).getBytes(StandardCharsets.UTF_8);
                DatagramPacket outPacket = new DatagramPacket(outBuff, outBuff.length, address, port);
                socket.send(outPacket);
            } else if (type == 3) { // 刷新`在线用户`
                outBuff = (msg).getBytes(StandardCharsets.UTF_8);
                DatagramPacket outPacket = new DatagramPacket(outBuff, outBuff.length, InetAddress.getByName("255.255.255.255"), port);
                socket.send(outPacket);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String receive() {
        try {
            DatagramPacket inPacket = new DatagramPacket(inBuff, inBuff.length);
            socket.receive(inPacket);
            String msg = new String(inPacket.getData(), 0, inPacket.getLength(), StandardCharsets.UTF_8);
            if (msg.equals("detect")) {
                // 发送检测请求
                send("echo", 3, null);
                return "detect";
            }
            return "receive: " + msg;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean isClosed() {
        return socket.isClosed();
    }

    public void close() {
        // 关闭套接字
        socket.close();
        System.out.println("Socket closed.");
    }

    //    public void startRefreshThread(HashSet<String> onlineUsers) {
//        // 接收刷新在线用户响应
//       this.refreshThread = new Thread(() -> {
//            while (true) {
//                try {
//                    DatagramPacket inPacket = new DatagramPacket(inBuff, inBuff.length);
//                    socket.receive(inPacket);
//                    String msg = new String(inPacket.getData(), 0, inPacket.getLength(), StandardCharsets.UTF_8);
//                    if (msg.equals("echo")) {
//                        String usrAddr = inPacket.getAddress().toString().substring(1);
//                        onlineUsers.add(usrAddr);
//                    }
//                } catch (Exception e) {
//                    e.printStackTrace();
//                } finally {
//                    // 加入适当的休眠时间
//                    try {
//                        Thread.sleep(5000); // 100毫秒
//                    } catch (InterruptedException e) {
//                        Thread.currentThread().interrupt(); // 恢复中断状态
//                    }
//                }
//            }
//        }, "RefreshThread");
//        refreshThread.start();
//    }
    public void RefreshUsers( HashSet<String> onlineUsers ) {
        long startTime = System.currentTimeMillis();
        long endTime = startTime + 5000; // 设置结束时间为当前时间加上5秒

        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<?> future = executor.submit(() -> {
            while (System.currentTimeMillis() < endTime) {
                try {
                    DatagramPacket inPacket = new DatagramPacket(inBuff, inBuff.length);
                    socket.receive(inPacket);
                    String msg = new String(inPacket.getData(), 0, inPacket.getLength(), StandardCharsets.UTF_8);
                    if ("echo".equals(msg)) {
                        String usrAddr = inPacket.getAddress().toString().substring(1);
                        onlineUsers.add(usrAddr);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        try {
            // 等待任务完成或者超时
            future.get(5, java.util.concurrent.TimeUnit.SECONDS);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            executor.shutdownNow(); // 尝试立即停止所有正在执行的任务
            try {
                if (!executor.awaitTermination(5, java.util.concurrent.TimeUnit.SECONDS)) {
                    executor.shutdownNow(); // 再次尝试强制停止
                }
            } catch (InterruptedException ex) {
                executor.shutdownNow();
                Thread.currentThread().interrupt(); // 恢复中断状态
            }
        }
    }

    public HashSet<String> refreshOnlineUsers() {
        // 发送刷新在线用户请求
        send("detect", 3, null);
        // 刷新在线用户
        RefreshUsers(onlineUsers);
        return onlineUsers;
    }
}
