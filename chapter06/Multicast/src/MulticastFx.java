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

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class MulticastFx extends Application {

    private Multicast multicast;
    private final Button btnExit = new Button("退出");
    private final Button btnSend = new Button("发送");

    private final TextArea OutputArea = new TextArea();

    private final TextField InputField = new TextField();


    public void start(Stage primaryStage) throws IOException {

        BorderPane mainPane = new BorderPane();
        VBox mainVBox = new VBox();

        //内容显示区域
        VBox vBox = new VBox();
        vBox.setSpacing(10);//各控件之间的间隔
        //VBox面板中的内容距离四周的留空区域
        vBox.setPadding(new Insets(10, 20, 10, 20));
        vBox.getChildren().addAll(new Label("信息显示区:"), OutputArea, new Label("信息输入区"), InputField);
        //设置显示信息区的文本区域可以纵向自动扩充范围
        VBox.setVgrow(OutputArea, Priority.ALWAYS);
        // 设置文本只读和自动换行
        OutputArea.setEditable(false);
        OutputArea.setStyle("-fx-wrap-text: true; /* 实际上是默认的 */ -fx-font-size: 18px;");


        InputField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                btnSend.fire();
            }
        });

        //底部按钮区域
        HBox hBox2 = new HBox();
        hBox2.setSpacing(10);
        hBox2.setPadding(new Insets(10, 20, 10, 20));

        hBox2.setAlignment(Pos.CENTER_RIGHT);
        hBox2.getChildren().addAll(btnSend, btnExit);

        mainVBox.getChildren().addAll(vBox, hBox2);

        mainPane.setCenter(mainVBox);
        VBox.setVgrow(vBox, Priority.ALWAYS);
        Scene scene = new Scene(mainPane, 800, 600);


        multicast = new Multicast();

        Thread receiveThread = new Thread(() -> {
            while (true) {
                try {
                    String msg = multicast.receive();
                    Platform.runLater(() -> {
                        OutputArea.appendText(msg + "\n");
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, "receiveThread");

        receiveThread.start();


        btnExit.setOnAction(event -> {
            //TODO 退出程序
            System.exit(0);
        });

        btnSend.setOnAction(event -> {
            //TODO 发送消息
            String message = InputField.getText().trim();
            if (message.isEmpty()) {
                return;
            }
            multicast.send(message);
            try {
                OutputArea.appendText("From/" + InetAddress.getLocalHost().toString() + " " + "20221003xxx xx" + message + "\n");
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }
            InputField.clear();
        });
        // 添加滚轮事件
        OutputArea.setOnScroll(event -> { // event滚轮事件,从底层的gestureEvent中继承,里面定义了controlDown变量,表示是否按下了ctrl键
            if (event.isControlDown()) {
                if (event.getDeltaY() > 0) {
                    OutputArea.setStyle("-fx-font-size: " + (OutputArea.getFont().getSize() + 1) + "px;");
                } else {
                    OutputArea.setStyle("-fx-font-size: " + (OutputArea.getFont().getSize() - 1) + "px;");
                }
            }
        });
        OutputArea.setWrapText(true);

        primaryStage.setScene(scene);
        primaryStage.show();

    }

    public static void main(String[] args) {
        launch(args);
    }
}

