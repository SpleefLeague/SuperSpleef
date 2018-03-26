/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.spleefleague.superspleef.game.multispleef;

import com.spleefleague.core.chat.ChatManager;
import com.spleefleague.superspleef.SuperSpleef;
import com.spleefleague.superspleef.game.SpleefBattle;
import com.spleefleague.superspleef.game.SpleefMode;
import com.spleefleague.superspleef.player.SpleefPlayer;
import java.util.List;
import java.util.Random;
import java.util.StringJoiner;
import org.bukkit.ChatColor;

/**
 *
 * @author jonas
 */
public class MultiSpleefBattle extends SpleefBattle<MultiSpleefArena> {
    
    public MultiSpleefBattle(MultiSpleefArena arena, List<SpleefPlayer> players) {
        super(arena, players);
        if(arena.getMaxRating() == -1) {
            this.changePointsCup((players.size() - 1) * 5);
        }
    }

    @Override
    protected void addToBattleManager() {
        SuperSpleef.getInstance().getMultiSpleefBattleManager().add(this);
    }

    @Override
    protected void removeFromBattleManager() {
        SuperSpleef.getInstance().getMultiSpleefBattleManager().remove(this);
    }
    
    @Override
    public void onArenaLeave(SpleefPlayer player) {
        super.onArenaLeave(player);
        if(player.isDead() && player.getSpectatorTarget() == null && !isInCountdown()) {
            List<SpleefPlayer> alive = getAlivePlayers();
            giveTempSpectator(player, alive.get(new Random().nextInt(alive.size())).getPlayer());
        }
    }
    
    @Override
    protected void applyRatingChange(SpleefPlayer winner) {
        double[] ratingChanges = new double[getPlayers().size()];
        SpleefMode mode = getSpleefMode();
        for (int i = 0; i < getPlayers().size(); i++) {
            SpleefPlayer p1 = getPlayers().get(i);
            int score1 = getData(p1).getPoints();
            for (int j = i + 1; j < getPlayers().size(); j++) {
                SpleefPlayer p2 = getPlayers().get(j);
                int score2 = getData(p2).getPoints();
                double ratingChange = SpleefBattle.calculateEloRatingChange(p1.getRating(mode), p2.getRating(mode), Integer.compare(score2, score1));
                ratingChanges[i] += ratingChange;
                ratingChanges[j] -= ratingChange;
            }
        }
        for (int i = 0; i < ratingChanges.length; i++) {
            ratingChanges[i] /= ratingChanges.length - 1;
            getPlayers().get(i).setRating(mode, (int)Math.ceil(ratingChanges[i]));
        }
        StringJoiner endScore = new StringJoiner("-");
        String playerList = "";
        for (int i = 0; i < getPlayers().size(); i++) {
            SpleefPlayer sp = getPlayers().get(i);
            endScore.add(Integer.toString(getData(sp).getPoints()));
            playerList += ChatColor.RED + sp.getName() + ChatColor.WHITE + " (" + sp.getRating(mode) + ")" + ChatColor.GREEN + " gets " + ChatColor.GRAY + (int)Math.ceil(ratingChanges[i]) + ChatColor.WHITE + " points. ";
        }
        ChatManager.sendMessage(mode.getChatPrefix(), ChatColor.GREEN + "Game in arena " + ChatColor.WHITE + getArena().getName() + ChatColor.GREEN + " is over " + ChatColor.WHITE + "(" + endScore + ")" + ChatColor.GREEN + ". " + playerList, SuperSpleef.getInstance().getEndMessageChannel());
        this.getPlayers().forEach((p) -> {
            SuperSpleef.getInstance().getPlayerManager().save(p);
        });
    }
}
