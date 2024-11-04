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
        VBox.setVgrow(OutputArea, Priority.ALWAYS);
        OutputArea.setEditable(false);
        OutputArea.setStyle("-fx-wrap-text: true; -fx-font-size: 14px;");
        mainPane.setCenter(vBox);

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
            tcpClient.send(sendMsg);
            InputField.clear();
            OutputArea.appendText("客户端发送：" + sendMsg + "\n");
        });

        hBox2.setAlignment(Pos.CENTER_RIGHT);
        hBox2.getChildren().addAll(btnSend, btnExit);

        mainVBox.getChildren().addAll(hBox, vBox, hBox2);
        mainPane.setCenter(mainVBox);
        Scene scene = new Scene(mainPane, 700, 400);

        IpAdd_input.setText("127.0.0.1");
        Port_input.setText("8080");

        primaryStage.setScene(scene);
        primaryStage.show();
    }
}