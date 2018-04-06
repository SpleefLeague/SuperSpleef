/*
        
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.spleefleague.superspleef.game.classic;

import com.spleefleague.core.chat.ChatManager;
import static com.spleefleague.gameapi.queue.Battle.calculateEloRatingChange;
import com.spleefleague.superspleef.SuperSpleef;
import com.spleefleague.superspleef.game.SpleefBattle;
import com.spleefleague.superspleef.game.SpleefMode;
import com.spleefleague.superspleef.player.SpleefPlayer;
import java.util.List;
import org.bukkit.ChatColor;

/**
 *
 * @author Jonas
 */
public class NormalSpleefBattle extends SpleefBattle<NormalSpleefArena> {
    
    public NormalSpleefBattle(NormalSpleefArena arena, List<SpleefPlayer> players) {
        super(arena, players);
    }

    @Override
    protected void addToBattleManager() {
        SuperSpleef.getInstance().getClassicSpleefBattleManager().add(this);
    }

    @Override
    protected void removeFromBattleManager() {
        SuperSpleef.getInstance().getClassicSpleefBattleManager().remove(this);
    }
    
    @Override
    protected void applyRatingChange(SpleefPlayer winner) {
        if(getPlayers().size() != 2) return;
        SpleefPlayer p1 = getPlayers().get(0);
        SpleefPlayer p2 = getPlayers().get(1);
        if(winner != null && p1 != winner && p2 != winner) return;
        SpleefMode mode = this.getSpleefMode();
        int winnerCase = p1 == winner ? -1 : p2 == winner ? 1 : 0;
        int ratingChange = (int)Math.ceil(calculateEloRatingChange(p1.getRating(mode), p2.getRating(mode), winnerCase));
        String endScore = getData(p1).getPoints() + "-" + getData(p2).getPoints();
        String playerList = "";
        p1.setRating(mode, p1.getRating(mode) + ratingChange);
        p2.setRating(mode, p2.getRating(mode) - ratingChange);
        playerList += ChatColor.RED + p1.getName() + ChatColor.WHITE + " (" + p1.getRating(mode) + ")" + ChatColor.GREEN + " gets " + ChatColor.GRAY + ratingChange + ChatColor.GREEN + (ratingChange == 1 ? " point. " : " points. ");
        playerList += ChatColor.RED + p2.getName() + ChatColor.WHITE + " (" + p2.getRating(mode) + ")" + ChatColor.GREEN + " gets " + ChatColor.GRAY + -ratingChange + ChatColor.GREEN + (ratingChange == 1 ? " point. " : " points. ");
        ChatManager.sendMessage(mode.getChatPrefix(), ChatColor.GREEN + "Game in arena " + ChatColor.WHITE + getArena().getName() + ChatColor.GREEN + " is over " + ChatColor.WHITE + "(" + endScore + ")" + ChatColor.GREEN + ". " + playerList, SuperSpleef.getInstance().getEndMessageChannel());
        this.getPlayers().forEach((p) -> {
            SuperSpleef.getInstance().getPlayerManager().save(p);
        });
    }
}
