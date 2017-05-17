package com.javarush.task.task30.task3008.client;

import com.javarush.task.task30.task3008.Connection;
import com.javarush.task.task30.task3008.ConsoleHelper;
import com.javarush.task.task30.task3008.Message;
import com.javarush.task.task30.task3008.MessageType;

import java.io.IOException;
import java.net.Socket;

public class Client {
    protected Connection connection;
    private volatile boolean clientConnected = false;

    public static void main(String[] args) {
        Client client = new Client();
        client.run();
    }

    public void run() {
        SocketThread socketThread = getSocketThread();
        socketThread.setDaemon(true);
        socketThread.start();

        try {
            synchronized (this) {
                this.wait();
            }
        } catch (InterruptedException e) {
            ConsoleHelper.writeMessage("Ошибка");
            return;
        }
        if (clientConnected) {
            ConsoleHelper.writeMessage("Соединение установлено. Для выхода наберите команду 'exit'.");
        } else
            ConsoleHelper.writeMessage("Произошла ошибка во время работы клиента.");
        while (clientConnected) {
            String message;
            if (!(message = ConsoleHelper.readString()).equals("exit")) {
                if (shouldSendTextFromConsole()) {
                    sendTextMessage(message);
                }
            } else return;
        }
    }

    protected String getServerAddress() {
        ConsoleHelper.writeMessage("Введите адрес сервера: ");
        return ConsoleHelper.readString();

    }

    protected int getServerPort() {
        ConsoleHelper.writeMessage("Введите порт ввода сервера: ");
        return ConsoleHelper.readInt();
    }

    protected String getUserName() {
        ConsoleHelper.writeMessage("Введите имя пользователя: ");
        return ConsoleHelper.readString();
    }

    protected boolean shouldSendTextFromConsole() {
        return true;
    }

    protected SocketThread getSocketThread() {
        SocketThread socketThread = new SocketThread();
        return socketThread;
    }

    protected void sendTextMessage(String text) {
        try {
            connection.send(new Message(MessageType.TEXT, text));
        } catch (IOException e) {
            ConsoleHelper.writeMessage("Ошибка отправки!");
            clientConnected = false;
        }
    }


    public class SocketThread extends Thread {
        protected void processIncomingMessage(String message) {
            System.out.println(message);
        }

        protected void informAboutAddingNewUser(String userName) {
            System.out.println("Участник с именем: " + userName + " присоединился к чату:)");
        }

        protected void informAboutDeletingNewUser(String userName) {
            System.out.println("Участник с именем: " + userName + " покинул чат:(");
        }

        protected void notifyConnectionStatusChanged(boolean clientConnected) {
            Client.this.clientConnected = clientConnected;
            synchronized (Client.this) {
                Client.this.notify();
            }
        }

        protected void clientHandshake() throws IOException, ClassNotFoundException {
            while (true) {
                Message message = connection.receive();
                if(message.getType()==MessageType.NAME_REQUEST){
                    String userName = getUserName();
                    connection.send(new Message(MessageType.USER_NAME, userName));

                }
                else if(message.getType()==MessageType.NAME_ACCEPTED){
                    notifyConnectionStatusChanged(true);
                    return;
                }
                else
                    throw new IOException("Unexpected MessageType");
            }
        }

        protected void clientMainLoop()throws IOException, ClassNotFoundException {
            while (true) {
                Message message = connection.receive();
                if(message.getType()==MessageType.TEXT){
                    processIncomingMessage(message.getData());
                }
                else if(message.getType()==MessageType.USER_ADDED){
                    informAboutAddingNewUser(message.getData());
                } else if (message.getType() == MessageType.USER_REMOVED) {
                    informAboutDeletingNewUser(message.getData());
                }
                else {
                    throw new IOException("Unexpected MessageType");
                }
            }

        }

        @Override
        public void run() {
           try{
               Socket socket = new Socket(getServerAddress(),getServerPort());
               Client.this.connection = new Connection(socket);
               clientHandshake();
               clientMainLoop();

           }
           catch (IOException | ClassNotFoundException e){
               notifyConnectionStatusChanged(false);
           }
        }
    }
}
