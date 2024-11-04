import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.nio.charset.StandardCharsets;
public class Multicast {
    InetAddress groupIP;
    int port = 8900;
    MulticastSocket ms = null;
    byte[] inBuff = new byte[1024]; // 1MB数据
    byte[] outBuff = new byte[1024];

    public Multicast() throws IOException {
        groupIP = InetAddress.getByName("225.0.0.1");
        // 开启一个组播端口
        ms = new MulticastSocket(port);
        // 告诉网卡这样的 IP 地址数据包要接收
        ms.joinGroup(groupIP);
    }

    public void send(String msg) {
        try {
            outBuff = ("From/" + InetAddress.getLocalHost().toString() + " " + "20221003xxx xx" + msg).getBytes(StandardCharsets.UTF_8);
            DatagramPacket outPacket = new DatagramPacket(outBuff, outBuff.length, groupIP, port);
            ms.send(outPacket);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String receive() {
        try {
            DatagramPacket inPacket = new DatagramPacket(inBuff, inBuff.length);
            ms.receive(inPacket);
            String msg = new String(inPacket.getData(), 0, inPacket.getLength(), StandardCharsets.UTF_8);
            return "From " + inPacket.getAddress().getHostAddress() + " " + msg + "\n";
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public void close() {
        try {
            ms.leaveGroup(groupIP);
            ms.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
