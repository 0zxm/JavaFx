import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class BASE64 {
    public static void main(String[] args) {
        String usrName = "m15813109801@163.com";
        String passwd = "MYnwt6tQtEHKZpLN";
//        String usrName = "2378173954@qq.com";
//        String passwd = "syveljrcjezcdjae";

        String encodedUsrName = encode(usrName);
        String encodedPasswd = encode(passwd);

        System.out.println("Encoded Username: " + encodedUsrName);
        System.out.println("Encoded Password: " + encodedPasswd);
    }
    public static String encode(String str) {
//        return new sun.misc.BASE64Encoder().encode(str.getBytes());
        Base64.Encoder encoder = Base64.getEncoder();
        String encodedMmsg = null;
        try {
            encodedMmsg = encoder.encodeToString(str.getBytes(StandardCharsets.UTF_8));
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return encodedMmsg;
    }
}
