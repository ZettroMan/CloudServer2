import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;

import java.io.*;
import java.net.Socket;
import java.net.URL;
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
        File dir = new File(clientFilesPath);
        for (String file : dir.list()) {
            lv.getItems().add(file);
        }
    }

    // ./download fileName
    // ./upload fileName
    public void sendCommand(ActionEvent actionEvent) {
        String command = txt.getText();
        String [] op = command.split(" ");
        if (op[0].equals("./download")) {
            try {
                os.writeUTF(op[0]);
                os.writeUTF(op[1]);
                String response = is.readUTF();
                System.out.println("resp: " + response);
                if (response.equals("OK")) {
                    File file = new File(clientFilesPath + "/" + op[1]);
                    if (!file.exists()) {
                        file.createNewFile();
                    }
                    long len = is.readLong();
                    byte [] buffer = new byte[1024];
                    try(FileOutputStream fos = new FileOutputStream(file)) {
                        if (len < 1024) {
                            int count = is.read(buffer);
                            fos.write(buffer, 0, count);
                        } else {
                            for (long i = 0; i < len / 1024; i++) {
                                int count = is.read(buffer);
                                fos.write(buffer, 0, count);
                            }
                        }
                    }
                    lv.getItems().add(op[1]);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            // TODO: 7/23/2020 upload
        }
    }
}
