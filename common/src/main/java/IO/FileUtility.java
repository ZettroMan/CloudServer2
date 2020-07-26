package IO;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class FileUtility {

    public static void createFile(String fileName) throws IOException {
        File file = new File(fileName);
        if (!file.exists()) {
            file.createNewFile();
        }
    }

    public static void createDirectory(String dirName) throws IOException {
        File file = new File(dirName);
        if (!file.exists()) {
            file.mkdir();
        }
    }


    public static void move(File dir, File file) throws IOException {
        String path = dir.getAbsolutePath() + "/" + file.getName();
        createFile(path);
        InputStream is = new FileInputStream(file);
        try(OutputStream os = new FileOutputStream(new File(path))) {
            byte [] buffer = new byte[8192];
            while (is.available() > 0) {
                int readBytes = is.read(buffer);
                System.out.println(readBytes);
                os.write(buffer, 0, readBytes);
            }
        }
    }

    public static void sendFile(Socket socket, File file) throws IOException {
        InputStream is = new FileInputStream(file);
        long size = file.length();
        int count = (int) (size / 8192) / 10, readBuckets = 0;
        // /==========/
        try(DataOutputStream os = new DataOutputStream(socket.getOutputStream())) {
            byte [] buffer = new byte[8192];
            os.writeUTF(file.getName());
            System.out.print("/");
            while (is.available() > 0) {
                int readBytes = is.read(buffer);
                readBuckets++;
                if (readBuckets % count == 0) {
                    System.out.print("=");
                }
                os.write(buffer, 0, readBytes);
            }
            System.out.println("/");
        }
    }


    public static void main(String[] args) throws IOException {
        // createFile("./common/1.txt");
        // createDirectory("./common/dir1");
//        long start = System.currentTimeMillis();
//        move(new File("./common/dir1"), new File("./common/1.txt"));
//        long end = System.currentTimeMillis();
//        System.out.println("time: " + (end - start) + " ms.");
        sendFile(new Socket("localhost", 8189),
                new File("./common/RidOf.mp4"));
    }

}
