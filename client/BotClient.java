package com.javarush.task.task30.task3008.client;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class BotClient extends Client {
    public static void main(String[] args) {
        BotClient client = new BotClient();
        client.run();
    }
    @Override
    protected String getUserName() {
        return "date_bot_"+(int)(100*Math.random());
    }

    @Override
    protected boolean shouldSendTextFromConsole() {
        return false;
    }

    @Override
    protected SocketThread getSocketThread() {
        BotSocketThread bst = new BotSocketThread();
        return bst;

    }

    public  class BotSocketThread extends SocketThread{
        @Override
        protected void clientMainLoop() throws IOException, ClassNotFoundException {
            sendTextMessage("Привет чатику. Я бот. Понимаю команды: дата, день, месяц, год, время, час, минуты, секунды.");
            super.clientMainLoop();
        }

        @Override
        protected void processIncomingMessage(String message) {
            System.out.println(message);
            String senderName = "";
            String senderMessageText;
            if(message.contains(": ")){
                senderName = message.substring(0,message.indexOf(": "));
                senderMessageText = message.substring(message.indexOf(":" )+ 2);
            }
            else
                senderMessageText = message;
            SimpleDateFormat  format = null;
            if("дата".equalsIgnoreCase(senderMessageText)){
                format = new SimpleDateFormat("d.MM.YYYY");
            }
            else if("день".equalsIgnoreCase(senderMessageText)){
                format = new SimpleDateFormat("d");
            }
            else if("месяц".equalsIgnoreCase(senderMessageText)){
                format = new SimpleDateFormat("MMMM");
            }
            else if("год".equalsIgnoreCase(senderMessageText)){
                format = new SimpleDateFormat("YYYY");
            }
            else if("время".equalsIgnoreCase(senderMessageText)){
                format=  new SimpleDateFormat("H:mm:ss");
            }
            else if("час".equalsIgnoreCase(senderMessageText)){
                format = new SimpleDateFormat("H");
            }
            else if("минуты".equalsIgnoreCase(senderMessageText)){
                format = new SimpleDateFormat("m");
            }
            else if("секунды".equalsIgnoreCase(senderMessageText)){
                format = new SimpleDateFormat("s");
            }
            if(format!=null){
                sendTextMessage("Информация для " + senderName + ": " + format.format(Calendar.getInstance().getTime()));
            }
        }
    }
}
