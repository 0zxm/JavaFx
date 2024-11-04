import javafx.stage.FileChooser;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class TextFileIO {

    // 注意：这里移除了private PrintWriter pw; 和 private Scanner sc; 字段，因为我们在方法内部管理它们

    // 内容添加到文件中，文件通过对话框来确定

    public TextFileIO() {
    }  // 空构造方法

    public void append(String msg) {
        FileChooser fileChooser = new FileChooser();
        File file = fileChooser.showSaveDialog(null);
        if (file == null) { // 用户放弃操作则返回
            return;
        }

        // 使用try-with-resources语句自动关闭资源
        try (PrintWriter pw = new PrintWriter(new OutputStreamWriter(new FileOutputStream(file, true), StandardCharsets.UTF_8))) {
            pw.println(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 从文件中加载内容
    public String load() {
        FileChooser fileChooser = new FileChooser();
        File file = fileChooser.showOpenDialog(null);
        if (file == null) { // 用户放弃操作则返回
            return null;
        }

        StringBuilder sb = new StringBuilder();
        try (Scanner sc = new Scanner(file, "UTF-8")) {
            while (sc.hasNextLine()) { // 使用hasNextLine()确保换行符不会重复添加
                sb.append(sc.nextLine()).append("\n"); // 补上行读取的行末尾回车
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // 移除字符串末尾可能存在的多余换行符
        if (sb.length() > 0 && sb.charAt(sb.length() - 1) == '\n') {
            sb.setLength(sb.length() - 1);
        }

        return sb.toString();
    }
}