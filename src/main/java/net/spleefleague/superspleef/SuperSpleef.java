/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.spleefleague.superspleef;

import com.mongodb.DB;
import net.spleefleague.core.SpleefLeague;
import net.spleefleague.core.player.PlayerManager;
import net.spleefleague.core.plugin.CorePlugin;
import net.spleefleague.superspleef.game.BattleManager;
import net.spleefleague.superspleef.player.SpleefPlayer;
import org.bukkit.ChatColor;

/**
 *
 * @author Jonas
 */
public class SuperSpleef extends CorePlugin {

    private static SuperSpleef instance;
    private PlayerManager<SpleefPlayer> playerManager;
    private BattleManager battleManager;
    
    public SuperSpleef() {
        super("[SuperSpleef]", ChatColor.GRAY + "[" + ChatColor.GOLD + "SuperSpleef" + ChatColor.GRAY + "]" + ChatColor.RESET);
    }
    
    @Override
    public void start() {
        instance = this;
        this.playerManager = new PlayerManager(this, SpleefPlayer.class);
        this.battleManager = new BattleManager();
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
}