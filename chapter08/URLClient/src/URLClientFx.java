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
import java.net.MalformedURLException;
import java.net.URL;

public class URLClientFx extends Application {
    private final Button sendBtn = new Button("发送");
    private final Button quitBtn = new Button("退出");
    private final TextField urlField = new TextField();
    private final TextArea responseArea = new TextArea();
    private URLClient urlClient;

    private Thread receiveThread;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("URL Downloader");
        BorderPane mainPane = new BorderPane();

        VBox centerBox = new VBox(10);
        centerBox.setPadding(new Insets(10, 20, 10, 20));
        centerBox.getChildren().addAll(new Label("网页信息显示区: "), responseArea);
        centerBox.setAlignment(Pos.CENTER);

        VBox urlBox = new VBox(10);
        urlBox.setPadding(new Insets(10, 20, 10, 20));
        urlBox.getChildren().addAll(new Label("输入URL地址: "), urlField);
        urlBox.setAlignment(Pos.CENTER_LEFT);

        HBox bottomBox = new HBox(10);
        bottomBox.setPadding(new Insets(10, 20, 10, 20));
        bottomBox.getChildren().addAll(sendBtn, quitBtn);
        bottomBox.setAlignment(Pos.CENTER_RIGHT);

        VBox mainBox = new VBox();
        mainBox.getChildren().addAll(centerBox, urlBox, bottomBox);
        mainBox.setAlignment(Pos.CENTER);


        VBox.setVgrow(responseArea, Priority.ALWAYS);
        VBox.setVgrow(centerBox, Priority.ALWAYS);

        mainPane.setCenter(mainBox);

        Scene scene = new Scene(mainPane, 720, 450);

        // 按钮事件处理
        quitBtn.setOnAction(event -> System.exit(0));
        sendBtn.setOnAction(
                event -> {
                    try {
                        String url = this.urlField.getText().trim();
                        try {
                            this.urlClient = new URLClient(url);
                        } catch (MalformedURLException e) {
                            responseArea.appendText("URL格式错误！\n");
                            return;
                        }
                        Thread receiveThread = new Thread(() -> {
                            while (true) {
                                try {
                                    String line = this.urlClient.readLine();
                                    if (line == null) {
                                        break;
                                    }
                                    Platform.runLater(
                                            () -> {
                                                this.responseArea.appendText(line);
                                            }
                                    );
                                } catch (IOException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                        }, "receiveThread");
                        receiveThread.start();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }

                }
        );

        urlField.setText("http://www.baidu.com");
        responseArea.setEditable(false);
        responseArea.setWrapText(true);
        // 界面显示
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}

