/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.spleefleague.superspleef.game;

import com.spleefleague.core.SpleefLeague;
import com.spleefleague.core.chat.ChatChannel;
import com.spleefleague.core.chat.ChatManager;
import com.spleefleague.core.chat.Theme;
import com.spleefleague.core.io.EntityBuilder;
import com.spleefleague.core.player.PlayerState;
import com.spleefleague.core.player.Rank;
import com.spleefleague.core.player.SLPlayer;
import com.spleefleague.core.plugin.GamePlugin;
import com.spleefleague.superspleef.SuperSpleef;
import com.spleefleague.superspleef.game.signs.GameSign;
import com.spleefleague.superspleef.player.SpleefPlayer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

/**
 *
 * @author Jonas
 */
public class Battle {
    
    private final Arena arena;
    private final List<SpleefPlayer> players; //MIGHT CONTAIN PLAYERS WHICH LEFT THE GAME. USE getActivePlayers() FOR ACTIVE PLAYERS INSTEAD
    private final List<SpleefPlayer> spectators;
    private final HashSet<Block> destroyedBlocks;
    private final HashMap<SpleefPlayer, PlayerData> data;
    private final ChatChannel cc;
    private int ticksPassed = 0;
    private BukkitRunnable clock;
    private Scoreboard scoreboard;
    private boolean inCountdown = false;
    
    protected Battle(Arena arena, List<SpleefPlayer> players) {
        this.arena = arena;
        this.players = players;
        this.spectators = new ArrayList<>();
        this.data = new HashMap<>();
        this.destroyedBlocks = new HashSet<>();
        this.cc = new ChatChannel("GAMECHANNEL" + this.hashCode(), "GAMECHANNEL" + this.hashCode(), Rank.DEFAULT, false, true);
    }

    public Arena getArena() {
        return arena;
    }

    public Collection<SpleefPlayer> getPlayers() {
        return players;
    }
    
    public boolean isNormalSpleef() {
        return players.size() == 2;
    }
    
    public void addSpectator(SpleefPlayer sp) {
        spectators.add(sp);
        Location spawn = arena.getSpectatorSpawn();
        if(spawn != null) {
            sp.getPlayer().teleport(spawn);
        }
        sp.getPlayer().setScoreboard(scoreboard);
        sp.sendMessage(Theme.INCOGNITO + "You are now spectating the battle on " + ChatColor.GREEN + arena.getName());
        SLPlayer slp = SpleefLeague.getInstance().getPlayerManager().get(sp.getPlayer());
        slp.setState(PlayerState.SPECTATING);
        slp.addChatChannel(cc.getName());
    }
    
    public boolean isSpectating(SpleefPlayer sjp) {
        return spectators.contains(sjp);
    }
    
    public void removeSpectator(SpleefPlayer sp) {
        resetPlayer(sp);
    }

    public void removePlayer(SpleefPlayer sp) {
        removePlayer(sp, sp.getName() + " has left the game!");
    }
    
    public void removePlayer(SpleefPlayer sp, String message) {
        resetPlayer(sp);
        ArrayList<SpleefPlayer> activePlayers = getActivePlayers();
        if(activePlayers.size() == 1) {
            end(activePlayers.get(0));
        }
        else if(activePlayers.size() > 1) {   
            for(SpleefPlayer pl : activePlayers) {
                pl.getPlayer().sendMessage(SuperSpleef.getInstance().getChatPrefix() + " " + Theme.ERROR.buildTheme(false) + message);
            }
            if(arena.getSpleefMode() == SpleefMode.MULTI) {
                scoreboard.getObjective("rounds").getScore(ChatColor.GREEN + "Players:").setScore(activePlayers.size());
            }
        }
    }
    
    public void addDestroyedBlock(Block block) {
        destroyedBlocks.add(block);
    }
    
    public ArrayList<SpleefPlayer> getActivePlayers() {
        ArrayList<SpleefPlayer> active = new ArrayList<>();
        for (SpleefPlayer sp : players) {
            if (sp.isIngame()) {
                active.add(sp);
            }
        }
        return active;
    }
    
    private void resetPlayer(SpleefPlayer sp) {
        SLPlayer slp = SpleefLeague.getInstance().getPlayerManager().get(sp.getPlayer());
        if(spectators.contains(sp)) {
            spectators.remove(sp);
        }
        else {
            removeSpawnCage(this.getData(sp).getSpawn());
            sp.setIngame(false);
            sp.setFrozen(false);
            sp.setRequestingReset(false);
            sp.setRequestingEndgame(false);
            data.get(sp).restoreOldData();
        }
        sp.getPlayer().setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());    
        sp.getPlayer().teleport(SpleefLeague.getInstance().getSpawnLocation());
        slp.removeChatChannel(cc.getName());   
        slp.setState(PlayerState.IDLE);
    }

    public void end(SpleefPlayer winner) {
        end(winner, arena.isRated());
    }

    public void end(SpleefPlayer winner, boolean rated) {
        saveGameHistory(winner);
        if(arena.getSpleefMode() == SpleefMode.MULTI) {
            ChatManager.sendMessage(SuperSpleef.getInstance().getChatPrefix(), ChatColor.RED + winner.getName() + ChatColor.GREEN + " won the MultiSpleef battle on " + ChatColor.WHITE + arena.getName() + ChatColor.GREEN + "!", "GAME_MESSAGE_SPLEEF_END");
        }
        if (rated) {
            applyRatingChange(winner);
        }
        for(SpleefPlayer sp : getActivePlayers()) {
            resetPlayer(sp);
        }
        for(SpleefPlayer sp : new ArrayList<>(spectators)) {
            resetPlayer(sp);
        }
        cleanup();
    }
    
    private void cleanup() {
        clock.cancel();
        resetField();
        arena.setOccupied(false);
        SuperSpleef.getInstance().getBattleManagerSpleef().remove(this);
        ChatManager.unregisterChannel(cc);
        GameSign.updateGameSigns(arena);
    }
    
    public void resetField() {
        for(Block block : destroyedBlocks) {
            block.setType(Material.SNOW_BLOCK);
        }
        destroyedBlocks.clear();
    }
    
    public void cancel() {
        cancel(true);
    }

    public void cancel(boolean sendMessage) {
        saveGameHistory(null);
        if(sendMessage) {
            ChatManager.sendMessage(SuperSpleef.getInstance().getChatPrefix(), Theme.INCOGNITO.buildTheme(false) + "The battle has been cancelled by a moderator.", cc.getName());
        }
        for(SpleefPlayer sp : new ArrayList<>(spectators)) {
            resetPlayer(sp);
        }
        for (SpleefPlayer sp : getActivePlayers()) {
            resetPlayer(sp);
        }
        cleanup();
    }
    
    public void onArenaLeave(SpleefPlayer player) {
            if(isInCountdown()) {
            player.getPlayer().teleport(data.get(player).getSpawn());
        }
        else {
            if(arena.getSpleefMode() == SpleefMode.NORMAL) {
                for(SpleefPlayer sp : getActivePlayers()) {
                    if(sp != player) {
                        PlayerData playerdata = this.data.get(sp);
                        playerdata.increasePoints();
                        scoreboard.getObjective("rounds").getScore(sp.getName()).setScore(playerdata.getPoints());
                        if(playerdata.getPoints() < arena.getMaxRating()) {
                            int round = 0;
                            for(PlayerData pd : data.values()) {
                                round += pd.getPoints();
                            }
                            ChatManager.sendMessage(SuperSpleef.getInstance().getChatPrefix(), Theme.INFO.buildTheme(false) + sp.getName() + " has won round " + round, cc.getName());
                            startRound();
                        }
                        else {
                            end(sp);
                        }
                    }
                }
            }
            else if(arena.getSpleefMode() == SpleefMode.MULTI) {
                removePlayer(player);
                for(SpleefPlayer sp : getActivePlayers()) {
                    if(sp != player) {
                        PlayerData playerdata = this.data.get(sp);
                        playerdata.increasePoints();
                    }
                }
            }
            else {
                this.cancel(false);
            }
        }
    }

    public void start() {
        arena.setOccupied(true);
        GameSign.updateGameSigns(arena);
        ChatManager.registerChannel(cc);
        SuperSpleef.getInstance().getBattleManagerSpleef().add(this);
        scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
        Objective objective = scoreboard.registerNewObjective("rounds", "dummy");
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);
        if(arena.getSpleefMode() == SpleefMode.MULTI) {
            objective.setDisplayName(ChatColor.GRAY + "00:00:00 | " + ChatColor.RED + "Players:");
        }
        else {
            objective.setDisplayName(ChatColor.GRAY + "00:00:00 | " + ChatColor.RED + "Score:");
        }
        String playerNames = "";
        for(int i = 0; i < players.size(); i++) {
            SpleefPlayer sp = players.get(i);
            if(i == 0) {
                playerNames = ChatColor.RED + sp.getName();
            }
            else if(i == players.size() - 1) {
                playerNames += ChatColor.GREEN + " and " + ChatColor.RED + sp.getName();
            }
            else {
                playerNames += ChatColor.GREEN + ", " + ChatColor.RED + sp.getName();
            }
            Player p = sp.getPlayer();
            GamePlugin.unspectateGlobal(p);
            GamePlugin.dequeueGlobal(p);
            SLPlayer slp = SpleefLeague.getInstance().getPlayerManager().get(p);
            this.data.put(sp, new PlayerData(sp, arena.getSpawns()[i]));
            p.eject();
            p.teleport(arena.getSpawns()[i]);
            p.setHealth(p.getMaxHealth());
            p.setFoodLevel(20);
            sp.setIngame(true);
            sp.setFrozen(true);
            sp.setRequestingReset(false);
            p.setScoreboard(scoreboard);
            p.setGameMode(GameMode.SURVIVAL);
            p.getInventory().clear();
            p.getInventory().addItem(new ItemStack(Material.DIAMOND_SPADE));
            for(PotionEffect effect : p.getActivePotionEffects()) {
                p.removePotionEffect(effect.getType());
            }
            for(SpleefPlayer sp1 : players) {
                if(sp != sp1) {
                    p.showPlayer(sp1.getPlayer());
                }
            }
            p.setFlying(false);
            p.setAllowFlight(false);
            slp.addChatChannel(cc.getName());
            if(arena.getSpleefMode() == SpleefMode.NORMAL) {
                scoreboard.getObjective("rounds").getScore(sp.getName()).setScore(data.get(sp).getPoints());
            }
            slp.setState(PlayerState.INGAME);
        }
        if(arena.getSpleefMode() == SpleefMode.MULTI) {
            scoreboard.getObjective("rounds").getScore(ChatColor.GREEN + "Players:").setScore(getActivePlayers().size());
        }
        ChatManager.sendMessage(SuperSpleef.getInstance().getChatPrefix(), Theme.SUCCESS.buildTheme(false) + "Beginning match on " + ChatColor.WHITE + arena.getName() + ChatColor.GREEN + " between " + ChatColor.RED + playerNames + ChatColor.GREEN + "!", "GAME_MESSAGE_SPLEEF_START");
        for (int i = 0; i < players.size(); i++) {
            SpleefPlayer sp = players.get(i);
            sp.getPlayer().teleport(arena.getSpawns()[i]);
        }
        startClock();
        startRound();
    }
    
    public void startRound() {
        inCountdown = true;
        resetField();
        for(SpleefPlayer sp : getActivePlayers()) {
            Location spawn = this.data.get(sp).getSpawn();
            createSpawnCage(spawn);
            sp.setFrozen(true);
            sp.setRequestingReset(false);
            sp.setRequestingEndgame(false);
            sp.getPlayer().setFireTicks(0);
            sp.getPlayer().teleport(this.data.get(sp).getSpawn());
        }
        BukkitRunnable br = new BukkitRunnable() {
            private int secondsLeft = 3;

            @Override
            public void run() {
                if (secondsLeft > 0) {
                    ChatManager.sendMessage(SuperSpleef.getInstance().getChatPrefix(), secondsLeft + "...", cc.getName());
                    secondsLeft--;
                } else {
                    ChatManager.sendMessage(SuperSpleef.getInstance().getChatPrefix(), "GO!", cc.getName());
                    for (SpleefPlayer sp : getActivePlayers()) {
                        sp.setFrozen(false);
                    }
                    onDone();
                    super.cancel();
                }
            }
            
            public void onDone() {
                for(SpleefPlayer sp : getActivePlayers()) {
                    removeSpawnCage(data.get(sp).getSpawn());
                    sp.setFrozen(false);
                }
                inCountdown = false;
            }
        };
        br.runTaskTimer(SuperSpleef.getInstance(), 20, 20);
    }

    private void updateScoreboardTime() {
        if (scoreboard == null) {
            return;
        }
        Objective objective = scoreboard.getObjective("rounds");
        if (objective != null) {
            String s = DurationFormatUtils.formatDuration(ticksPassed * 50, "HH:mm:ss", true);
            if(arena.getSpleefMode() == SpleefMode.MULTI) {
                objective.setDisplayName(ChatColor.GRAY.toString() + s + " | " + ChatColor.RED + "MultiSpleef");
            }
            else {
                objective.setDisplayName(ChatColor.GRAY.toString() + s + " | " + ChatColor.RED + "Score:");
            }
        }
    }

    private void startClock() {
        clock = new BukkitRunnable() {
            @Override
            public void run() {
                if(!isInCountdown()) {
                    ticksPassed++;
                    updateScoreboardTime();
                }
            }
        };
        clock.runTaskTimer(SuperSpleef.getInstance(), 0, 1);
    }

    private void createSpawnCage(Location s) {
        modifySpawnCage(s, Material.GLASS);
    }
    
    private void removeSpawnCage(Location s) {
        modifySpawnCage(s, Material.AIR);
    }
    
    private void modifySpawnCage(Location s, Material type) {
        World w = s.getWorld();
        for (int x = s.getBlockX() - 1; x <= s.getBlockX() + 1; x++) {
            for (int z = s.getBlockZ() - 1; z <= s.getBlockZ() + 1; z++) {
                if (x == s.getBlockX() && z == s.getBlockZ()) {
                    w.getBlockAt(x, s.getBlockY(), z).setType(Material.AIR); //Just in case
                    w.getBlockAt(x, s.getBlockY() + 1, z).setType(Material.AIR);
                    w.getBlockAt(x, s.getBlockY() + 2, z).setType(type);
                } 
                else {
                    for (int y = s.getBlockY(); y <= s.getBlockY() + 2; y++) {
                        w.getBlockAt(x, y, z).setType(type);
                    }
                }
            }
        }
    }
    
    public boolean isInCountdown() {
        return inCountdown;
    }
    
    private void applyRatingChange(SpleefPlayer winner) {
        if (arena.getSpleefMode() == SpleefMode.NORMAL) {
            int winnerPoints = 0;
            int winnerSWCPoints = 0;
            final int MIN_RATING = 1, MAX_RATING = 40;
            String playerList = "";
            for (SpleefPlayer sp : players) {
                if (sp != winner) {
                    float elo = (float) (1f / (1f + Math.pow(2f, ((sp.getRating() - winner.getRating()) / 250f))));
                    int rating = (int) Math.round(MAX_RATING * (1f - elo));
                    if (rating < MIN_RATING) {
                        rating = MIN_RATING;
                    }
                    winnerPoints += rating;
                    sp.setRating(sp.getRating() - rating);
                    playerList += ChatColor.RED + sp.getName() + ChatColor.WHITE + " (" + sp.getRating() + ")" + ChatColor.GREEN + " gets " + ChatColor.GRAY + -rating + ChatColor.WHITE + " points. ";
                    if (winner.joinedSWC() && sp.joinedSWC()) {
                        float swcElo = (float) (1f / (1f + Math.pow(2f, ((sp.getSwcRating() - winner.getSwcRating()) / 400f))));
                        int swcRating = (int) Math.round(MAX_RATING * (1f - swcElo));
                        if (swcRating < MIN_RATING) {
                            swcRating = MIN_RATING;
                        }
                        winnerSWCPoints += swcRating;
                        sp.setSwcRating(sp.getSwcRating() - swcRating);
                        sp.sendMessage(SuperSpleef.getInstance().getChatPrefix() + ChatColor.GREEN + " You lost " + ChatColor.GRAY + -swcRating + " (" + sp.getSwcRating() + ")" + ChatColor.GREEN + " SWC points");
                        //playerList += ChatColor.RED + sp.getName() + ChatColor.GREEN + " also lost " + ChatColor.GRAY + -swcRating + ChatColor.WHITE + " SWC points. ";
                    }
                }
            }
            winner.setRating(winner.getRating() + winnerPoints);
            playerList += ChatColor.RED + winner.getName() + ChatColor.WHITE + " (" + winner.getRating() + ")" + ChatColor.GREEN + " gets " + ChatColor.GRAY + winnerPoints + ChatColor.GREEN + " points. ";
            if (winner.joinedSWC()) {
                //playerList += ChatColor.RED + winner.getName() + ChatColor.GREEN + " also gets " + ChatColor.GRAY + winnerSWCPoints + ChatColor.WHITE + " SWC points. ";
                winner.setSwcRating(winner.getSwcRating() + winnerSWCPoints);
                winner.sendMessage(SuperSpleef.getInstance().getChatPrefix() + ChatColor.GREEN + " You got " + ChatColor.GRAY + winnerSWCPoints + " (" + winner.getSwcRating() + ")" + ChatColor.GREEN + " SWC points");
            }
            ChatManager.sendMessage(SuperSpleef.getInstance().getChatPrefix(), ChatColor.GREEN + "Game in arena " + ChatColor.WHITE + arena.getName() + ChatColor.GREEN + " is over. " + playerList, "GAME_MESSAGE_SPLEEF_END");
        }
        else if(arena.getSpleefMode()== SpleefMode.MULTI) {
            //Comming maybe later
        }
    }
    
    private void saveGameHistory(SpleefPlayer winner) {
        GameHistory gh = new GameHistory(this, winner);
        EntityBuilder.save(gh, SuperSpleef.getInstance().getPluginDB().getCollection("GameHistory"));
    }
    
    public PlayerData getData(SpleefPlayer sp) {
        return data.get(sp);
    }
    
    public int getDuration() {
        return ticksPassed;
    }

    public static class PlayerData {

        private int points;
        private final Location spawn;
        private final SpleefPlayer sp;
        private final GameMode oldGamemode;
        private final ItemStack[] oldInventory;
        
        public PlayerData(SpleefPlayer sp, Location spawn) {
            this.sp = sp;
            this.spawn = spawn;
            this.points = 0;
            Player p = sp.getPlayer();
            oldGamemode = p.getGameMode();
            oldInventory = p.getInventory().getContents();
        }

        public Location getSpawn() {
            return spawn;
        }

        public int getPoints() {
            return points;
        }

        public void increasePoints() {
            this.points++;
        }

        public SpleefPlayer getPlayer() {
            return sp;
        }
        
        public void restoreOldData() {
            Player p = sp.getPlayer();
            p.setGameMode(oldGamemode);
            p.setFlying(false);
            p.getInventory().setContents(oldInventory);
        }
    }
}
