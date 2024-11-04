import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
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

public class LookUpScoreFX extends Application {

    private final Button btnCon = new Button("连接");
    private final Button btnExit = new Button("退出");
    private final Button btnSend = new Button("发送");
    private final TextField IpAdd_input = new TextField();
    private final TextField Port_input = new TextField();
    private final TextArea OutputArea = new TextArea();
    private final TextField InputField = new TextField();
    private LookUpScore lookUpScore;
    private Thread receiveThread = null;

    public static void main(String[] args) {
        launch(args);
    }

    public void start(Stage primaryStage) {
        //
        // 新增，设置标题，类名改成LookUpScoreFX
        primaryStage.setTitle("查看平时成绩");
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

        // 设置按钮的交互效果
        btnCon.setOnAction(event -> {
            String ip = IpAdd_input.getText().trim();
            String port = Port_input.getText().trim();
            // 设置不能再次点击
            btnCon.setDisable(true);
            try {
                //tcpClient不是局部变量，是本程序定义的一个TCPClient类型的成员变量
                lookUpScore = new LookUpScore(ip, port);
                // 用于接收服务器信息的单独线程
                receiveThread = new Thread(() -> {
                    String msg = null;
                    // 不知道服务器有多少回传信息，就持续不断接收
                    // 由于在另外一个线程，不会阻塞主线程的正常运行
                    while ((msg = lookUpScore.receive()) != null) {
                        String msgTemp = msg; // msgTemp 实质是final类型
                        Platform.runLater(() -> {
                            OutputArea.appendText(msgTemp + "\n");
                        });
                    }
                    // 跳出了循环，说明服务器已关闭，读取为null，提示对话关闭
                    Platform.runLater(() -> {
                        OutputArea.appendText("对话已关闭！\n");
                    });
                }, "receiveThread");
                receiveThread.start(); // 启动线程
                btnSend.setDisable(false);
            } catch (Exception e) {
                OutputArea.appendText("服务器连接失败！" + e.getMessage() + "\n");
            }
        });
        btnExit.setOnAction(event -> {
            if (lookUpScore != null) {
                //
                // 新增代码
                try {
                    //向服务器发送关闭连接的约定信息
                    lookUpScore.send("bye");
                    // 等待服务器收到/读取信息再关闭输入输出流，这样不会报错
                    Thread.sleep(1000);
                    lookUpScore.close();
                    btnSend.setDisable(true);
                    // 等待线程回收资源
                    receiveThread.join();
                } catch (Exception e) {
                    System.out.println(e.getStackTrace());
                }
            }
            System.exit(0);
        });
        Port_input.addEventHandler(KeyEvent.KEY_PRESSED, new EventHandler<javafx.scene.input.KeyEvent>() {
            @Override
            public void handle(javafx.scene.input.KeyEvent event) {
                if (event.getCode() == KeyCode.ENTER) {
                    btnCon.fire();
                }
            }
        });
        //
        // 结束
        btnSend.setOnAction(event -> {
            String sendMsg = InputField.getText();
            lookUpScore.send(sendMsg);//向服务器发送一串字符
            InputField.clear();
            OutputArea.appendText("客户端发送：" + sendMsg + "\n");
        });

        hBox2.setAlignment(Pos.CENTER_RIGHT);
        hBox2.getChildren().addAll(btnSend, btnExit);

        mainVBox.getChildren().addAll(hBox, vBox, hBox2);

        mainPane.setCenter(mainVBox);
        Scene scene = new Scene(mainPane, 700, 400);

        IpAdd_input.setText("127.0.0.1");
        Port_input.setText("8888");

        primaryStage.setScene(scene);
        primaryStage.show();
    }
}