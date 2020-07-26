package IO;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {

    DataInputStream is;
    DataOutputStream os;
    ServerSocket server;

    public Server() throws IOException {
        server = new ServerSocket(8189);
        Socket socket = server.accept();
        System.out.println("Client accepted!");
        is = new DataInputStream(socket.getInputStream());
        os = new DataOutputStream(socket.getOutputStream());
        String fileName = is.readUTF();
        System.out.println("fileName: " + fileName);
        File file = new File("./common/server/" + fileName);
        file.createNewFile();
        try (FileOutputStream os = new FileOutputStream(file)) {
            byte[] buffer = new byte[8192];
            while (true) {
                int r = is.read(buffer);
                if (r == -1) break;
                os.write(buffer, 0, r);
            }
        }
        System.out.println("File uploaded!");
    }

    public static void main(String[] args) throws IOException {
        new Server();
    }
}
