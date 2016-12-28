package main;

import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.TelegramBotsApi;
import org.telegram.telegrambots.exceptions.TelegramApiException;
import org.telegram.telegrambots.exceptions.TelegramApiRequestException;

/**
 * Created by Guillermo Serrahima on 12/27/16.
 */
public class Main {

    public static void main(String[] args) {
        ApiContextInitializer.init();

        TelegramBotsApi botsApi = new TelegramBotsApi();

        try {
            botsApi.registerBot(new VylionBot());
        } catch (TelegramApiRequestException e1) {
            e1.printStackTrace();
        }
    }
}
