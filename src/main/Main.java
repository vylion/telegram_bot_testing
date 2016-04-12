package main;

import com.mashape.unirest.http.exceptions.UnirestException;

/**
 * Created by vylion on 4/1/16.
 */
public class Main {
    public static void main(String[] args) {
        TestBot bot = new TestBot();
        System.out.println("The TestBot awakens.");

        try {
            System.out.println("Bot running.");
            bot.run();
        } catch (UnirestException e) {
            System.out.println("Error catched.");
            e.printStackTrace();
        }
    }
}
