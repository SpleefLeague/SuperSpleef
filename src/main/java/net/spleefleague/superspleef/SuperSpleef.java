/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.spleefleague.superspleef;

import com.mongodb.DB;
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
import net.spleefleague.superspleef.listener.ConnectionListener;
import net.spleefleague.superspleef.listener.GameListener;
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
    
    public SuperSpleef() {
        super("[SuperSpleef]", ChatColor.GRAY + "[" + ChatColor.GOLD + "SuperSpleef" + ChatColor.GRAY + "]" + ChatColor.RESET);
    }
    
    @Override
    public void start() {
        instance = this;
        Arena.initialize();
        this.playerManager = new PlayerManager(this, SpleefPlayer.class);
        this.battleManager = new BattleManager();
        ChatManager.registerPublicChannel(new ChatChannel("GAME_MESSAGE_SPLEEF_END", "Spleef game start notifications", Rank.DEFAULT, true));
        ChatManager.registerPublicChannel(new ChatChannel("GAME_MESSAGE_SPLEEF_START", "Spleef game result messages", Rank.DEFAULT, true));
        ConnectionListener.init();
        GameListener.init();
        CommandLoader.loadCommands(this, "net.spleefleague.superspleef.commands");
    }

    @Override
    public DB getPluginDB() {
        return SpleefLeague.getInstance().getMongo().getDB("SuperSpleef");
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
    public void spectate(Player p) {
        SpleefPlayer sp = getPlayerManager().get(p);
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
            ChatManager.sendMessage(SuperSpleef.getInstance().getChatPrefix() + Theme.SUPER_SECRET + " The battle on " + battle.getArena().getName() + " has been cancelled.", "STAFF");
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
}