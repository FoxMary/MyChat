package veo.client;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;

import java.net.URL;
import java.util.ResourceBundle;

public class Controller implements Initializable {      //инициализация приложения
    @FXML
    TextArea textArea;

    @FXML
    TextField msgField, loginField;

    @FXML
    HBox msgPanel, authPanel;

    @FXML
    PasswordField passField;

    @FXML
    ListView<String> clientsList;

    private boolean authentificated;
    private String nickname;

    public void setAuthentificated(boolean authentificated) {
        this.authentificated = authentificated;
        this.clientsList = new ListView<>();
        authPanel.setVisible(!authentificated);             //показать\скрыть элемент управления
        authPanel.setManaged(!authentificated);         //показать\скрыть место для элемента управления
        msgPanel.setVisible(authentificated);
        msgPanel.setManaged(authentificated);
        clientsList.setVisible(authentificated);
        clientsList.setManaged(authentificated);
        if (!authentificated) nickname = " ";
        }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setAuthentificated(false);
        clientsList.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                String nickname = clientsList.getSelectionModel().getSelectedItem();        //получение содержимого строки, на который кликнули 2 раза
                msgField.setText("/w " + nickname + " ");
                msgField.requestFocus();            //при фокусе выделяется все содержимое
                msgField.selectEnd();               //курсор ставится в конец
            }
        });
        linkCallbacks();
    }

    public void sendAuth() {
        Network.sendAuth(loginField.getText(), passField.getText());
        loginField.clear();
        passField.clear();
    }

    public void sendMsg() {
        if (Network.sendMsg(msgField.getText())) {
            msgField.clear();
            msgField.requestFocus();
        }
    }

    public void showAlert(String msg) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.WARNING, msg, ButtonType.OK);
            alert.showAndWait();
        });
    }

    public void linkCallbacks() {
        Network.setCallOnException(args -> showAlert(args[0].toString()));
        Network.setCallOnCloseConnection(args -> setAuthentificated(false));
        Network.setCallOnAuthentidicated(args -> {
            setAuthentificated(true);
            nickname = args[0].toString();
        });
        Network.setCallOnMsgReceived(args -> {
            String msg = args[0].toString();
            if (msg.startsWith("/")) {
                if (msg.startsWith("/clients ")) {
                    String[] tokens = msg.split("\\s");
                    Platform.runLater(() -> {                                //прокидывает задачу из моего потока в поток javaFX
                        clientsList.getItems().clear();
                        for (int i = 1; i < tokens.length; i++) {
                            clientsList.getItems().add(tokens[i]);
                        }
                    });
                }
            } else {
                textArea.appendText(msg + "\n");
            }
        });
    }
}
