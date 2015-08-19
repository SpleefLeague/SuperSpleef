/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.spleefleague.superspleef;

import com.mongodb.client.MongoDatabase;
import com.spleefleague.core.SpleefLeague;
import com.spleefleague.core.chat.ChatChannel;
import com.spleefleague.core.chat.ChatManager;
import com.spleefleague.core.chat.Theme;
import com.spleefleague.core.command.CommandLoader;
import com.spleefleague.core.player.PlayerManager;
import com.spleefleague.core.player.Rank;
import com.spleefleague.core.plugin.GamePlugin;
import com.spleefleague.superspleef.game.Arena;
import com.spleefleague.superspleef.game.Battle;
import com.spleefleague.superspleef.game.BattleManager;
import com.spleefleague.superspleef.game.SpleefMode;
import com.spleefleague.superspleef.game.signs.GameSign;
import com.spleefleague.superspleef.listener.ConnectionListener;
import com.spleefleague.superspleef.listener.EnvironmentListener;
import com.spleefleague.superspleef.listener.GameListener;
import com.spleefleague.superspleef.listener.SignListener;
import com.spleefleague.superspleef.player.SpleefPlayer;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

/**
 *
 * @author Jonas
 */
public class SuperSpleef extends GamePlugin {

    private static SuperSpleef instance;
    private PlayerManager<SpleefPlayer> playerManager;
    private BattleManager battleManagerSpleef;
    private BattleManager battleManagerMultiSpleef;
    private boolean queuesOpen = true;
    
    public SuperSpleef() {
        super("[SuperSpleef]", ChatColor.GRAY + "[" + ChatColor.GOLD + "SuperSpleef" + ChatColor.GRAY + "]" + ChatColor.RESET);
    }
    
    @Override
    public void start() {
        instance = this;
        Arena.initialize();
        this.playerManager = new PlayerManager(this, SpleefPlayer.class);
        this.battleManagerSpleef = new BattleManager(SpleefMode.NORMAL);
        this.battleManagerMultiSpleef = new BattleManager(SpleefMode.MULTI);
        ChatManager.registerChannel(new ChatChannel("GAME_MESSAGE_SPLEEF_END", "Spleef game start notifications", Rank.DEFAULT, true));
        ChatManager.registerChannel(new ChatChannel("GAME_MESSAGE_SPLEEF_START", "Spleef game result messages", Rank.DEFAULT, true));
        ConnectionListener.init();
        GameListener.init();
        SignListener.init();
        EnvironmentListener.init();
        GameSign.initialize();
        CommandLoader.loadCommands(this, "com.spleefleague.superspleef.commands");
    }
    
    @Override
    public void stop() {
        for(Battle battle : battleManagerSpleef.getAll()) {
            battle.cancel(false);
        }
        playerManager.saveAll();
    }

    @Override
    public MongoDatabase getPluginDB() {
        return SpleefLeague.getInstance().getMongo().getDatabase("SuperSpleef");
    }   
    
    public PlayerManager<SpleefPlayer> getPlayerManager() {
        return playerManager;
    }
    
    public BattleManager getBattleManagerSpleef() {
        return battleManagerSpleef;
    }
    
    public BattleManager getBattleManagerMultiSpleef() {
        return battleManagerMultiSpleef;
    }
    
    public static SuperSpleef getInstance() {
        return instance;
    }
    
    @Override
    public boolean spectate(Player target, Player p) {
        SpleefPlayer tsjp = getPlayerManager().get(target);
        SpleefPlayer sjp = getPlayerManager().get(p);
        if(sjp.getVisitedArenas().contains(tsjp.getCurrentBattle().getArena())) {
            tsjp.getCurrentBattle().addSpectator(sjp);
            return true;
        }
        else {
            p.sendMessage(SuperSpleef.getInstance().getChatPrefix() + Theme.ERROR.buildTheme(false) + " You can only spectate arenas you have already visited!");
            return false;
        }
    }
    
    @Override
    public void unspectate(Player p) {
        SpleefPlayer sjp = getPlayerManager().get(p);
        for(Battle battle : getBattleManagerSpleef().getAll()) {
            if(battle.isSpectating(sjp)) {
                battle.removeSpectator(sjp);
            }
        }
        for(Battle battle : getBattleManagerMultiSpleef().getAll()) {
            if(battle.isSpectating(sjp)) {
                battle.removeSpectator(sjp);
            }
        }
    }
    
    @Override
    public boolean isSpectating(Player p) {
        SpleefPlayer sjp = getPlayerManager().get(p);
        for(Battle battle : getBattleManagerSpleef().getAll()) {
            if(battle.isSpectating(sjp)) {
                return true;
            }
        }
        for(Battle battle : getBattleManagerMultiSpleef().getAll()) {
            if(battle.isSpectating(sjp)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void dequeue(Player p) {
        SpleefPlayer sp = getPlayerManager().get(p);
        getBattleManagerSpleef().dequeue(sp);
        getBattleManagerMultiSpleef().dequeue(sp);
    }

    @Override
    public void cancel(Player p) {
        SpleefPlayer sp = getPlayerManager().get(p);
        Battle battle = getBattleManagerSpleef().getBattle(sp);
        if(battle == null) {
            battle = getBattleManagerMultiSpleef().getBattle(sp);
        }
        if(battle != null) {
            battle.cancel();    
            ChatManager.sendMessage(SuperSpleef.getInstance().getChatPrefix() + Theme.SUPER_SECRET.buildTheme(false) + " The battle on " + battle.getArena().getName() + " has been cancelled.", "STAFF");
        }
    }
    
    @Override
    public void surrender(Player p) {
        SpleefPlayer sp = getPlayerManager().get(p);
        Battle battle = getBattleManagerSpleef().getBattle(sp);
        if(battle == null) {
            battle = getBattleManagerMultiSpleef().getBattle(sp);
        }
        if(battle != null) {
            for(SpleefPlayer active : battle.getActivePlayers()) {
                active.sendMessage(SuperSpleef.getInstance().getChatPrefix() + Theme.SUPER_SECRET.buildTheme(false) + " " + p.getName() + " has surrendered!");
            }
            battle.removePlayer(sp);
        }
    }

    @Override
    public boolean isQueued(Player p) {
        SpleefPlayer sp = getPlayerManager().get(p);
        if(!getBattleManagerSpleef().isQueued(sp)) {
            return getBattleManagerMultiSpleef().isQueued(sp);
        }
        return true;
    }

    @Override
    public boolean isIngame(Player p) {
        SpleefPlayer sp = getPlayerManager().get(p);
        if(!getBattleManagerSpleef().isIngame(sp)) {
            return getBattleManagerMultiSpleef().isIngame(sp);
        }
        return true;
    }
    
    @Override
    public void cancelAll() {
        for(Battle battle : battleManagerSpleef.getAll()) {
            battle.cancel();
        }
    }

    @Override
    public void printStats(Player p) {
        SpleefPlayer sjp = playerManager.get(p);
        p.sendMessage(Theme.INFO + p.getName() + "'s Spleef stats");
        p.sendMessage(Theme.INCOGNITO + "Rating: " + ChatColor.YELLOW + sjp.getRating());
        p.sendMessage(Theme.INCOGNITO + "Rank: " + ChatColor.YELLOW + sjp.getRank());
    }

    @Override
    public void requestEndgame(Player p) {
        SpleefPlayer sp = SuperSpleef.getInstance().getPlayerManager().get(p);
        Battle battle = sp.getCurrentBattle();
        if (battle != null) {
            sp.setRequestingEndgame(true);
            boolean shouldEnd = true;
            for (SpleefPlayer spleefplayer : battle.getActivePlayers()) {
                if (!spleefplayer.isRequestingEndgame()) {
                    shouldEnd = false;
                    break;
                }
            }
            if (shouldEnd) {
                battle.cancel(false);
            }
            else {
                for (SpleefPlayer spleefplayer : battle.getActivePlayers()) {
                    if (!spleefplayer.isRequestingEndgame()) {
                        spleefplayer.getPlayer().sendMessage(SuperSpleef.getInstance().getChatPrefix() + " " + Theme.WARNING.buildTheme(false) + "Your opponent wants to end this game. To agree enter " + ChatColor.YELLOW + "/endgame.");
                    }
                }
                sp.sendMessage(SuperSpleef.getInstance().getChatPrefix() + " " + Theme.WARNING.buildTheme(false) + "You requested this game to be cancelled.");
            }
        }
    }

    @Override
    public void setQueueStatus(boolean open) {
        queuesOpen = open;
    }
    
    public boolean queuesOpen() {
        return queuesOpen;
    }
}