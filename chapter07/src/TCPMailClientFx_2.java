import javafx.application.Application;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

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

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle(TITLE);

        mailServerAdd.setPrefColumnCount(20); // 设置文本框宽度

        primaryStage.show();
    }
}
