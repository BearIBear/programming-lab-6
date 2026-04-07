import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;

public class MainServer {
    public static void main(String[] args) {
        try {
            Selector selector = Selector.open();
            DatagramChannel server = DatagramChannel.open();
            server.bind(new InetSocketAddress(3553));
            server.configureBlocking(false);
            server.register(selector, SelectionKey.OP_READ);
            ByteBuffer buffer = ByteBuffer.allocate(1024);
            while (true) {
                selector.select();
                SelectionKey selectedKey = selector.selectedKeys().toArray(SelectionKey[]::new)[0];
                selector.selectedKeys().remove(selectedKey);

                SocketAddress clientAddress = server.receive(buffer);
                buffer.flip();
                byte[] receivedData = new byte[buffer.remaining()];
                buffer.get(receivedData);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
