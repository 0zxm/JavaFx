import javafx.application.Application;
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

import java.time.LocalDateTime;


public class SimpleFx extends Application {

    private final Button btnExit = new Button("退出");

    private final Button btnSend = new Button("发送");
    private final Button btnOpen = new Button("加载");
    private final Button btnSave = new Button("保存");

    //待发送信息的文本框
    private final TextField tfSend = new TextField();
    //显示信息的文本区域
    private final TextArea taDisplay = new TextArea();

    public static void main(String[] args) {
        launch(args);
    }

    public void start(Stage primaryStage) {
        BorderPane mainPane = new BorderPane();
        //内容显示区域
        VBox vBox = new VBox();
        vBox.setSpacing(10);//各控件之间的间隔
        //VBox面板中的内容距离四周的留空区域
        vBox.setPadding(new Insets(10, 20, 10, 20));
        vBox.getChildren().addAll(new Label("信息显示区:"), taDisplay, new Label("信息输入区"), tfSend);
        //设置显示信息区的文本区域可以纵向自动扩充范围
        VBox.setVgrow(taDisplay, Priority.ALWAYS);
        taDisplay.setEditable(false);
        taDisplay.setStyle("-fx-wrap-text: true; /* 实际上是默认的 */ -fx-font-size: 14px;");
        mainPane.setCenter(vBox);   // 设置文本只读和自动换行
        //底部按钮区域
        HBox hBox = new HBox();
        hBox.setSpacing(10);
        hBox.setPadding(new Insets(10, 20, 10, 20));

        // 设置按钮的交互效果
        btnExit.setOnAction(event -> {
            System.exit(0);
        });
        btnSend.setOnAction(event -> {
            String msg = tfSend.getText();
            taDisplay.appendText(msg + "\n");
            tfSend.clear();
        });

        // 文件操作类
        TextFileIO textFileIO = new TextFileIO();

        btnSave.setOnAction(event -> {
            textFileIO.append(
                    LocalDateTime.now().withNano(0) + " " + taDisplay.getText());
        });
        btnOpen.setOnAction(event -> {
            String msg = textFileIO.load();
            if (msg != null) {
                taDisplay.clear();
                taDisplay.setText(msg);
            }
        });

        tfSend.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                String text = tfSend.getText();
                String prefix = event.isShiftDown() ? "echo: " : "";
                taDisplay.appendText(prefix + text + "\n");
                tfSend.setText(""); // 清空文本框
            }
        });

        hBox.setAlignment(Pos.CENTER_RIGHT);
        hBox.getChildren().addAll(btnSend, btnSave, btnOpen, btnExit);
        mainPane.setBottom(hBox);
        Scene scene = new Scene(mainPane, 700, 400);

        primaryStage.setScene(scene);
        primaryStage.show();


    }

}
