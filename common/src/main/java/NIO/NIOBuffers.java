package NIO;

import java.io.FileNotFoundException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.FileChannel;

public class NIOBuffers {
    public static void main(String[] args) throws FileNotFoundException {
        RandomAccessFile rf =
                new RandomAccessFile("./common/src/main/resources/NIOdir/file1.txt", "rw");
        FileChannel channel = rf.getChannel();
        ByteBuffer buffer = ByteBuffer.allocate(5);
        for (int i = 0; i < 5; i++) {
            buffer.put((byte) i);
        }
        buffer.flip();
        System.out.println(buffer.get());
        System.out.println(buffer.get());
        System.out.println(buffer.get());
        System.out.println(buffer.get());
        System.out.println(buffer.get());
        buffer.flip();
        System.out.println(buffer.get());
        // SocketChannel - tcp
        // DatagramChannel - udp
    }
}
