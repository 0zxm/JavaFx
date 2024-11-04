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
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class TCPMailClientFx_1 extends Application {

    private final Button btnCon = new Button("连接");

    private final Button btnSend = new Button("发送");

    private final TextArea textArea = new TextArea();
    private final TextField IpAdd_input = new TextField();
    private final TextField Port_input = new TextField();

    private final TextField input = new TextField();

    private TCPMailClient tcpmail;

    public static void main(String[] args) {
        launch(args);
    }

    public void start(Stage primaryStage) {

        primaryStage.setTitle("邮件测试");
        BorderPane mainPane = new BorderPane();
        VBox mainVBox = new VBox();

        HBox hBox = new HBox();
        hBox.setSpacing(10);//各控件之间的间隔
        //HBox面板中的内容距离四周的留空区域
        hBox.setPadding(new Insets(20, 20, 10, 20));
        hBox.getChildren().addAll(new Label("IP地址: "), IpAdd_input, new Label("端口: "), Port_input, btnCon);

        hBox.setAlignment(Pos.TOP_CENTER);

        VBox vBox = new VBox(10);
        vBox.setPadding(new Insets(10, 10, 10, 10));
        vBox.getChildren().addAll(textArea);
        vBox.setAlignment(Pos.CENTER);


        HBox inputBox = new HBox(10);
        inputBox.setPadding(new Insets(10, 10, 10, 10));
        inputBox.getChildren().addAll(input, btnSend);
        inputBox.setAlignment(Pos.CENTER);
        HBox.setHgrow(input, Priority.ALWAYS);
        // 获取加密后的邮箱账号密码
        String usrName = "m15813109801@163.com";
        String passwd = "MYnwt6tQtEHKZpLN";

        String encodedUsrName = BASE64.encode(usrName);
        String encodedPasswd = BASE64.encode(passwd);

        // 设置按钮的交互效果
        btnCon.setOnAction(event -> {
            String ip = IpAdd_input.getText().trim();
            String port = Port_input.getText().trim();
            if (ip.isEmpty() || port.isEmpty()) {
                return;
            }
            try {
                tcpmail = new TCPMailClient(ip, (port));
                Thread recieveThread = new Thread(() -> {
                    while (true) {
                        String msg = tcpmail.receive();
                        if (msg == null) {
                            continue;
                        }
                        Platform.runLater(() -> {
                            textArea.appendText(msg + "\n");
                        });
                    }
                }, "recieveThread");
                recieveThread.start();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        btnSend.setOnAction(event -> {
            String text = input.getText();
            if (text.isEmpty()) {
                return;
            }
            if (!tcpmail.isConnected()) {
                System.out.println("服务器未连接");
                return;
            }
            try {
                tcpmail.send(text + "\r\n");
                input.clear();
                Platform.runLater(() -> {
                    textArea.appendText("已发送：" + text + "\n");
                });
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
        input.setOnKeyPressed((event) -> {
            if (event.getCode() == KeyCode.ENTER) {
                btnSend.fire();
            }
        });

        Port_input.addEventHandler(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.ENTER) {
                btnCon.fire();
            }
        });

        mainVBox.getChildren().addAll(hBox, vBox, inputBox);

        mainPane.setCenter(mainVBox);
        Scene scene = new Scene(mainPane);

        IpAdd_input.setText("smtp.163.com");
        Port_input.setText("465");

        primaryStage.setScene(scene);
        primaryStage.show();
    }
}