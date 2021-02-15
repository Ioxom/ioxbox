package io.ioxcorp.ioxbox.listeners.confirmation;

import io.ioxcorp.ioxbox.data.format.WhatAmIDoing;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.HashMap;
import java.util.concurrent.CountDownLatch;

public class ConfirmationGetter extends ListenerAdapter {

    public final CountDownLatch latch;
    public final long id;
    public boolean response;
    public int attempts;

    public ConfirmationGetter(CountDownLatch latch, long id) {
        this.id = id;
        this.latch = latch;
        this.attempts = 0;
    }

    /**
     * convenience method to check if we're getting confirmation from a user
     * @param id the id of the user we want to check if we're getting confirmation from
     * @return whether or not we're getting confirmation from the user
     * @author ioxom
     */
    public static boolean gettingConfirmationFrom(long id) {
        return booleans.containsKey(id) || confirmationGetters.containsKey(id) || channels.containsKey(id);
    }

    public static final HashMap<Long, MessageChannel> channels = new HashMap<>();
    public static final HashMap<Long, Boolean> booleans = new HashMap<>();
    public static final HashMap<Long, ConfirmationGetter> confirmationGetters = new HashMap<>();

    /**
     * gets confirmation from a user - warning: blocks the thread it's running on until confirmation is given - proceed with caution
     * @param id the id of the user we want confirmation from
     * @return {@link WhatAmIDoing WhatAmIDoing} a {@link MessageChannel MessageChannel} and a {@link Boolean Boolean} containing the response and the channel it was sent in
     * @author ioxom
     */
    public static WhatAmIDoing crab(long id) {
        //safeguard: if we're already getting confirmation from someone we can't do multiple instances at the same time
        //ideally this has already been checked for
        if (gettingConfirmationFrom(id)) return null;

        //create a ConfirmationGetter to handle it
        CountDownLatch crabDownLatch = new CountDownLatch(1);
        confirmationGetters.put(id, new ConfirmationGetter(crabDownLatch, id));
        confirmationGetters.get(id).run();

        try {
            //ensure that we've gotten our confirmation before returning a response
            crabDownLatch.await();
            return new WhatAmIDoing(channels.get(id), booleans.getOrDefault(id, false));
        } catch (InterruptedException e) {
            e.printStackTrace();
            return new WhatAmIDoing(channels.get(id), false);
        } finally {
            ConfirmationGetter.clean(id);
        }
    }

    /**
     * method to remove references of a user from {@link ConfirmationGetter}'s static hash maps
     * this normally runs after getting confirmation from that user
     * @param id the id of the user we want to clean from our maps
     * @author ioxom
     */
    public static void clean(long id) {
        booleans.remove(id);
        channels.remove(id);
        confirmationGetters.remove(id);
    }

    public void run() {
        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if (this.attempts >= 5) {
            channels.get(this.id).sendMessage("no proper response received, assuming no").queue();
            booleans.put(this.id, false);
        } else {
            booleans.put(this.id, response);
        }
    }
}