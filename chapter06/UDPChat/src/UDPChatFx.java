import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashSet;

import static javafx.scene.input.KeyCode.ENTER;

public class UDPChatFx extends Application {
    private final UDPChat chat = new UDPChat();
    private final TextArea Output = new TextArea();
    private final TextField Input = new TextField();
    private final Button refreshButton = new Button("刷新在线用户");
    private final Button sendButton = new Button("发送");
    private final Button closeButton = new Button("关闭");
    private final ComboBox<String> ipComboBox = new ComboBox<>();

    public void start(Stage primaryStage) {
        BorderPane mainPane = new BorderPane();
        mainPane.setPadding(new Insets(10));

        ipComboBox.resize(150, 20);
        ipComboBox.setEditable(true);
        ipComboBox.getItems().add("所有用户");
        ipComboBox.getSelectionModel().select("所有用户");

        // 设置对话框区域
        VBox vbox = new VBox(10);
        vbox.getChildren().addAll(new Label("对话框"), Output);
        VBox.setVgrow(Output, Priority.ALWAYS);
        Output.setEditable(false);

        // 设置输入区域
        HBox hbox = new HBox(10);
        hbox.setAlignment(Pos.CENTER);
        HBox.setHgrow(Input, Priority.ALWAYS);
        hbox.getChildren().addAll(ipComboBox, refreshButton, Input, sendButton, closeButton);

        VBox mainVBox = new VBox(10);
        mainVBox.getChildren().addAll(vbox, hbox);
        mainPane.setCenter(mainVBox);
        VBox.setVgrow(vbox, Priority.ALWAYS);

        Thread ReceiveThread = new Thread(() -> {
            // 退出线程
            while (!chat.isClosed()) {
                String msg = chat.receive();
                System.out.println("接收到消息: " + msg);
                Platform.runLater(() -> Output.appendText(msg + "\n"));
            }
        }, "ReceiveThread");
        ReceiveThread.start();
        // 设置关闭按钮事件
        closeButton.setOnAction(e -> {
            chat.close();
            System.exit(0);
        });

        // 设置刷新按钮事件
        refreshButton.setOnAction(e -> {
            System.out.println("刷新在线用户列表");
            HashSet<String> onlineUsers = chat.refreshOnlineUsers();
            ipComboBox.getItems().clear();
            ipComboBox.getItems().add("所有用户");
            ipComboBox.getItems().addAll(onlineUsers);
            ipComboBox.getSelectionModel().select("所有用户");
        });

        Input.setOnKeyPressed(e -> {
            if (e.getCode() == ENTER) {
                sendButton.fire();
            }
        });
        // 设置发送按钮事件
        sendButton.setOnAction(e -> {
            String msg = Input.getText();
            if (msg.isEmpty()) {
                return;
            }
            if (ipComboBox.getSelectionModel().getSelectedItem().equals("所有用户")) {
                System.out.println("群发: " + msg);
                chat.send(msg, 1, null); // 默认群发
            } else {
                InetAddress ip = null;
                try {
                    ip = InetAddress.getByName(ipComboBox.getSelectionModel().getSelectedItem());
                } catch (UnknownHostException ex) {
                    throw new RuntimeException(ex);
                }
                chat.send(msg, 2, ip); // 指定用户群发
            }
            Output.appendText("我: " + msg + "\n");
            Input.clear();
        });

        primaryStage.setScene(new Scene(mainPane, 760, 450));
        primaryStage.setTitle("UDP Chat Application");
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
