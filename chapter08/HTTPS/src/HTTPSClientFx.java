import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;

public class HTTPSClientFx extends Application {
    private final Button connectBtn = new Button("连接");
    private final Button requestBtn = new Button("网页请求");
    private final Button clearBtn = new Button("清空");
    private final Button quitBtn = new Button("退出");
    private final TextField urlField = new TextField();
    private final TextField portField = new TextField();
    private final TextArea responseArea = new TextArea();

    private HTTPSClient httpsc;

    private Thread receiveThread;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("HTTPS Client");
        BorderPane mainPane = new BorderPane();

        HBox topBox = new HBox();
        topBox.setSpacing(10);
        topBox.setPadding(new Insets(10));
        topBox.getChildren().addAll(new Label("网页地址: "), urlField, new Label("端口: "), portField, connectBtn);
        topBox.setAlignment(Pos.CENTER);

        VBox centerBox = new VBox(10);
        centerBox.setPadding(new Insets(10, 20, 10, 20));
        centerBox.getChildren().addAll(new Label("网页信息显示区: "), responseArea);
        centerBox.setAlignment(Pos.CENTER);

        HBox bottomBox = new HBox(10);
        bottomBox.setPadding(new Insets(10, 10, 10, 10));
        bottomBox.getChildren().addAll(requestBtn, clearBtn, quitBtn);
        bottomBox.setAlignment(Pos.CENTER_RIGHT);

        VBox mainBox = new VBox();
        mainBox.getChildren().addAll(topBox, centerBox, bottomBox);
        mainBox.setAlignment(Pos.CENTER);


        VBox.setVgrow(responseArea, Priority.ALWAYS);
        VBox.setVgrow(centerBox, Priority.ALWAYS);

        mainPane.setCenter(mainBox);

        Scene scene = new Scene(mainPane, 720, 450);

        // 按钮事件
        quitBtn.setOnAction(event -> {
            httpsc.send("Connection:close" + "\r\n");
            this.httpsc.close();
            receiveThread.interrupt();
            System.exit(0);
        });

        // 连接按钮事件
        connectBtn.setOnAction(event -> {
            try {
                String url = urlField.getText().trim();
                httpsc = new HTTPSClient(url, portField.getText().trim());
                if (httpsc.isConnected()) {
                    this.responseArea.appendText("连接成功！\n");
                }
                receiveThread = new Thread(() -> {
                    while (true) {
                        String response = httpsc.receive();
                        if (response == null) {
                            break;
                        }
                        Platform.runLater(() -> {
                            this.responseArea.appendText(response);
                        });
                    }
                }, "ReceiveThread");
                receiveThread.start();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        clearBtn.setOnAction(event -> {
            this.responseArea.clear();
        });

        requestBtn.setOnAction(event -> {
            httpsc.send("GET / HTTP/1.1");
            httpsc.send("Host: " + this.urlField.getText().trim());
            httpsc.send("Accept: */*");
            httpsc.send("Accept-Language: zh-cn");
            httpsc.send("User-Agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64)");
            httpsc.send("Connection: Keep-Alive");
            httpsc.send("\r\n");
        });


        urlField.setText("www.gdufs.edu.cn");
        portField.setText("443");

        this.responseArea.setEditable(false);
        this.responseArea.setWrapText(true);

        // 添加滚轮事件
        responseArea.setOnScroll(event -> {
            if (event.isControlDown()) {
                if (event.getDeltaY() > 0) {
                    responseArea.setStyle("-fx-font-size: " + (responseArea.getFont().getSize() + 1) + "px;");
                } else {
                    responseArea.setStyle("-fx-font-size: " + (responseArea.getFont().getSize() - 1) + "px;");
                }
            }
        });

        // 界面显示
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}

