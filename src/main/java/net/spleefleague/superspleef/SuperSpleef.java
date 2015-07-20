/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.spleefleague.superspleef;

import com.mongodb.client.MongoDatabase;
import net.spleefleague.core.SpleefLeague;
import net.spleefleague.core.chat.ChatChannel;
import net.spleefleague.core.chat.ChatManager;
import net.spleefleague.core.chat.Theme;
import net.spleefleague.core.command.CommandLoader;
import net.spleefleague.core.player.PlayerManager;
import net.spleefleague.core.player.Rank;
import net.spleefleague.core.plugin.GamePlugin;
import net.spleefleague.superspleef.game.Arena;
import net.spleefleague.superspleef.game.Battle;
import net.spleefleague.superspleef.game.BattleManager;
import net.spleefleague.superspleef.game.signs.GameSign;
import net.spleefleague.superspleef.listener.ConnectionListener;
import net.spleefleague.superspleef.listener.EnvironmentListener;
import net.spleefleague.superspleef.listener.GameListener;
import net.spleefleague.superspleef.listener.SignListener;
import net.spleefleague.superspleef.player.SpleefPlayer;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

/**
 *
 * @author Jonas
 */
public class SuperSpleef extends GamePlugin {

    private static SuperSpleef instance;
    private PlayerManager<SpleefPlayer> playerManager;
    private BattleManager battleManager;
    private boolean queuesOpen = true;
    
    public SuperSpleef() {
        super("[SuperSpleef]", ChatColor.GRAY + "[" + ChatColor.GOLD + "SuperSpleef" + ChatColor.GRAY + "]" + ChatColor.RESET);
    }
    
    @Override
    public void start() {
        instance = this;
        Arena.initialize();
        this.playerManager = new PlayerManager(this, SpleefPlayer.class);
        this.battleManager = new BattleManager();
        ChatManager.registerChannel(new ChatChannel("GAME_MESSAGE_SPLEEF_END", "Spleef game start notifications", Rank.DEFAULT, true));
        ChatManager.registerChannel(new ChatChannel("GAME_MESSAGE_SPLEEF_START", "Spleef game result messages", Rank.DEFAULT, true));
        ConnectionListener.init();
        GameListener.init();
        SignListener.init();
        EnvironmentListener.init();
        GameSign.initialize();
        CommandLoader.loadCommands(this, "net.spleefleague.superspleef.commands");
    }
    
    @Override
    public void stop() {
        for(Battle battle : battleManager.getAll()) {
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
    
    public BattleManager getBattleManager() {
        return battleManager;
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
        for(Battle battle : getBattleManager().getAll()) {
            if(battle.isSpectating(sjp)) {
                battle.removeSpectator(sjp);
            }
        }
    }
    
    @Override
    public boolean isSpectating(Player p) {
        SpleefPlayer sjp = getPlayerManager().get(p);
        for(Battle battle : getBattleManager().getAll()) {
            if(battle.isSpectating(sjp)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void dequeue(Player p) {
        SpleefPlayer sp = getPlayerManager().get(p);
        getBattleManager().dequeue(sp);
    }

    @Override
    public void cancel(Player p) {
        SpleefPlayer sp = getPlayerManager().get(p);
        Battle battle = getBattleManager().getBattle(sp);
        if(battle != null) {
            battle.cancel();    
            ChatManager.sendMessage(SuperSpleef.getInstance().getChatPrefix() + Theme.SUPER_SECRET.buildTheme(false) + " The battle on " + battle.getArena().getName() + " has been cancelled.", "STAFF");
        }
    }
    
    @Override
    public void surrender(Player p) {
        SpleefPlayer sjp = getPlayerManager().get(p);
        Battle battle = getBattleManager().getBattle(sjp);
        if(battle != null) {
            for(SpleefPlayer active : battle.getActivePlayers()) {
                active.sendMessage(SuperSpleef.getInstance().getChatPrefix() + Theme.SUPER_SECRET.buildTheme(false) + " " + p.getName() + " has surrendered!");
            }
            battle.removePlayer(sjp);
        }
    }

    @Override
    public boolean isQueued(Player p) {
        SpleefPlayer sp = getPlayerManager().get(p);
        return getBattleManager().isQueued(sp);
    }

    @Override
    public boolean isIngame(Player p) {
        SpleefPlayer sp = getPlayerManager().get(p);
        return getBattleManager().isIngame(sp);
    }
    
    @Override
    public void cancelAll() {
        for(Battle battle : battleManager.getAll()) {
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