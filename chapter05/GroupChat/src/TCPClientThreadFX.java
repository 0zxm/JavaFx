import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class TCPClientThreadFX extends Application {

    private final Button btnCon = new Button("连接");
    private final Button btnExit = new Button("退出");
    private final Button btnSend = new Button("发送");
    private final TextField IpAdd_input = new TextField();
    private final TextField Port_input = new TextField();
    private final TextArea OutputArea = new TextArea();
    private final TextField InputField = new TextField();
    private TCPClient tcpClient;
    private Thread receiveThread;

    private String no_name;

    public static void main(String[] args) {
        launch(args);
    }

    public void start(Stage primaryStage) {
        btnSend.setDisable(true);

        BorderPane mainPane = new BorderPane();
        VBox mainVBox = new VBox();

        HBox hBox = new HBox();
        hBox.setSpacing(10);
        hBox.setPadding(new Insets(20, 20, 10, 20));
        hBox.getChildren().addAll(new Label("IP地址: "), IpAdd_input, new Label("端口: "), Port_input, btnCon);
        hBox.setAlignment(Pos.TOP_CENTER);

        VBox vBox = new VBox();
        vBox.setSpacing(10);
        vBox.setPadding(new Insets(10, 20, 10, 20));
        vBox.getChildren().addAll(new Label("信息显示区:"), OutputArea, new Label("信息输入区"), InputField);
        // setVgrow()方法用于设置组件的拉伸策略，在这里设置为ALWAYS，即组件将会填充整个区域
        VBox.setVgrow(OutputArea, Priority.ALWAYS);
        OutputArea.setEditable(false);
        OutputArea.setStyle("-fx-wrap-text: true; -fx-font-size: 16px;");

        InputField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                btnSend.fire();
            }
        });

        HBox hBox2 = new HBox();
        hBox2.setSpacing(10);
        hBox2.setPadding(new Insets(10, 20, 10, 20));

        btnCon.setOnAction(event -> {
            String ip = IpAdd_input.getText().trim();
            String port = Port_input.getText().trim();
            btnCon.setDisable(true);
            try {
                tcpClient = new TCPClient(ip, port);
                receiveThread = new Thread(() -> {
                    String msg;
                    while ((msg = tcpClient.receive()) != null) {
                        String msgTemp = msg;
                        if (msgTemp.equals("clearScreen")) {
                            OutputArea.clear();
                            continue;
                        } else if (msgTemp.startsWith("no_name:")) {
                            no_name = msgTemp.split(":")[1];
                            continue;
                        }
                        Platform.runLater(() -> {
                            OutputArea.appendText(msgTemp + "\n");
                        });
                    }
                    Platform.runLater(() -> {
                        OutputArea.appendText("对话已关闭！\n");
                    });
                });
                receiveThread.start();
                btnSend.setDisable(false);
            } catch (Exception e) {
                OutputArea.appendText("服务器连接失败！" + e.getMessage() + "\n");
            }
        });

        btnExit.setOnAction(event -> {
            if (tcpClient != null) {
                tcpClient.send("bye");
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                tcpClient.close();
                btnSend.setDisable(true);
            }
            System.exit(0);
        });

        btnSend.setOnAction(event -> {
            String sendMsg = InputField.getText();
            if (sendMsg.trim().isEmpty()) {
                return;
            }
            tcpClient.send(sendMsg);
            InputField.clear();
            // 获取本机ip
            String ip;
            try {
                ip = InetAddress.getLocalHost().getHostAddress();
            } catch (UnknownHostException e) {
                throw new RuntimeException(e);
            }
            // 添加窗口标题
            primaryStage.setTitle(ip + " [" + no_name + "]");
            OutputArea.appendText("Me: " + sendMsg + "\n");
        });

        // 给文本区添加滚轮事件并且要按住Ctrl键增加字号
        OutputArea.setOnScroll(event -> {
            if (event.isControlDown()) {
                if (event.getDeltaY() > 0) {
                    OutputArea.setStyle("-fx-font-size: " + (OutputArea.getFont().getSize() + 1) + "px;");
                } else {
                    OutputArea.setStyle("-fx-font-size: " + (OutputArea.getFont().getSize() - 1) + "px;");
                }
            }
        });
        // 文本自动换行
        OutputArea.setWrapText(true);

        hBox2.setAlignment(Pos.CENTER_RIGHT);
        hBox2.getChildren().addAll(btnSend, btnExit);

        mainVBox.getChildren().addAll(hBox, vBox, hBox2);
        VBox.setVgrow(vBox, Priority.ALWAYS);
        mainPane.setCenter(mainVBox);
        Scene scene = new Scene(mainPane, 800, 550);

        IpAdd_input.setText("127.0.0.1");
        Port_input.setText("8888");

        primaryStage.setScene(scene);
        primaryStage.show();
    }
}