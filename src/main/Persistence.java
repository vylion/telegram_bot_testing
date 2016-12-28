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
            System.out.println("\n------------\n" +
                    "There was an error trying to SAVE chat INFO of " + c.getId() +
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
            Files.write(file, c.getHistory(), Charset.forName("UTF-8"), StandardOpenOption.APPEND);
        } catch (IOException e) {
            System.out.println("\n------------\n" +
                    "There was an error trying to SAVE chat HISTORY of " + c.getId() +
                    "\n------------\n");
        }

        c.emptyHistory();
    }
}
