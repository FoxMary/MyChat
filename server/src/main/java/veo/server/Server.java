package veo.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Vector;

public class Server {
    private Vector<ClientHandler> clients;              //Vector синхронизированная коллекция
    private AuthService authService;

    public Server() {
        clients = new Vector<>();
        authService = new SimpleAuthService();
        try (ServerSocket serverSocket = new ServerSocket(8189)) {         //запуск сервера, который работает на порту 8189
            System.out.println("Сервер запущен на порту 8189");
            while (true) {
                Socket socket = serverSocket.accept();                                                     //точка соединения, сервер ждет пока кто то подключится, когда кто то подключился создается socket
              new ClientHandler(this, socket);
                System.out.println("Подключился новый пользователь");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Сервер завершил свою работу");
    }

    public void broadcastMsg(String msg) {                  //широковещательная рассылка
        for(ClientHandler o : clients) {
            o.sendMsg(msg);
        }
    }

    public void privateMsg (ClientHandler sender, String receiverNick, String msg) {
        if (sender.getNickname().equals(receiverNick)) {
            sender.sendMsg("Заметка: " + msg);
            return;
        }
        for (ClientHandler o : clients) {
            if (o.getNickname().equals(receiverNick)) {
                o.sendMsg("От " + sender.getNickname() + ": " + msg);
                sender.sendMsg("Для " + receiverNick + ": " + msg);
                return;
            }
        }
        sender.sendMsg("Пользователь " + receiverNick + " не найден.");
    }

    public void subscribe(ClientHandler clientHandler) {
        clients.add(clientHandler);
        broadcastClientsList();
    }

    public void unsubscribe(ClientHandler clientHandler) {
        clients.remove(clientHandler);
        broadcastClientsList();
    }

    public AuthService getAuthService() {
        return authService;
    }

    public boolean isNickBusy(String nickname) {
        for(ClientHandler o : clients) {
            if (o.getNickname().equals(nickname)) {
                return true;
            }
        }
        return false;
    }

    public void broadcastClientsList() {
        StringBuilder sb = new StringBuilder(15 * clients.size());
            sb.append("/clients ");
            // 'clients '
            for (ClientHandler o : clients) {
                sb.append(o.getNickname()).append(" ");
            }
            // 'clients nick1 nick2 nick3 '
        sb.setLength(sb.length() - 1);
            // 'clients nick1 nick2 nick3'
        String out = sb.toString();
        for (ClientHandler o : clients) {
            o.sendMsg(out);
        }
    }
}
