package veo.client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class Network  {
    private static Socket socket;
    private static DataInputStream in;
    private static DataOutputStream out;

    public static Callback callOnMsgReceived;
    public static Callback callOnAuthentidicated;
    public static Callback callOnException;
    public static Callback callOnCloseConnection;

    public static void sendAuth(String login, String password) {
        try {
            if (socket == null || socket.isClosed()) connect();
            out.writeUTF("/auth " + login + " " + password);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void connect() {
        try {
            socket = new Socket("localhost", 8189);
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());
            Thread t = new Thread(() -> {
                try {
                    while (true) {
                        String msg = in.readUTF();
                        if (msg.startsWith("/authok ")) {
                            callOnAuthentidicated.collback(msg.split("\\s")[1]);
                            break;
                        }
                    }
                    while (true) {
                        String msg = in.readUTF();
                        callOnMsgReceived.collback(msg);
                    }
                } catch (IOException e) {
                    callOnException.collback("Соединение с сервером разорвано");
                } finally {
                    closeConnection();
                }
            });
            t.setDaemon(true);
            t.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static boolean sendMsg(String msg) {
        try {
            out.writeUTF(msg);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static void closeConnection () {
        callOnCloseConnection.collback();
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
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
