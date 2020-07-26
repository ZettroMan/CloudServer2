import com.sun.javaws.IconUtil;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;

import java.io.*;
import java.net.Socket;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.ResourceBundle;

public class Controller implements Initializable {

    public ListView<String> lv;
    public TextField txt;
    public Button send;
    private Socket socket;
    private DataInputStream is;
    private DataOutputStream os;
    private final String clientFilesPath = "./common/src/main/resources/clientFiles";

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            socket = new Socket("localhost", 8189);
            is = new DataInputStream(socket.getInputStream());
            os = new DataOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
        refreshFileList();
    }

    public void refreshFileList() {
        File dir = new File(clientFilesPath);
        lv.getItems().clear();
        for (String file : dir.list()) {
            lv.getItems().add(file);
        }
    }

    // ./download fileName
    // ./upload fileName
    public void sendCommand(ActionEvent actionEvent) {
        String command = txt.getText();
        String[] op = command.split(" ", 2);
        byte response;
        try {
            if (op[0].equals("./download")) {
                os.writeInt(0);
                os.writeInt(op[1].getBytes().length);
                os.write(op[1].getBytes());
                response = is.readByte();
                System.out.println("response: " + response);
                if (response == 0) {
                    try {
                        long fileSize = is.readLong();
                        long bytesRest = fileSize;
                        //long fileSize = 0L;
                        System.out.println("File length is : " + fileSize);
                        File file = new File(clientFilesPath + "/" + op[1]);
                        if (!file.exists()) {
                            file.createNewFile();
                        }
                        byte[] buffer = new byte[1024];
                        try (FileOutputStream fos = new FileOutputStream(file)) {
                            while (bytesRest > 0L) {
                                int count = is.read(buffer);
                                fos.write(buffer, 0, count);
                                bytesRest -= count;
                                System.out.println("Bytes rest : " + bytesRest);
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        System.out.println("Total bytes received : " + fileSize);
                        //lv.getItems().add(op[1]);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            } else if (op[0].equals("./upload")) {
                // TODO: 7/23/2020 upload
            } else {
                //os.write(1);
                // os.write(("Command: " + txt.getText()).getBytes().length);
                os.writeInt(999);
                os.write(("Command: " + txt.getText()).getBytes());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        refreshFileList();
    }
}
