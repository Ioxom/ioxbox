package io.ioxcorp.ioxbot;

import static io.ioxcorp.ioxbot.Main.boxes;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.awt.Color;

//unfinished, being worked on by Thonkman
public class Listener extends ListenerAdapter {
    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        final String prefix = "-box ";
        final String messageContentRaw = event.getMessage().getContentRaw().toLowerCase();

        if (!messageContentRaw.startsWith(prefix)) return;
        String[] message = messageContentRaw.split(prefix)[1].split(" ");
        CustomUser author = new CustomUser(event.getAuthor());

        switch (message[0]) {
            case "yes":
                event.getChannel().sendMessage("Box is here :package: ").queue();
                break;
            case "help":
                EmbedBuilder helpEmbed = new EmbedBuilder()
                        .setAuthor("The Box People :tm:")
                        .setColor(new Color(0xfc03df))
                        .addField("What in the heck does this bot do?", "This bot is very hot, it stores cool things for you. Like words, words and more words for now.", false)
                        .addField("Commands", "Prefix is -box \n Commands are the following; yes, add", false)
                        .addField("The IoxCorp Incorporated", "IoxCorp Incorporated was founded in 04/01/20 by the \"Ioxom Foundation\", and is actively being sponsored by \" The Birch Tree\", It is also funded by \"Thonkman\"\nIf you would like to learn more, please use the command -ioxcorp (nonexistant atm)", false)
                        .setFooter("Powered by electricity *and ioxcorp:tm:*");
                event.getChannel().sendMessage(helpEmbed.build()).queue();
                break;
            case "add":
                boxes.put(author.id, new Box(author, message[1]));
                event.getChannel().sendMessage(boxes.get(author.id).embed()).queue();
                break;
        }
    }
}