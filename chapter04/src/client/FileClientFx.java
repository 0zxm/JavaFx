package client;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;

public class FileClientFx extends Application {

    private final Button btnCon = new Button("连接");
    private final Button btnExit = new Button("退出");
    private final Button btnSend = new Button("发送");
    private final Button btnDownload = new Button("下载");
    private final TextField IpAdd_input = new TextField();
    private final TextField Port_input = new TextField();
    private final TextArea OutputArea = new TextArea();
    private final TextField InputField = new TextField();
    private FileDialogClient fileDialogClient;
    private Thread receiveMsgThread = null;
    private String ip, port;

    public static void main(String[] args) {
        launch(args);
    }

    public void start(Stage primaryStage) {
        primaryStage.setTitle("文件传输");
        btnSend.setDisable(true);
        BorderPane mainPane = new BorderPane();
        VBox mainVBox = new VBox();

        HBox hBox = new HBox();
        hBox.setSpacing(10);//各控件之间的间隔
        //HBox面板中的内容距离四周的留空区域
        hBox.setPadding(new Insets(20, 20, 10, 20));
        hBox.getChildren().addAll(new Label("IP地址: "), IpAdd_input, new Label("端口: "), Port_input, btnCon);

        hBox.setAlignment(Pos.TOP_CENTER);
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
        OutputArea.setStyle("-fx-wrap-text: true; /* 实际上是默认的 */ -fx-font-size: 14px;");
        mainPane.setCenter(vBox);

        InputField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                btnSend.fire();
            }
        });

        //底部按钮区域
        HBox hBox2 = new HBox();
        hBox2.setSpacing(10);
        hBox2.setPadding(new Insets(10, 20, 10, 20));

        // 重构thread，使用runnable接口，不要使用lambda表达式
        class ReceiveHandler implements Runnable {
            @Override
            public void run() {
                String msg = null;
                // 不知道服务器有多少回传信息，就持续不断接收
                // 由于在另外一个线程，不会阻塞主线程的正常运行
                while ((msg = fileDialogClient.receive()) != null) {
                    String msgTemp = msg; // msgTemp 实质是final类型
                    Platform.runLater(() -> {
                        OutputArea.appendText(msgTemp + "\n");
                    });
                }
                // 跳出了循环，说明服务器已关闭，读取为null，提示对话关闭
                Platform.runLater(() -> {
                    OutputArea.appendText("对话已关闭！\n");
                });
            }
        }


        // 设置按钮的交互效果
        btnCon.setOnAction(event -> {
            ip = IpAdd_input.getText().trim();
            port = Port_input.getText().trim();
            // 设置不能再次点击
            btnCon.setDisable(true);
            try {
                fileDialogClient = new FileDialogClient(ip, port);
                // 用于接收服务器信息的单独线程
                receiveMsgThread = new Thread(new ReceiveHandler(), "receiveThread");
                receiveMsgThread.start(); // 启动线程
                btnSend.setDisable(false);
            } catch (Exception e) {
                OutputArea.appendText("服务器连接失败！" + e.getMessage() + "\n");
            }
        });
        btnDownload.setOnAction(event -> {
            if (InputField.getText().equals("")) //没有输入文件名则返回
                return;
            String fName = InputField.getText().trim();
            InputField.clear();
            FileChooser fileChooser = new FileChooser();
            fileChooser.setInitialFileName(fName);
            File saveFile = fileChooser.showSaveDialog(null);
            if (saveFile == null) {
                return;//用户放弃操作则返回
            }
            try {
                //数据端口是2020
                FileDataClient fdclient = new FileDataClient(ip, "2020");
                fdclient.getFile(saveFile);
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setContentText(saveFile.getName() + " 下载完毕！");
                alert.showAndWait();
                //通知服务器已经完成了下载动作，不发送的话，服务器不能提供有效反馈信息
                fileDialogClient.send("客户端开启下载");
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        btnExit.setOnAction(event -> {
            if (fileDialogClient != null) {
                //
                // 新增代码
                try {
                    //向服务器发送关闭连接的约定信息
                    fileDialogClient.send("bye");
                    // 等待子线程和服务器 收到/读取信息完毕再关闭输入输出流，这样不会报错
                    Thread.sleep(500);
                    fileDialogClient.close();
                    btnSend.setDisable(true);
                    // 等待线程回收资源
                    receiveMsgThread.join();
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                }
            }
            System.exit(0);
        });
        Port_input.addEventHandler(KeyEvent.KEY_PRESSED, new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                if (event.getCode() == KeyCode.ENTER) {
                    btnCon.fire();
                }
            }
        });
        //信息显示区鼠标拖动高亮文字直接复制到信息输入框，方便选择文件名
        //taDispaly 为信息选择区的 TextArea，tfSend 为信息输入区的 TextField
        //为 taDisplay 的选择范围属性添加监听器，当该属性值变化(选择文字时)会触发监听器中的代码
        OutputArea.selectionProperty().addListener((observable, oldValue, newValue) -> {
            //只有当鼠标拖动选中了文字才复制内容
            if (!OutputArea.getSelectedText().equals(""))
                InputField.setText(OutputArea.getSelectedText());
        });


        btnSend.setOnAction(event -> {
            String sendMsg = InputField.getText();
            fileDialogClient.send(sendMsg);//向服务器发送一串字符
            InputField.clear();
            OutputArea.appendText("客户端发送：" + sendMsg + "\n");
        });

        hBox2.setAlignment(Pos.CENTER_RIGHT);
        hBox2.getChildren().addAll(btnSend, btnDownload, btnExit);

        mainVBox.getChildren().addAll(hBox, vBox, hBox2);

        mainPane.setCenter(mainVBox);
        Scene scene = new Scene(mainPane, 700, 400);

        IpAdd_input.setText("127.0.0.1");
        Port_input.setText("8888");

        primaryStage.setScene(scene);
        primaryStage.show();
    }
}