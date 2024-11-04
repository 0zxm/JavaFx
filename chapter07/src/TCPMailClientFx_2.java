import javafx.application.Application;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;

import static javafx.scene.layout.Priority.ALWAYS;

public class TCPMailClientFx_2 extends Application {
    private static final String TITLE = "TCP邮箱客户端";

    private final Button btnSend = new Button("发送");
    private final Button btnQuit = new Button("退出");
    private final TextField mailServerAdd = new TextField(" 邮件服务器地址 ");
    private final TextField mailServerPort = new TextField(" 邮件服务器端口 ");
    private final TextField mailFrom = new TextField(" 发件人地址 ");
    private final TextField mailTo = new TextField(" 收件人地址 ");
    private final TextField mailSubject = new TextField(" 邮件主题 ");
    private final TextArea mailContent = new TextArea(" 邮件内容 ");

    private TCPMailClient tcpMailClient;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle(TITLE);

        HBox hbox1 = new HBox();
        hbox1.getChildren().addAll(new Label("邮件服务器地址："), mailServerAdd, new Label("邮件服务器端口："), mailServerPort);

        HBox hbox2 = new HBox();
        hbox2.getChildren().addAll(new Label("发件人地址："), mailFrom, new Label("收件人地址："), mailTo);
        HBox hbox3 = new HBox();
        hbox3.getChildren().addAll(new Label("邮件主题："), mailSubject);

        VBox vbox = new VBox();
        vbox.getChildren().addAll(this.mailContent);

        HBox hbox4 = new HBox();
        hbox4.getChildren().addAll(btnSend, btnQuit);

        VBox mainBox = new VBox();
        mainBox.setPadding(new javafx.geometry.Insets(10));
        mainBox.getChildren().addAll(hbox1, hbox2, hbox3, vbox, hbox4);

        VBox.setVgrow(this.mailContent, ALWAYS);
        VBox.setVgrow(vbox, ALWAYS);

        primaryStage.setScene(new javafx.scene.Scene(mainBox));
        primaryStage.show();

        // 添加默认文字
        mailServerAdd.setText("smtp.163.com");
        mailServerPort.setText("465");
        mailFrom.setText("m15813109801@163.com");
        mailTo.setText("m15813109801@163.com");
        mailSubject.setText("测试邮件");
        mailContent.setText("这是一封测试邮件。");


        btnQuit.setOnAction(event -> {
            // 退出程序代码
            System.exit(0);
        });

        btnSend.setOnAction(event -> {
            // 发送邮件代码
            String smtpAddr = mailServerAdd.getText().trim();
            String smtpPort = mailServerPort.getText().trim();
            try {
                tcpMailClient = new TCPMailClient(smtpAddr, smtpPort);
                tcpMailClient.send("HELO myfriend");

                tcpMailClient.send("AUTH LOGIN");

                String userName = "m15813109801@163.com";
                String authCode = "MYnwt6tQtEHKZpLN";

                String msg = BASE64.encode(userName);
                tcpMailClient.send(msg);

                msg = BASE64.encode(authCode);
                tcpMailClient.send(msg);

                msg = "MAIL FROM:<" + mailFrom.getText().trim() + ">";
                tcpMailClient.send(msg);

                msg = "RCPT TO:<" + mailTo.getText().trim() + ">";
                tcpMailClient.send(msg);

                msg = "DATA";
                tcpMailClient.send(msg);

                msg = "FROM:" + mailFrom.getText().trim();
                tcpMailClient.send(msg);

                msg = "SUBJECT:" + mailSubject.getText().trim();
                tcpMailClient.send(msg);

                msg = "TO:" + mailTo.getText().trim();

                tcpMailClient.send(msg);

                // 发送空行,隔开邮件正文和内容
                tcpMailClient.send("");

                msg = "这是一封测试邮件。";
                tcpMailClient.send(msg);

                msg = ".";
                tcpMailClient.send(msg);

                tcpMailClient.send("QUIT");

                tcpMailClient.close();

                System.out.println("邮件发送成功！");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }
}