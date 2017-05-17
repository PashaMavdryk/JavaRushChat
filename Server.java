package com.javarush.task.task30.task3008;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Server {
    private static Map<String, Connection> connectionMap = new ConcurrentHashMap<>();

    public static void main(String[] args) throws IOException {

        ConsoleHelper.writeMessage("Введите порт сервера: ");
        int serverPort = ConsoleHelper.readInt();

        try (ServerSocket serverSocket = new ServerSocket(serverPort)) {

            ConsoleHelper.writeMessage("Сервер запущен");

            while (true) {
                //Слушаем
                Socket socket = serverSocket.accept();
                Handler handler = new Handler(socket);
                //запускаем handler
                handler.start();
            }
        } catch (IOException e) {
            ConsoleHelper.writeMessage("Ошибка сокета");
        }

    }

    public static void sendBroadcastMessage(Message message) {
        try {
            for (Connection connection : connectionMap.values()) {
                connection.send(message);
            }
        } catch (IOException e) {
            e.printStackTrace();
            ConsoleHelper.writeMessage("Сообщение не отправлено");
        }
    }

    private static class Handler extends Thread {
        private Socket socket;

        private Handler(Socket socket) {
            this.socket = socket;
        }

        private String serverHandshake(Connection connection) throws IOException, ClassNotFoundException {


            while (true) {
                connection.send(new Message(MessageType.NAME_REQUEST));
                Message message = connection.receive();
                if (message.getType() == MessageType.USER_NAME) {
                    if (!message.getData().isEmpty()) {
                        if (!connectionMap.containsKey(message.getData())) {
                            connectionMap.put(message.getData(), connection);
                            connection.send(new Message(MessageType.NAME_ACCEPTED));
                            return message.getData();
                        }
                    }
                }
            }
        }

        private void sendListOfUsers(Connection connection, String userName)throws IOException{
            for(String key : connectionMap.keySet()){
                Message message = new Message(MessageType.USER_ADDED, key);
                if(!key.equals(userName))
                connection.send(message);
            }
        }

        private void serverMainLoop(Connection connection, String userName) throws IOException, ClassNotFoundException{
            while (true){
               Message message =  connection.receive();

               if(message.getType()==MessageType.TEXT){
                    String s = userName + ": " + message.getData();
                    Message formattedMessage = new Message(MessageType.TEXT, s);
                    sendBroadcastMessage(formattedMessage);
               }
               else {
                   ConsoleHelper.writeMessage("Error!");
               }

            }
        }

        @Override
        public void run() {

            String clientName = null;
            try(Connection connection  = new Connection(socket)){
                ConsoleHelper.writeMessage("Подключение к порту: " + connection.getRemoteSocketAddress());

                clientName = serverHandshake(connection);

                sendBroadcastMessage(new Message(MessageType.USER_ADDED, clientName));

                sendListOfUsers(connection,clientName);

                serverMainLoop(connection,clientName);
            }

            catch (IOException | ClassNotFoundException ex) {
                ConsoleHelper.writeMessage("Ошибка при обмене данными с удаленным адресом");
            }
            finally {
                ConsoleHelper.writeMessage("Установлено соединение с адресом: " + socket.getRemoteSocketAddress());
            }
            if(!connectionMap.isEmpty()){
            connectionMap.remove(clientName);}
            sendBroadcastMessage(new Message(MessageType.USER_REMOVED, clientName));

            ConsoleHelper.writeMessage("Соединение с удаленным адресом закрыто");
        }
    }
}
