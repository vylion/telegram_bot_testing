package main;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.json.JSONArray;
import org.json.JSONObject;

import java.time.LocalDate;

/**
 * Created by vylion on 4/1/16.
 */
public class TestBot {
    private static final String TOKEN = "208948965:AAH24jpwNdkXk-y2dqIMbRcAuzrfFd4RagE";
    private static final String LOGTAG = "TESTBOT";
    private static final String USERNAME = "VylionTestBot";
    private static final String BASE_URL = "https://api.telegram.org/bot" + TOKEN;
    private int anacondaCounter;
    private boolean alive;

    private final String[] names = {
            USERNAME,
            myName(),
            "Vylion Testbot",
            "Vyl's Testbot",
            "Vyl Testbot",
            "VylTestbot",
            "Testbot de Vyl"
    };

    public TestBot() {
        anacondaCounter = 0;
    }

    //--- Http handling

    public HttpResponse<JsonNode> sendMessage(Long chatId, String text) throws UnirestException {
        System.out.println("Sending message: \n" + text + "\n");

        return Unirest.post(BASE_URL + "/sendMessage")
                .field("chat_id", chatId)
                .field("text", text)
                .asJson();
    }

    public HttpResponse<JsonNode> sendMessage(String channel, String text) throws UnirestException {
        System.out.println("Sending message to " + channel + ": \n" + text + "\n");

        return Unirest.post(BASE_URL + "/sendMessage")
                .field("chat_id", channel)
                .field("text", text)
                .asJson();
    }

    public HttpResponse<JsonNode> replyMessage(Long chatId, Integer repliedMessage, String text) throws UnirestException {
        System.out.println("Sending message: \n" + text + "\n");

        return Unirest.post(BASE_URL + "/sendMessage")
                .field("chat_id", chatId)
                .field("text", text)
                .field("reply_to_message_id", repliedMessage)
                .asJson();
    }

    public HttpResponse<JsonNode> getUpdates(Integer offset) throws UnirestException {
        return Unirest.post(BASE_URL + "/getUpdates")
                .field("offset", offset)
                .asJson();
    }

    public HttpResponse<JsonNode> getUpdates(Integer offset, Integer timeout) throws UnirestException {
        return Unirest.post(BASE_URL + "/getUpdates")
                .field("offset", offset)
                .field("timeout", timeout)
                .asJson();
    }

    //--- bot thinking

    public void run() throws UnirestException {
        int last_upd_id = 0;
        HttpResponse<JsonNode> response;
        System.out.println("Listening.\n");

        while(true) {
            response = getUpdates(last_upd_id++);
            if (response.getStatus() == 200) {
                JSONArray responses = response.getBody().getObject().getJSONArray("result");
                if(responses.isNull(0)) continue;
                else last_upd_id = responses.getJSONObject(responses.length()-1).getInt("update_id")+1;

                for (int i = 0; i < responses.length(); i++) {
                    if(responses.getJSONObject(i).has("message")) {
                        JSONObject message = responses.getJSONObject(i).getJSONObject("message");

                        processMessage(message);
                    }
                }
            }
        }
    }

    private void processMessage(JSONObject message) throws UnirestException {
        String user = "blank_username";
        String name = "blank_name";
        String chatName = "blank_chatname";

        long chat_id = message.getJSONObject("chat").getLong("id");

        int message_id = message.getInt("message_id");

        if (message.getJSONObject("from").has("username"))
            user = message.getJSONObject("from").getString("username");

        if (message.getJSONObject("chat").has("title"))
            chatName = message.getJSONObject("chat").getString("title");
        else if (!name.equals("blank_name"))
            chatName = name;
        else if (!user.equals("blank_username"))
            chatName = user;
        else chatName = "chat id " + chat_id;

        //System.out.println("Message received from " + chatName + "\n");
        System.out.println(message + "\n");

        if (message.getJSONObject("from").has("first_name") || message.getJSONObject("from").has("last_name")) {
            if (!message.getJSONObject("from").has("first_name"))
                name = message.getJSONObject("from").getString("last_name");

            else if (!message.getJSONObject("from").has("name"))
                name = message.getJSONObject("from").getString("first_name");

            else name = message.getJSONObject("from").getString("first_name") + " " +
                        message.getJSONObject("from").getString("last_name");
        }

        //IGNORE VGA
        //if(Long.toString(chat_id).equals("-1001049453258")) return;

        if (message.has("text")) {
            String text = message.getString("text");
            String reply = "";

            if (text.startsWith("/")) handleCommand(chat_id, chatName, message_id, user, name, text);
            else handleText(chat_id, message_id, user, name, text);
            return;
        } else if (message.has("new_chat_participant")) {
            handleNewParticipant(chat_id, message.getJSONObject("new_chat_participant"));
            return;
        } else if (message.has("left_chat_participant")) {
            handleExpulsion(chat_id, message.getJSONObject("left_chat_participant"));
            return;
        }
    }

    //--- processMessage auxiliar funcitons

    private void handleCommand(long chat_id, String chat_name, int message_id, String user, String name, String text) throws UnirestException {
        String command;

        //START COMMAND
        command = "/start";
        if (text.startsWith(command + "@") && !text.startsWith(command + "@" + USERNAME)) return;
        if (text.startsWith(command)) {
            sendMessage(chat_id, "Hola, soy el testbot de Vylion.");

            if((text.length() > command.length()) && (!text.startsWith(command + "@" + USERNAME))) {
                handleText(chat_id, message_id, user, name, text);
            }
            return;
        }

        //ECHO COMMAND
        command = "/echo";
        if (text.startsWith(command + "@") && !text.startsWith(command + "@" + USERNAME)) return;
        if (text.startsWith(command)) {
            if(text.startsWith(command + " " + command)) {
                sendMessage(chat_id, "Qué manía con los nested /echo");
                return;
            }

            text = text.substring(command.length());
            if(text.startsWith("@")) {
                if(text.startsWith("@" + USERNAME)) text = text.substring(("@" + USERNAME).length());
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
        if (text.startsWith(command + "@") && !text.startsWith(command + "@" + USERNAME)) return;
        if (text.startsWith(command)) {
            text = text.substring(command.length());
            if(text.startsWith("@")) {
                if(text.startsWith("@" + USERNAME)) text = text.substring(("@" + USERNAME).length());
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
        if (text.startsWith(command + "@") && !text.startsWith(command + "@" + USERNAME)) return;
        if (text.startsWith(command)) {
            text = text.substring(command.length());
            if(text.startsWith("@")) {
                if(text.startsWith("@" + USERNAME)) text = text.substring(("@" + USERNAME).length());
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
        if (text.startsWith(command + "@") && !text.startsWith(command + "@" + USERNAME)) return;
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
            return;
        }

        //BYE
        command = "/bye";
        if (text.startsWith(command + "@") && !text.startsWith(command + "@" + USERNAME)) return;
        if (text.startsWith(command)) {
            bye(chat_id);
        }

        /* NO FUNCIONA - No ese pueden enviar PMs directos a @username, sólo a @channel
        else if(text.startsWith("/send")) {
            text = text.substring("/send".length());
            if(!text.startsWith(" @")) sendMessage(chat_id, "formato incorrecto");
            else {
                text = text.substring(1);
                String[] splitText = text.split(" ", 2);

                sendMessage(splitText[0], splitText[1]);
            }
        }
        */
    }

    private void handleWords(long chat_id, int message_id, String text, boolean mention) throws UnirestException {

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

    private void handleText(long chat_id, int message_id, String user, String name, String text) throws UnirestException {
        boolean mention = iWasMentioned(text);

        if( (mention) && ( (text.toLowerCase().contains("say goodbye")) ||
                (text.toLowerCase().contains("despídete")) ||
                (text.toLowerCase().contains("despidete")) ||
                (text.toLowerCase().contains("adiós")) ||
                (text.toLowerCase().contains("adios")) ) ) {
            System.out.println("Time to say goodbye");
            bye(chat_id);
            return;
        }

        else handleWords(chat_id, message_id, text, mention);
    }

    private void handleNewParticipant(long chat_id, JSONObject participant) throws UnirestException {
        if(participant.has("username")) System.out.println("I see there's a new chat participant called " +
                participant.getString("first_name") + " of username " +
                participant.getString("username"));
        else System.out.println("I see there's a new chat participant called " +
                participant.getString("first_name") );

        if(participant.has("username") && participant.getString("username").equals(me())) {
            sendMessage(chat_id, "Encantado de conoceros, please be gentle~");
        }

        else {
            sendMessage(chat_id, "Buenas, " + participant.getString("first_name"));
        }
    }

    private void handleExpulsion(long chat_id, JSONObject participant) throws UnirestException {
        if(participant.has("username")) System.out.println("I see that " + participant.getString("first_name") +
                " of username " + participant.getString("username") +
                " has been kicked.");
        else System.out.println("I see that " + participant.getString("first_name") +
                " (of no username) has been kicked.");

        if(participant.has("username") && participant.getString("username").equals(me())) {
            complainAboutExpulsion();
        }

        else {
            sendMessage(chat_id, "Rip " + participant.getString("first_name"));
        }
    }

    private void complainAboutExpulsion() {

    }

    //--- other auxiliar functions

    private boolean iWasMentioned(String text) {
        for(int i = 0; i < names.length; i++) {
            if(text.toLowerCase().contains(names[i].toLowerCase())) {
                System.out.println("They mentioned me");
                return true;
            }
        }

        return false;
    }

    private String me() {
        return USERNAME;
    }

    private String myName() {
        return "Vylion's Testbot";
    }

    private String myToken() {
        return TOKEN;
    }

    private void bye(long chat_id) throws UnirestException {
        sendMessage(chat_id, "Smell ya later, nerds");
        return;
    }
}