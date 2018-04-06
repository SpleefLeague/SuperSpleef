/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.spleefleague.superspleef.game.multi;

import com.spleefleague.core.chat.ChatManager;
import static com.spleefleague.gameapi.queue.Battle.calculateEloRatingChange;
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
            this.setPointsCup((players.size() - 1) * 5);
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
        List<SpleefPlayer> players = getPlayers();
        double[] ratingChanges = new double[players.size()];
        SpleefMode mode = getSpleefMode();
        for (int i = 0; i < players.size(); i++) {
            SpleefPlayer p1 = players.get(i);
            int score1 = getData(p1).getPoints();
            for (int j = i + 1; j < players.size(); j++) {
                SpleefPlayer p2 = players.get(j);
                int score2 = getData(p2).getPoints();
                double ratingChange = calculateEloRatingChange(p1.getRating(mode), p2.getRating(mode), Integer.compare(score2, score1));
                ratingChanges[i] += ratingChange;
                ratingChanges[j] -= ratingChange;
            }
        }
        for (int i = 0; i < ratingChanges.length; i++) {
            ratingChanges[i] /= ratingChanges.length - 1;
            SpleefPlayer sp = players.get(i);
            sp.setRating(mode, sp.getRating(mode) + (int)Math.ceil(ratingChanges[i]));
        }
        StringJoiner endScore = new StringJoiner("-");
        String playerList = "";
        for (int i = 0; i < players.size(); i++) {
            SpleefPlayer sp = players.get(i);
            endScore.add(Integer.toString(getData(sp).getPoints()));
            playerList += ChatColor.RED + sp.getName() + ChatColor.WHITE + " (" + sp.getRating(mode) + ")" + ChatColor.GREEN + " gets " + ChatColor.GRAY + (int)Math.ceil(ratingChanges[i]) + ChatColor.WHITE + " points. ";
        }
        ChatManager.sendMessage(mode.getChatPrefix(), ChatColor.GREEN + "Game in arena " + ChatColor.WHITE + getArena().getName() + ChatColor.GREEN + " is over " + ChatColor.WHITE + "(" + endScore + ")" + ChatColor.GREEN + ". " + playerList, SuperSpleef.getInstance().getEndMessageChannel());
        players.forEach((p) -> {
            SuperSpleef.getInstance().getPlayerManager().save(p);
        });
    }
}
