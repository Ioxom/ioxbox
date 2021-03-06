package io.ioxcorp.ioxbox;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import io.ioxcorp.ioxbox.data.config.Config;
import io.ioxcorp.ioxbox.data.format.Box;
import io.ioxcorp.ioxbox.data.json.JacksonYeehawHelper;
import io.ioxcorp.ioxbox.frame.IoxboxFrame;
import io.ioxcorp.ioxbox.frame.logging.LogType;
import io.ioxcorp.ioxbox.listeners.MainListener;
import io.ioxcorp.ioxbox.listeners.confirmation.ConfirmationGetter;
import io.ioxcorp.ioxbox.listeners.confirmation.ConfirmationGetterListener;
import io.ioxcorp.ioxbox.listeners.status.StatusSetter;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import javax.security.auth.login.LoginException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Properties;
import java.util.Random;

/**
 * main class for logging in and creating all our objects
 * @author ioxom
 */
public final class Main extends ListenerAdapter {
    private Main() {

    }

    public static final Random RANDOM = new Random();
    public static final ObjectMapper MAPPER = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);

    //get the version from a .properties that's saved in the .jar that shadowJar produces
    private static String version;
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

            version = properties.getProperty("version");
            properties.clear();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String getVersion() {
        return Main.version;
    }

    public static final IoxboxFrame FRAME = new IoxboxFrame();
    static {
        FRAME.init();
    }

    //get saved box data
    public static final HashMap<Long, Box> BOXES = JacksonYeehawHelper.read();

    private static JDA api;
    private static final Config CONFIG = new Config();
    private static boolean fullyConnected;
    private static boolean firstConnection = true;

    public static void main(final String[] args) {
        CONFIG.readConfig();

        //throw error if version is not found
        if (version == null) {
            version = "0.0.0";
            FRAME.log(LogType.ERR, "could not get version from \"ioxbox.properties\". this file should normally be stored in the .jar file that you ran, but it seems an error occurred on file creation. try reporting a bug on our bug tracker!");
        }

        //log in
        connect();
        FRAME.log(LogType.INIT, "initialized jda");
    }

    /**
     * simple method for shutting down {@link Main#api JDA}, then
     */
    public static void reloadJDA() {
        shutdown();
        connect();
        FRAME.log(LogType.INIT, "reconnected jda");
    }

    /**
     * disconnect from discord. this should always be used in place of {@link JDA#shutdown()}
     */
    public static void shutdown() {
        fullyConnected = false;
        ConfirmationGetter.shutdown();
        api.shutdown();
        FRAME.setReloadJDAImage("images/lightning_bolt.png");
        FRAME.log(LogType.MAIN, "shut down JDA, disconnected from discord");
    }

    /**
     * connect the {@link Main#api api} to discord using {@link Main#CONFIG Main's config's} token, then add listeners
     */
    public static void connect() {
        try {
            final String token = CONFIG.getToken();
            api = JDABuilder.createDefault(token).build();
            Main.FRAME.log(firstConnection ? LogType.INIT : LogType.MAIN, "successfully logged in JDA");
        } catch (LoginException e) {
            FRAME.log(LogType.ERR, "invalid token; enter a valid one and run /configure to log in");
        }
        addListeners();
        if (firstConnection) {
            firstConnection = false;
        }
    }

    public static void setApi(final JDABuilder builder) throws LoginException {
        api = builder.build();
    }

    /**
     * should be used instead of {@link System#exit(int)}
     * @param code the code to exit with
     */
    public static void exit(final int code) {
        shutdown();
        System.exit(code);
    }

    /**
     * add the four main listeners for ioxbox: {@link MainListener}, {@link ConfirmationGetterListener}, {@link StatusSetter} and {@link Main}<br>
     * events added:<br>
     * {@link MainListener}: {@link ListenerAdapter#onMessageReceived(net.dv8tion.jda.api.events.message.MessageReceivedEvent) ListenerAdapter.onMessageReceived(MessageReceivedEvent)} - handles all commands sent to the bot<br>
     * {@link ConfirmationGetterListener}: {@link ListenerAdapter#onMessageReceived(net.dv8tion.jda.api.events.message.MessageReceivedEvent) ListenerAdapter.onMessageReceived(MessageReceivedEvent)} - handles requests for confirmation; detects whether the requested user has said yes or not<br>
     * {@link StatusSetter}: {@link ListenerAdapter#onReady(ReadyEvent)} - begins a {@link io.ioxcorp.ioxbox.listeners.status.StatusRunnable StatusRunnable} that cycles ioxbox's status<br>
     * {@link Main}: {@link ListenerAdapter#onReady(ReadyEvent)} - tells ioxbox whether JDA is fully connected or not, shows whether it is with {@link IoxboxFrame#reloadJDA reloadJDA's} icon, and logs the connection<br>
     */
    public static void addListeners() {
        api.addEventListener(new MainListener(), new ConfirmationGetterListener(), new StatusSetter(20), new Main());
    }

    @Override
    public void onReady(final @NotNull ReadyEvent e) {
        FRAME.log(LogType.MAIN, "fully loaded JDA; connected to discord");
        fullyConnected = true;
        FRAME.setReloadJDAImage("images/lightning_bolt_white.png");
    }

    public static JDA getApi() {
        return api;
    }

    public static boolean isFullyConnected() {
        return fullyConnected;
    }

    public static Config getConfig() {
        return CONFIG;
    }
}
