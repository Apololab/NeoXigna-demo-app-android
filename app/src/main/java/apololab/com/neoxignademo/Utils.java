package apololab.com.neoxignademo;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;

public class Utils {

    public static int IO_BUFFER_SIZE = 4096;
    public static Charset DEFAULT_CHARSET = Charset.forName("UTF-8");

    public static String streamToString(InputStream stream) throws IOException{
        byte[] result = streamToByteArray(stream);
        return new String(result,DEFAULT_CHARSET);
    }

    public static byte[] streamToByteArray(InputStream inputStream) throws IOException {
        try (ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            copyStream(inputStream, output);
            return output.toByteArray();
        }
    }

    public static void copyStream(InputStream inputStream, OutputStream output) throws IOException {
        int len;
        byte[] buffer = new byte[IO_BUFFER_SIZE];
        while ((len = inputStream.read(buffer)) > 0) {
            output.write(buffer, 0, len);
        }
    }
}
