package org.nyxcode.wurm.timedpay;

import com.wurmonline.server.Players;
import com.wurmonline.server.WurmCalendar;
import com.wurmonline.server.players.Player;
import org.gotti.wurmunlimited.modloader.classhooks.HookManager;
import org.gotti.wurmunlimited.modloader.interfaces.Configurable;
import org.gotti.wurmunlimited.modloader.interfaces.Initable;
import org.gotti.wurmunlimited.modloader.interfaces.PreInitable;
import org.gotti.wurmunlimited.modloader.interfaces.WurmServerMod;

import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by whisper2shade on 22.01.2017.
 */
public class TimedPay implements WurmServerMod, Initable, PreInitable, Configurable {

    long lastPayout;

    private static Logger logger = Logger.getLogger("org.gotti.wurmunlimited.mods.timedpaymod.TimedPayMod");
    private int amountCash;
    private int amountKarma;
    private int payoutInterval;

    @Override
    public void configure(Properties properties) {
        amountCash = Integer.parseInt(properties.getProperty("amountCash"));
        amountKarma = Integer.parseInt(properties.getProperty("amountKarma"));
        payoutInterval = 8 * Integer.parseInt(properties.getProperty("payoutInterval"));
    }

    @Override
    public void init() {
        HookManager.getInstance().registerHook("com.wurmonline.server.Players", "pollPlayers", "()V", () -> (object, method, args) -> {
            Players players = (Players) object;
            addMoneyToLoggedPlayers(players);
            return method.invoke(object, args);
        });
        lastPayout = WurmCalendar.currentTime;
    }

    @Override
    public void preInit() {

    }

    public long addMoneyToLoggedPlayers(Players players) {
        Set<String> processedSteamIds = new HashSet<>();
        long currentTime = WurmCalendar.getCurrentTime();
        if (currentTime > lastPayout + payoutInterval) {
            logger.log(Level.FINE, "executing payout");
            lastPayout = currentTime;
            List<Player> playerList = Arrays.asList(players.getPlayers());
            for (Player player : playerList) {
                String steamId = player.SteamId;
                if (!processedSteamIds.contains(steamId)) {
                    addMoneyAndKarma(player);
                    processedSteamIds.add(steamId);
                }
            }
        }
        return currentTime;
    }


    public void addMoneyAndKarma(Player player) {
        try {
            StringBuilder sb = new StringBuilder();
            if(amountCash > 0) {
                sb.append("Giving cash ");
                player.addMoney(amountCash);
            }
            if(amountKarma > 0) {
                if(sb.length() > 0) {
                    sb.append("and karma ");
                } else {
                    sb.append("Giving karma ");
                }
                player.setKarma(player.getKarma() + amountKarma);
            }
            sb.append("to player " + player.getName());
            logger.log(Level.INFO, sb.toString());
        } catch (IOException e) {

        }
    }
}

