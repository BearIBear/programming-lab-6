import org.jline.utils.NonBlockingReader;

public class TestJline {
    public static void main(String[] args) throws Exception {
        System.out.println(NonBlockingReader.class.getMethod("read", long.class).getName());
    }
}
