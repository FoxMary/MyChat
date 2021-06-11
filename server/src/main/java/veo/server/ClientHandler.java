package veo.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ClientHandler {
    private String nickname;
    private Server server;
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;

    public String getNickname() {
        return nickname;
    }

    public ClientHandler(Server server, Socket socket) {
        try {
            this.server = server;
            this.socket = socket;
            this.in = new DataInputStream(socket.getInputStream());                                 //считывает, что приходит с сокета со входящего потока
            this.out = new DataOutputStream(socket.getOutputStream());
            new Thread(() -> {
                try {
                    while (true) {
                        String msg = in.readUTF();                                                                        //ждет пока клиент пришлет сообщение (nextLine() - считывание строчки текста
                        if (msg.startsWith("/auth ")) {                                                                              //если клиент прислал команду /end  - завершаем цикл прослушивания
                       //  /auth login pass - разбиение по пробелу
                        String[] tokens = msg.split("\\s");
                        String nick = server.getAuthService().getNicknameByLoginAndPassword(tokens[1], tokens[2]);
                        if (nick != null && !server.isNickBusy(nick)) {
                            sendMsg("/authok " + nick);
                            nickname = nick;
                            server.subscribe(this);
                            break;
                        }
                        }
                    }
                    while (true) {
                        String msg = in.readUTF();                                                                        //ждет пока клиент пришлет сообщение (nextLine() - считывание строчки текста
                        if (msg.startsWith("/")) {
                            if (msg.equals("/end")) {                                                                              //если клиент прислал команду /end  - завершаем цикл прослушивания
                                sendMsg("/end");
                                break;
                            }
                            if (msg.startsWith("/w ")) {
                                String[] tokens = msg.split("\\s", 3);
                                server.privateMsg(this, tokens[1], tokens[2]);
                            }
                        } else {
                            server.broadcastMsg(nickname + ": " + msg);
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                  disconnect();
                }
            }).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void sendMsg(String msg) {
        try {
            out.writeUTF(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void disconnect() {
        server.unsubscribe(this);
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
