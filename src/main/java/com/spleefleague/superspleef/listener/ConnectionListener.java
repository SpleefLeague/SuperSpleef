/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.spleefleague.superspleef.listener;

import com.comphenix.packetwrapper.WrapperPlayServerPlayerInfo;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.comphenix.protocol.wrappers.PlayerInfoData;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import com.comphenix.protocol.wrappers.WrappedGameProfile;
import com.spleefleague.core.SpleefLeague;
import com.spleefleague.core.player.SLPlayer;
import com.spleefleague.superspleef.SuperSpleef;
import com.spleefleague.superspleef.game.SpleefBattle;
import com.spleefleague.superspleef.player.SpleefPlayer;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_11_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Jonas
 */
public class ConnectionListener implements Listener {

    private static Listener instance;

    public static void init() {
        if (instance == null) {
            instance = new ConnectionListener();
            Bukkit.getPluginManager().registerEvents(instance, SuperSpleef.getInstance());
        }
    }

    private ConnectionListener() {

    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        SpleefPlayer sp = SuperSpleef.getInstance().getPlayerManager().get(event.getPlayer());
        if (sp == null) {
            return;
        }
        sp.invalidatePlayToRequest();
        if(sp.isIngame()) {
            SuperSpleef.getInstance().getBattleManager().getBattle(sp).removePlayer(sp, false);
        } else {
            SuperSpleef.getInstance().getBattleManager().dequeue(sp);
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        List<Player> ingamePlayers = new ArrayList<>();
        List<SpleefBattle> toCancel = new ArrayList<>();//Workaround
        for (SpleefBattle battle : SuperSpleef.getInstance().getBattleManager().getAll()) {
            for (SpleefPlayer p : battle.getActivePlayers()) {
                if (p.getPlayer() != null) {
                    event.getPlayer().hidePlayer(p.getPlayer());
                    p.getPlayer().hidePlayer(event.getPlayer());
                    ingamePlayers.add(p.getPlayer());
                } else {
                    toCancel.add(battle);
                    break;
                }
            }
        }
        for (SpleefBattle battle : toCancel) {
            for (SpleefPlayer p : battle.getActivePlayers()) {
                if (p.getPlayer() != null) {
                    p.kickPlayer("An error has occured. Please reconnect");
                }
            }
            SuperSpleef.getInstance().getBattleManager().remove(battle);
        }
        Bukkit.getScheduler().runTaskLater(SuperSpleef.getInstance(), () -> {
            List<PlayerInfoData> list = new ArrayList<>();
            SpleefLeague.getInstance().getPlayerManager().getAll().forEach((SLPlayer slPlayer) -> list.add(new PlayerInfoData(WrappedGameProfile.fromPlayer(slPlayer.getPlayer()), ((CraftPlayer) slPlayer.getPlayer()).getHandle().ping, EnumWrappers.NativeGameMode.SURVIVAL, WrappedChatComponent.fromText(slPlayer.getRank().getColor() + slPlayer.getName()))));
            WrapperPlayServerPlayerInfo packet = new WrapperPlayServerPlayerInfo();
            packet.setAction(EnumWrappers.PlayerInfoAction.ADD_PLAYER);
            packet.setData(list);
            ingamePlayers.forEach((Player p) -> packet.sendPacket(p));

            list.clear();
            ingamePlayers.forEach((Player p) -> {
                SLPlayer generalPlayer = SpleefLeague.getInstance().getPlayerManager().get(p);
                list.add(new PlayerInfoData(WrappedGameProfile.fromPlayer(p), ((CraftPlayer) p).getHandle().ping, EnumWrappers.NativeGameMode.SURVIVAL, WrappedChatComponent.fromText(generalPlayer.getRank().getColor() + generalPlayer.getName())));
            });
            packet.setData(list);
            packet.sendPacket(event.getPlayer());
        }, 10);
    }
}
