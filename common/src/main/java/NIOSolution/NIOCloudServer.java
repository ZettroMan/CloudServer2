package NIOSolution;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.*;
import java.nio.charset.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;

public class NIOCloudServer implements Runnable {
    private ServerSocketChannel server;
    private Selector selector;
    ByteBuffer commandBuffer;
    ByteBuffer tempBuffer;
    CharBuffer filenameBuffer;
    String filename;
    private final String serverFilesPath = "./common/src/main/resources/serverFiles";
    private final Charset ChUTF8 = StandardCharsets.UTF_8;
    private final CharsetDecoder decoder = ChUTF8.newDecoder();
    private final CharsetEncoder encoder = ChUTF8.newEncoder();

    public NIOCloudServer() throws IOException {
        server = ServerSocketChannel.open();
        server.socket().bind(new InetSocketAddress(8189));
        server.configureBlocking(false);
        selector = Selector.open();
        server.register(selector, SelectionKey.OP_ACCEPT);
        commandBuffer = ByteBuffer.allocate(4);
        filenameBuffer = CharBuffer.allocate(1024);
    }

    @Override
    public void run() {
        byte[] responseOK = { 0 };
        byte[] responseError = { 1 };
        byte[] responseFileNotFound = { 2 };
        byte[] responseDecodingError = { 3 };
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
                        //channel.write(ByteBuffer.wrap("Hello!".getBytes()));
                    }
                    if (key.isReadable()) {
                        SocketChannel channel = (SocketChannel) key.channel();
                        // TODO: 7/23/2020 fileStorage handle
                        System.out.println("read key");
                        int bytesRead = 0;
                        int operationID;
                        try {
                            commandBuffer.clear();
                            while (bytesRead < 4) bytesRead += channel.read(commandBuffer);
                            commandBuffer.flip();
                            operationID = commandBuffer.getInt();
                            System.out.println("OperationID = " + operationID);
                            switch (operationID) {
                                case 0:  //download to client
                                    System.out.println("Received command to download");
                                    bytesRead = 0;
                                    commandBuffer.clear();
                                    while (bytesRead < 4) bytesRead += channel.read(commandBuffer);
                                    commandBuffer.flip();
                                    commandBuffer.rewind();
                                    int filenameLength = commandBuffer.getInt();
                                    System.out.println("Filename length is " + filenameLength);
                                    tempBuffer = ByteBuffer.allocate(filenameLength + 10);// с небольшим запасом
                                    bytesRead = channel.read(tempBuffer);
                                    if (bytesRead != filenameLength) {
                                        System.out.println("Error receiving file name");
                                        break;
                                    }
                                    tempBuffer.flip();
                                    filenameBuffer.clear();
                                    CoderResult coderResult = decoder.decode(tempBuffer, filenameBuffer, true);
                                    if (!coderResult.isError()) {
                                        filenameBuffer.flip();
                                        filename = filenameBuffer.toString();
                                        System.out.println("Filename is : " + filename);
                                        Path filepath = Paths.get(serverFilesPath, filename);
                                        if(!Files.exists(filepath) || Files.isDirectory(filepath)) {
                                            channel.write(ByteBuffer.wrap(responseFileNotFound));
                                            break;
                                        }
                                        channel.write(ByteBuffer.wrap(responseOK));
                                        // here we should to transfer the file
                                        // first - send the file length
                                        long fileSize = Files.size(filepath);
                                        tempBuffer = ByteBuffer.allocate(8);
                                        tempBuffer.putLong(fileSize);
                                        tempBuffer.flip();
                                        channel.write(tempBuffer);
                                        tempBuffer = ByteBuffer.allocate(1024*1024);
                                        // next - write all the file to channel
                                        try(RandomAccessFile raf = new RandomAccessFile(filepath.toString(),"r");
                                            FileChannel inChannel = raf.getChannel()) {
                                           // inChannel.transferTo(0, fileSize, channel);
                                            while( inChannel.read(tempBuffer) != -1 ) {
                                                tempBuffer.flip();
                                                channel.write(tempBuffer);
                                                tempBuffer.clear();
                                            }
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                    } else {
                                        channel.write(ByteBuffer.wrap(responseDecodingError));
                                    }
                                    break;
                                case 1: //upload from client
                                    System.out.println("Received command to upload");
                                    break;
                                case 999:
                                    System.out.println("Received just a message:");
                                    commandBuffer.clear();
                                    bytesRead = channel.read(commandBuffer);
                                    while (bytesRead > 0) {
                                        commandBuffer.flip();
                                        while (commandBuffer.hasRemaining()) {
                                            System.out.print((char) commandBuffer.get());
                                        }
                                        commandBuffer.clear();
                                        bytesRead = channel.read(commandBuffer);
                                    }
                                    break;
                                default:
                                    System.out.println("Command received is not supported yet");
                                    break;
                            }
                        } catch (IOException e) {
                            key.channel().close();
                            System.out.println("Client disconnected");
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws IOException {
        new Thread(new NIOCloudServer()).start();
    }
}
