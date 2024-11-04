import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class URLClient {
    private final URL url;
    private final BufferedReader br;

    public URLClient(String urlString) throws IOException {
        url = new URL(urlString);
        //获得url的字节流输入
        InputStream in = url.openStream();
        br = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));

    }
    public String readLine() throws IOException {
        return br.readLine();
    }

    public void close() throws Exception {
        br.close();
    }
}
