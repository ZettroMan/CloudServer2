package NIO;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

public class NIOServer implements Runnable {
    private ServerSocketChannel server;
    private Selector selector;

    public NIOServer() throws IOException {
        server = ServerSocketChannel.open();
        server.socket().bind(new InetSocketAddress(8189));
        server.configureBlocking(false);
        selector = Selector.open();
        server.register(selector, SelectionKey.OP_ACCEPT);
    }

    @Override
    public void run() {
        try {
            System.out.println("server started");
            while (server.isOpen()) {
                selector.select();
                Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
                while (iterator.hasNext()) {
                    SelectionKey key = iterator.next();
                    iterator.remove();
                    if (key.isAcceptable()) {
                        System.out.println("client accepted");
                        SocketChannel channel = ((ServerSocketChannel) key.channel()).accept();
                        channel.configureBlocking(false);
                        channel.register(selector, SelectionKey.OP_READ);
                        channel.write(ByteBuffer.wrap("Hello!".getBytes()));
                    }
                    if (key.isReadable()) {
                        // TODO: 7/23/2020 fileStorage handle
                        System.out.println("read key");
                        ByteBuffer buffer = ByteBuffer.allocate(80);
                        int count = ((SocketChannel)key.channel()).read(buffer);
                        if (count == -1) {
                            key.channel().close();
                            break;
                        }
                        buffer.flip();
                        StringBuilder s = new StringBuilder();
                        while (buffer.hasRemaining()) {
                            s.append((char)buffer.get());
                        }
                        for (SelectionKey key1 : selector.keys()) {
                            if (key1.channel() instanceof SocketChannel && key1.isReadable()) {
                                ((SocketChannel) key1.channel()).write(ByteBuffer.wrap(s.toString().getBytes()));
                            }
                        }
                        System.out.println();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws IOException {
        new Thread(new NIOServer()).start();
    }
}
