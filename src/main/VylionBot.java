package main;

import org.telegram.telegrambots.api.methods.ForwardMessage;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.Message;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.exceptions.TelegramApiException;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Guillermo Serrahima on 12/27/16.
 */
public class VylionBot extends TelegramLongPollingBot {

    private int anacondaCounter;
    private Map<Long, ChatHolder> chats;
    private Persistence p;

    private final String[] names = {
            getBotUsername(),
            "Vylion's bot",
            "Vylion bot",
            "Vyl's bot",
            "Vyl bot",
            "Vylbot",
            "Bot de Vyl"
    };

    public VylionBot() {
        anacondaCounter = 0;
        chats = new HashMap<Long, ChatHolder>();
        p = Persistence.getInstance();

        p.println("VylionBot ready.\n");
    }

    @Override
    public void onUpdateReceived(Update update) {
        if(update.hasMessage()) {
            Message m = update.getMessage();
            p.println("\n---\n\n" + m + "\n");
            m.isUserMessage();
            m.getChat();

            ChatHolder current;
            if(!exists(m.getChatId())) {
                current = new ChatHolder(m.getChat());
                current.read(m);
                chats.put(m.getChatId(), current);
            }
            else {
                current = chats.get(m.getChatId());
                current.read(m);
            }
            p.saveChat(current);

            if(m.hasText()) {
                String user = "";
                String name = m.getFrom().getFirstName();
                String chatName;

                if(m.getChat().isGroupChat() ||
                        m.getChat().isChannelChat() ||
                        m.getChat().isSuperGroupChat())
                    chatName = m.getChat().getTitle();
                else chatName = m.getChat().getFirstName();

                if(m.getText().startsWith("/")) handleCommand(m.getChatId(), chatName, m.getMessageId(), user, name, m.getText());
                handleText(m.getChatId(), m.getMessageId(), user, name, m.getText());
            }

            if(m.hasPhoto()) {
                if(m.isUserMessage()) {
                    forwardMessage(m.getChatId(), (long) 8379173, m.getMessageId());
                }
            }
        }
    }

    @Override
    public String getBotUsername() {
        return "VylionBot";
    }

    @Override
    public String getBotToken() {
        return "165232232:AAGVoVm1AA_cP2RNGh3sR4nPX9hQvujr_ls";
    }

    private boolean exists(Long id) {
        return chats.containsKey(id);
    }

    private void sendMessage(Long chatId, String text) {
        SendMessage m = new SendMessage()
                .setChatId(chatId)
                .setText(text);

        try {
            sendMessage(m);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void replyMessage(Long chatId, Integer mId, String text) {
        SendMessage m = new SendMessage()
                .setChatId(chatId)
                .setReplyToMessageId(mId)
                .setText(text);

        try {
            sendMessage(m);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void forwardMessage(Long fromChat, Long toChat, Integer mId) {
        ForwardMessage m = new ForwardMessage()
                .setFromChatId(fromChat.toString())
                .setChatId(toChat)
                .setMessageId(mId);
        try {
            forwardMessage(m);
        } catch(TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void handleText(long chat_id, int message_id, String user, String name, String text) {
        boolean mention = iWasMentioned(text);

        handleWords(chat_id, message_id, text, mention);
    }

    private void handleCommand(long chat_id, String chat_name, int message_id, String user, String name, String text) {
        String command;

        //START COMMAND
        command = "/start";
        if (text.startsWith(command + "@") && !text.startsWith(command + "@" + getBotUsername())) return;
        if (text.startsWith(command)) {
            sendMessage(chat_id, "Hola, soy el bot de pruebas de Vylion.");

            if((text.length() > command.length()) && (!text.startsWith(command + "@" + getBotUsername()))) {
                handleText(chat_id, message_id, user, name, text);
            }
            return;
        }

        //ECHO COMMAND
        command = "/echo";
        if (text.startsWith(command + "@") && !text.startsWith(command + "@" + getBotUsername())) return;
        if (text.startsWith(command)) {
            if(text.startsWith(command + " " + command)) {
                sendMessage(chat_id, "Qué manía con los nested /echo");
                return;
            }

            text = text.substring(command.length());
            if(text.startsWith("@")) {
                if(text.startsWith("@" + getBotUsername())) text = text.substring(("@" + getBotUsername()).length());
                else return;
            }
            text = text.trim();
            if(text.length() > 0) sendMessage(chat_id, text);
            else sendMessage(chat_id, "Formato incorrecto. El formato es\n" +
                    command + " mensaje"
            );
            return;
        }

        //ME COMMAND
        command = "/me";
        if (text.startsWith(command + "@") && !text.startsWith(command + "@" + getBotUsername())) return;
        if (text.startsWith(command)) {
            text = text.substring(command.length());
            if(text.startsWith("@")) {
                if(text.startsWith("@" + getBotUsername())) text = text.substring(("@" + getBotUsername()).length());
                else return;
            }
            if(!text.startsWith(" ")) {
                sendMessage(chat_id, "Formato incorrecto. El formato es\n" +
                        command + " mensaje"
                );
                return;
            }
            text = text.trim();
            sendMessage(chat_id, name + " " + text);
            return;
        }

        //QUOTE COMMAND
        command = "/quote";
        if (text.startsWith(command + "@") && !text.startsWith(command + "@" + getBotUsername())) return;
        if (text.startsWith(command)) {
            text = text.substring(command.length());
            if(text.startsWith("@")) {
                if(text.startsWith("@" + getBotUsername())) text = text.substring(("@" + getBotUsername()).length());
                else return;
            }
            if(!text.startsWith(" ")) {
                sendMessage(chat_id, "Formato incorrecto. El formato es\n" +
                        command + " nombre: mensaje"
                );
                return;
            }
            text = text.trim();
            String[] quote = text.split(":", 2);
            if(quote.length < 2 || !quote[1].startsWith(" ")) {
                sendMessage(chat_id, "formato incorrecto");
                return;
            }

            sendMessage(chat_id, "\"" + quote[1].substring(1) + "\" - " + quote[0]);
            return;
        }

        //HELP COMMAND
        command = "/help";
        if (text.startsWith(command + "@") && !text.startsWith(command + "@" + getBotUsername())) return;
        if (text.startsWith(command)) {
            text = text.substring(command.length());

            sendMessage(chat_id, "Soy un bot personal de Vylion " +
                    "para la experimentación con el sistema de Bots de Telegram.\n\n" +
                    "Los comandos que acepto son:\n" +
                    "/start\n" +
                    "/help\n" +
                    "/echo seguido de un mensaje\n" +
                    "/me seguido de un mensaje\n" +
                    "/quote nombre: mensaje\n" +
                    "Y también reconozco varias palabras dentro de los mensajes."
            );
        }
    }

    private void handleWords(long chat_id, int message_id, String text, boolean mention) {

        //MY ANACONDA DON'T
        if(text.toLowerCase().equals("my anaconda don't")) {
            anacondaCounter++;
            if(anacondaCounter >= 3) {
                sendMessage(chat_id, "Want none unless you've got buns, hun");
                anacondaCounter = 0;
            }
            return;
        }

        //FIRE IN THE DISCO
        if(text.toLowerCase().contains("fire in the") ||
                text.toLowerCase().contains("fuego en la")) {
            replyMessage(chat_id, message_id, "Fire in the taco bell");
            return;
        }
        if(text.toLowerCase().contains("danger danger") ||
                text.toLowerCase().contains("danger, danger")) {
            replyMessage(chat_id, message_id, "HIGH VOLTAGE");
            return;
        }
        if(text.toLowerCase().equals("when we touch")) {
            replyMessage(chat_id, message_id, "WHEN WE KISS");
            return;
        }

        boolean memes = false;

        //VAPORWAVE AESTHETICS
        if (text.toUpperCase().contains("AESTHETICS") ||
                text.toUpperCase().contains("A E S T H E T I C S") ||
                text.toUpperCase().contains("\uFEFFＡＥＳＴＨＥＴＩＣＳ")) {
            sendMessage(chat_id, "\uFEFFＶＡＰＯＲＷＡＶＥ");
            memes = true;
        }
        if (text.toUpperCase().contains("VAPORWAVE") ||
                text.toUpperCase().contains("V A P O R W A V E") ||
                text.toUpperCase().contains("\uFEFFＶＡＰＯＲＷＡＶＥ")) {
            sendMessage(chat_id, "\uFEFFＡＥＳＴＨＥＴＩＣＳ");
            memes = true;
        }

        //BIENE ALESSIO
        if (text.toLowerCase().contains("biene")) {
            sendMessage(chat_id, "Alessio");
            memes = true;
        }

        //AYY LMAO
        if(text.toLowerCase().contains("lmao")) {
            sendMessage(chat_id, "ayy");
            memes = true;
        } else if (text.toLowerCase().contains("ayy")) {
            sendMessage(chat_id, "lmao");
            memes = true;
        } else if (text.toLowerCase().contains("qyy")) {
            replyMessage(chat_id, message_id, ">failing this hard");
            memes = true;
        }

        //EXODIA
        if (text.toLowerCase().equals("exodia")) {
            sendMessage(chat_id, "ANIQUILA");
            memes = true;
        }

        //H3H3
        if(text.toLowerCase().contains("h3h3")) {
            sendMessage(chat_id, "wow ethan, great moves, keep it up! Proud of you");
            memes = true;
        }
        if(text.toLowerCase().contains("imethanbradberry")) {
            replyMessage(chat_id, message_id, "CALM DOWN *fixes hair*");
            memes = true;
        } else if(text.toLowerCase().contains("bradberry")) {
            sendMessage(chat_id, "IMETHANBRADBERRY");
            memes = true;
        }

        //REMOVE KEBAB
        if(text.toLowerCase().contains("kebab")) {
            sendMessage(chat_id, "https://www.youtube.com/watch?v=ocW3fBqPQkU");
        }

        //ME VOY DEL WHATSAPP - CIM LABS EXCLUSIVE
        if(text.toLowerCase().equals("me voy") && (Long.toString(chat_id).equals("-1001036575277"))) {
            sendMessage(chat_id, "\"Me voy del Whatsapp, adiós.\"");
            memes = true;
        }

        if(memes) return;

        if(mention) {
            replyMessage(chat_id, message_id, "Ese soy yo");
            return;
        }
    }

    private boolean iWasMentioned(String text) {
        for(int i = 0; i < names.length; i++) {
            if(text.toLowerCase().contains(names[i].toLowerCase())) {
                p.println("They mentioned me");
                return true;
            }
        }

        return false;
    }
}
