package io.ioxcorp.ioxbox;

import io.ioxcorp.ioxbox.data.format.Box;
import io.ioxcorp.ioxbox.data.json.JacksonYeehawHelper;
import io.ioxcorp.ioxbox.listeners.Listener;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;

import javax.security.auth.login.LoginException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Properties;

import static io.ioxcorp.ioxbox.Frame.LogType;

public class Main {

    //get the version from a .properties that's saved in the .jar that gradle produces
    public static String VERSION;
    static {
        try {
            Properties properties = new Properties();
            String fileName = "ioxbox.properties";
            InputStream inputStream = Main.class.getClassLoader().getResourceAsStream(fileName);

            //load input stream
            if (inputStream != null) {
                properties.load(inputStream);
            } else {
                throw new FileNotFoundException("property file \"" + fileName + "\" not found in the classpath");
            }

            VERSION = properties.getProperty("version");
            properties.clear();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Frame frame;
    static {
        //create frame
        frame = new Frame();
        frame.init();
    }

    public static HashMap<Long, Box> boxes;
    static {
        //read saved box data
        boxes = JacksonYeehawHelper.read();
    }

    public static void main(String[] args) {

        //throw error if version is not found
        if (VERSION == null) {
            VERSION = "0.0.0";
            frame.log(Frame.LogType.ERROR, "could not get version from \"ioxbox.properties\". this file should normally be stored in the .jar file that is run, but it seems an error occurred on saving.");
        }

        //log in
        JDA api = null;
        try {
            String token = null;
            try {
                token = Files.readString(Paths.get("token.txt"));
                if (token == null) frame.log(LogType.FATAL_ERROR, "could not get token");
            } catch (IOException e) {
                frame.log(LogType.FATAL_ERROR, "token.txt not found");
            }
            api = JDABuilder.createDefault(token).build();
            Main.frame.log(LogType.INIT, "successfully logged in JDA");
        } catch (LoginException e) {
            frame.log(LogType.FATAL_ERROR, "invalid token");
        }

        //add event listeners
        if (api != null) {
            api.addEventListener(new Listener());
            frame.log(LogType.INIT, "initialized jda");
        } else {
            frame.log(LogType.ERROR, "failed to create JDA object for unknown reasons");
        }
    }
}