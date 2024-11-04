package server;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.Date;

public class UDPServer {
    private final int port = 8888;
    private DatagramSocket socket;

    public UDPServer() {
        try {
            socket = new DatagramSocket(port);
            System.out.println("Server started on port " + port);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void Service(){
        while (true) {
            byte[] buffer = new byte[1024];
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
            try {
                socket.receive(packet);
                String message = new String(packet.getData(), 0, packet.getLength());
                System.out.println("Received message: " + message);
                String response = "20221003174&徐彬&"+ new Date() + "&" + message;
                byte[] responseBytes = response.getBytes();
                // 返回响应
                DatagramPacket responsePacket = new DatagramPacket(responseBytes, responseBytes.length, packet.getAddress(), packet.getPort());
                socket.send(responsePacket);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    public static void main(String[] args) {
            new UDPServer().Service();
    }
}
