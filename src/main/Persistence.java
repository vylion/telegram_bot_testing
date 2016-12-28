package main;

import org.telegram.telegrambots.api.objects.Chat;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by Guillermo Serrahima on 4/14/16.
 */
public class Persistence {
    private static Persistence persistence;
    private static Path log = Paths.get("vylbotFiles/log.txt");

    private Persistence() {

    }

    public static Persistence getInstance() {
        if(persistence == null) persistence = new Persistence();
        return persistence;
    }

    private void saveChatInfo(ChatHolder c) {
        Path file;

        try {
            file = Paths.get("vylbotFiles/" + c.getId() + "/chat_info.txt");
            if (!Files.exists(file.getParent()))
                Files.createDirectories(file.getParent());
            Files.write(file, c.getInfo(), Charset.forName("UTF-8"));
        } catch (IOException e) {
            println("\n------------\n" +
                    "There was an error trying to SAVE CHAT INFO of " + c.getId() +
                    "\n------------\n");
        }
    }

    public void saveChat(ChatHolder c) {
        //Save chat stats
        saveChatInfo(c);

        //Save chat lists
        saveChatHistory(c);
    }

    public void saveChatHistory(ChatHolder c) {
        try {
            Path file = Paths.get("vylbotFiles/" + c.getId() + "/history.txt");
            if (!Files.exists(file.getParent()))
                Files.createDirectories(file.getParent());
            if(!Files.exists(file))
                Files.createFile(file);
            Files.write(file, c.getHistory(), Charset.forName("UTF-8"), StandardOpenOption.APPEND);
        } catch (IOException e) {
            println("\n------------\n" +
                    "There was an error trying to SAVE CHAT HISTORY of " + c.getId() +
                    "\n------------\n");
        }

        c.emptyHistory();
    }

    public static void println(String s) {
        System.out.println(s);
        List<String> lines = Arrays.asList(s.split("\n"));
        updateLog(lines);
    }

    public static void print(String s) {
        System.out.print(s);
        List<String> lines = Arrays.asList(s.split("\n"));
        updateLog(lines);
    }

    private static void updateLog(List<String> lines) {
        try {
            if (!Files.exists(log.getParent()))
                    Files.createDirectories(log.getParent());
            if(!Files.exists(log))
                Files.createFile(log);
            Files.write(log, lines, Charset.forName("UTF-8"), StandardOpenOption.APPEND);
        } catch (IOException e) {
            System.out.println("\n------------\n" +
                    "There was an error trying to UPDATE BOT LOG" +
                    "\n------------\n");
        }
    }
}
