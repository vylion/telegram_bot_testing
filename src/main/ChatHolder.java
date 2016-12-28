package main;

import org.telegram.telegrambots.api.objects.Chat;
import org.telegram.telegrambots.api.objects.Message;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.StringJoiner;

/**
 * Created by Guillermo Serrahima on 12/28/16.
 */
public class ChatHolder {
    private Chat chat;
    private List<List<String>> history;
    private String title;

    public ChatHolder(Chat chat, List<List<String>> history) {
        this.chat = chat;
        this.history = history;

        if(chat.isUserChat()) title = chat.getFirstName();
        else title = chat.getTitle();
    }

    public ChatHolder(Chat chat) {
        this(chat, new ArrayList<List<String>>());
    }

    public void read(Message m) {
        Date d = new Date((new Long(m.getDate()))*1000);

        ArrayList<String> entry = new ArrayList<>();
        String line = "[" + d.toInstant() + "]";

        if(!m.isUserMessage()) {
            line += " " + m.getFrom().getFirstName() + ":";
        }

        entry.add(line);

        if(m.hasPhoto()) {
            line = "[Photo]";
            entry.add(line);
        }

        if(m.hasText()) {
            line = m.getText();
        }
        else line = "[No text]";

        entry.add(line);
        entry.add(".");

        StringJoiner s = new StringJoiner("\n", "", "\n");
        for(int i = 0; i < entry.size(); ++i) s.add(entry.get(i));
        Persistence.println(s.toString());

        history.add(entry);
    }

    public Long getId() {
        return chat.getId();
    }

    public List<String> getInfo() {
        ArrayList<String> lines = new ArrayList<>();
        lines.add(title);

        return lines;
    }

    public List<String> getHistory() {
        ArrayList<String> lines = new ArrayList<>();

        for(int i = 0; i < history.size(); ++i) {
            lines.addAll(history.get(i));
        }

        return lines;
    }

    public void emptyHistory() {
        history = new ArrayList<>();
    }
}
