package server;

import main.Connection;
import main.ConnectionListener;
import main.Const;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.Iterator;

import static main.Const.*;

/**
 * Обеспечивает работу программы в режиме сервера
 */
public class Server implements ConnectionListener {

    /**
     * Специальная "обёртка" для ArrayList, которая обеспечивает
     *  доступ к массиву из разных потоков
     */
    private final ArrayList<Connection> connections = new ArrayList<>();
    private ServerSocket server;

    /**
     * Конструктор создаёт сервер. Затем для каждого подключения создаётся
     * объект Connection и добавляет его в список подключений.
     */
    public Server() {
        System.out.println("Сервер запущен");
        try{
            server = new ServerSocket(Const.PORT);
            while(true) {
                try {
                    new Connection(this, server.accept());
                } catch (IOException e) {
                    System.out.println("Ошибка соединения: " + e);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public synchronized void onConnectionReady(Connection connection) {
        connections.add(connection);
        System.out.println("Подключение пользователя: " + connection.getLogin());
        sendMessage(ANSI_YELLOW + connection.getLogin() + " заходит в чат" + ANSI_RESET);
    }

    @Override
    public synchronized void onReceiveString(Connection connection, String message) {
        sendMessage(connection, message);
    }

    @Override
    public synchronized void onDisconnect(Connection connection) {
        connections.remove(connection);
        System.out.println("Отключение пользователя: " + connection.getLogin());
        sendMessage(ANSI_YELLOW + connection.getLogin() + " выходит из чата" + ANSI_RESET);
    }

    @Override
    public synchronized void onException(Connection connection, Exception e) {
        System.out.println("Ошибка соединения: " + e);
    }
    private synchronized void sendMessage(String message){
        Iterator<Connection> connectionIterator = connections.iterator();
        while(connectionIterator.hasNext()) {
            connectionIterator.next().sendMessage(message);
        }
    }

    private synchronized void sendMessage(Connection connection, String message){
        Iterator<Connection> connectionIterator = connections.iterator();
        while(connectionIterator.hasNext()) {
            Connection nextConnection = connectionIterator.next();
            //if(!connection.equals(nextConnection))
                nextConnection.sendMessage(ANSI_RED + connection.getLogin() + ANSI_RESET + ": " + message);
        }
    }
}