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
import com.spleefleague.core.listeners.FakeBlockHandler;
import com.spleefleague.core.player.GeneralPlayer;
import com.spleefleague.core.player.PlayerState;
import com.spleefleague.core.player.Rank;
import com.spleefleague.core.player.SLPlayer;
import com.spleefleague.core.plugin.GamePlugin;
import com.spleefleague.core.utils.FakeArea;
import com.spleefleague.core.utils.FakeBlock;
import com.spleefleague.superspleef.SuperSpleef;
import com.spleefleague.superspleef.game.signs.GameSign;
import com.spleefleague.superspleef.player.SpleefPlayer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
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
public class Battle implements com.spleefleague.core.queue.Battle<Arena, SpleefPlayer> {

    private final Arena arena;
    private final List<SpleefPlayer> players, spectators;
    private final Map<SpleefPlayer, PlayerData> data;
    private final ChatChannel cc;
    private int ticksPassed = 0;
    private BukkitRunnable clock;
    private Scoreboard scoreboard;
    private boolean inCountdown = false;
    private final SpleefMode spleefMode;
    private final FakeArea spawnCages, field;
    
    protected Battle(Arena arena, List<SpleefPlayer> players) {
        this(arena, players, arena.getSpleefMode());
    }
    
    protected Battle(Arena arena, List<SpleefPlayer> players, SpleefMode spleefMode) {
        this.spleefMode = spleefMode;
        this.arena = arena;
        this.players = players;
        this.spectators = new ArrayList<>();
        this.data = new LinkedHashMap<>();
        this.spawnCages = new FakeArea();
        this.field = new FakeArea();
        for(Block block : arena.getField().getBlocks()) {
            this.field.addBlock(new FakeBlock(block.getLocation(), Material.SNOW_BLOCK));
        }
        this.cc = ChatChannel.createTemporaryChannel("GAMECHANNEL" + this.hashCode(), null, Rank.DEFAULT, false, false);
    }

    @Override
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
        Location spawn = arena.getSpectatorSpawn();
        if (spawn != null) {
            sp.teleport(spawn);
        }
        sp.setScoreboard(scoreboard);
        sp.sendMessage(Theme.INCOGNITO + "You are now spectating the battle on " + ChatColor.GREEN + arena.getName());
        FakeBlockHandler.addArea(spawnCages, sp.getPlayer());
        FakeBlockHandler.addArea(field, false, sp.getPlayer());
        FakeBlockHandler.removeArea(arena.getDefaultSnow(), false, sp.getPlayer());
        SLPlayer slp = SpleefLeague.getInstance().getPlayerManager().get(sp.getPlayer());
        slp.setState(PlayerState.SPECTATING);
        slp.addChatChannel(cc);
        for(SpleefPlayer spl : getActivePlayers()) {
            spl.showPlayer(sp.getPlayer());
            sp.showPlayer(spl.getPlayer());
        }
        for(SpleefPlayer spl : spectators) {
            spl.showPlayer(sp);
            sp.showPlayer(spl);
        }
        spectators.add(sp);
        hidePlayers(sp);
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
        if (activePlayers.size() == 1) {
            end(activePlayers.get(0));
        }
        else if (activePlayers.size() > 1) {
            for (SpleefPlayer pl : activePlayers) {
                pl.sendMessage(SuperSpleef.getInstance().getChatPrefix() + " " + Theme.ERROR.buildTheme(false) + message);
            }
            if (spleefMode == SpleefMode.MULTI) {
                players.remove(sp);
                scoreboard.getObjective("rounds").getScore(ChatColor.GREEN + "Players:").setScore(activePlayers.size());
            }
        }
    }

    @Override
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
        FakeBlockHandler.removeArea(spawnCages, slp.getPlayer());
        FakeBlockHandler.removeArea(field, false, slp.getPlayer());
        FakeBlockHandler.addArea(arena.getDefaultSnow(), false, slp.getPlayer());
        if (spectators.contains(sp)) {
            spectators.remove(sp);
        }
        else {
            sp.setIngame(false);
            sp.setFrozen(false);
            sp.setRequestingReset(false);
            sp.setRequestingEndgame(false);
            sp.closeInventory();
            data.get(sp).restoreOldData();
        }
        sp.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
        sp.teleport(SpleefLeague.getInstance().getSpawnLocation());
        slp.removeChatChannel(cc);
        slp.setState(PlayerState.IDLE);
        slp.resetVisibility();
    }

    public void end(SpleefPlayer winner) {
        end(winner, arena.isRated());
    }

    public void end(SpleefPlayer winner, boolean rated) {
        saveGameHistory(winner);
        if (spleefMode == SpleefMode.MULTI) {
            ChatManager.sendMessage(SuperSpleef.getInstance().getChatPrefix(), ChatColor.RED + winner.getName() + ChatColor.GREEN + " won the MultiSpleef battle on " + ChatColor.WHITE + arena.getName() + ChatColor.GREEN + "!", SuperSpleef.getInstance().getEndMessageChannel());
        }
        if (rated) {
            applyRatingChange(winner);
        }
        for (SpleefPlayer sp : getActivePlayers()) {
            resetPlayer(sp);
        }
        for (SpleefPlayer sp : new ArrayList<>(spectators)) {
            resetPlayer(sp);
        }
        cleanup();
    }

    private void cleanup() {
        clock.cancel();
        resetField();
        arena.registerGameEnd();
        SuperSpleef.getInstance().getBattleManager().remove(this);
        ChatManager.unregisterChannel(cc);
        GameSign.updateGameSigns(arena);
    }

    public void resetField() {
        for(FakeBlock blocks : field.getBlocks()) {
            blocks.setType(Material.SNOW_BLOCK);
        }
        FakeBlockHandler.update(field);
    }

    public void cancel() {
        cancel(true);
    }

    public void cancel(boolean sendMessage) {
        saveGameHistory(null);
        if (sendMessage) {
            ChatManager.sendMessage(SuperSpleef.getInstance().getChatPrefix(), Theme.INCOGNITO.buildTheme(false) + "The battle has been cancelled by a moderator.", cc);
        }
        for (SpleefPlayer sp : new ArrayList<>(spectators)) {
            resetPlayer(sp);
        }
        for (SpleefPlayer sp : getActivePlayers()) {
            resetPlayer(sp);
        }
        cleanup();
    }

    public void onArenaLeave(SpleefPlayer player) {
        if (isInCountdown()) {
            player.teleport(data.get(player).getSpawn());
        }
        else {
            if (spleefMode == SpleefMode.NORMAL) {
                for (SpleefPlayer sp : getActivePlayers()) {
                    if (sp != player) {
                        PlayerData playerdata = this.data.get(sp);
                        playerdata.increasePoints();
                        scoreboard.getObjective("rounds").getScore(sp.getName()).setScore(playerdata.getPoints());
                        if (playerdata.getPoints() < arena.getMaxRating()) {
                            int round = 0;
                            for (PlayerData pd : data.values()) {
                                round += pd.getPoints();
                            }
                            ChatManager.sendMessage(SuperSpleef.getInstance().getChatPrefix(), Theme.INFO.buildTheme(false) + sp.getName() + " has won round " + round, cc);
                            startRound();
                        }
                        else {
                            end(sp);
                        }
                    }
                }
            }
            else if (spleefMode == SpleefMode.MULTI) {
                removePlayer(player);
                for (SpleefPlayer sp : getActivePlayers()) {
                    if (sp != player) {
                        PlayerData playerdata = this.data.get(sp);
                        playerdata.increasePoints();
                    }
                }
            }
            else {
                this.cancel(false);
                return;
            }
            if (arena.getScoreboards() != null) {
                int[] score = new int[arena.getSize()];
                int i = 0;
                for (PlayerData pd : data.values()) {
                    score[i++] = pd.getPoints();
                }
                for(com.spleefleague.superspleef.game.scoreboards.Scoreboard scoreboard : arena.getScoreboards()) {
                    scoreboard.setScore(score);
                }
            }
        }
    }

    public void start() {
        arena.registerGameStart();
        if(arena.getScoreboards() != null) {
            for(com.spleefleague.superspleef.game.scoreboards.Scoreboard scoreboard : arena.getScoreboards()) {
                scoreboard.setScore(new int[arena.getSize()]);
            }
        }
        GameSign.updateGameSigns(arena);
        ChatManager.registerChannel(cc);
        SuperSpleef.getInstance().getBattleManager().add(this);
        scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
        Objective objective = scoreboard.registerNewObjective("rounds", "dummy");
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);
        if (spleefMode == SpleefMode.MULTI) {
            objective.setDisplayName(ChatColor.GRAY + "00:00:00 | " + ChatColor.RED + "Players:");
        }
        else {
            objective.setDisplayName(ChatColor.GRAY + "00:00:00 | " + ChatColor.RED + "Score:");
        }
        String playerNames = "";
        FakeBlockHandler.addArea(spawnCages, GeneralPlayer.toBukkitPlayer(players.toArray(new SpleefPlayer[players.size()])));
        FakeBlockHandler.addArea(field, false, GeneralPlayer.toBukkitPlayer(players.toArray(new SpleefPlayer[players.size()])));
        FakeBlockHandler.removeArea(arena.getDefaultSnow(), false, GeneralPlayer.toBukkitPlayer(players.toArray(new SpleefPlayer[players.size()])));
        
        for (int i = 0; i < players.size(); i++) {
            SpleefPlayer sp = players.get(i);
            if (i == 0) {
                playerNames = ChatColor.RED + sp.getName();
            }
            else if (i == players.size() - 1) {
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
            p.closeInventory();
            p.getInventory().clear();
            p.getInventory().addItem(new ItemStack(Material.DIAMOND_SPADE));
            for (PotionEffect effect : p.getActivePotionEffects()) {
                p.removePotionEffect(effect.getType());
            }
            for (SpleefPlayer sp1 : players) {
                if (sp != sp1) {
                    sp.showPlayer(sp1.getPlayer());
                }
            }
            p.setFlying(false);
            p.setAllowFlight(false);
            slp.addChatChannel(cc);
            if (spleefMode == SpleefMode.NORMAL) {
                scoreboard.getObjective("rounds").getScore(sp.getName()).setScore(data.get(sp).getPoints());
            }
            slp.setState(PlayerState.INGAME);
        }
        if (spleefMode == SpleefMode.MULTI) {
            scoreboard.getObjective("rounds").getScore(ChatColor.GREEN + "Players:").setScore(getActivePlayers().size());
        }
        ChatManager.sendMessage(SuperSpleef.getInstance().getChatPrefix(), Theme.SUCCESS.buildTheme(false) + "Beginning match on " + ChatColor.WHITE + arena.getName() + ChatColor.GREEN + " between " + ChatColor.RED + playerNames + ChatColor.GREEN + "!", SuperSpleef.getInstance().getStartMessageChannel());
        for (int i = 0; i < players.size(); i++) {
            SpleefPlayer sp = players.get(i);
            sp.teleport(arena.getSpawns()[i]);
        }
        hidePlayers();
        startClock();
        startRound();
    }
    
    private void hidePlayers() {
        List<SpleefPlayer> battlePlayers = getActivePlayers();
        battlePlayers.addAll(spectators);
        for(SpleefPlayer sjp : SuperSpleef.getInstance().getPlayerManager().getAll()) {
            hidePlayers(sjp);
        }
    }
    
    private void hidePlayers(SpleefPlayer target) {
        List<SpleefPlayer> battlePlayers = getActivePlayers();
        battlePlayers.addAll(spectators);
        for(SpleefPlayer active : battlePlayers) {
            if(!battlePlayers.contains(target)) {
                target.hidePlayer(active.getPlayer());
                active.hidePlayer(target.getPlayer());
            }
        }
    }

    public void startRound() {
        inCountdown = true;
        resetField();
        createSpawnCages();
        for (SpleefPlayer sp : getActivePlayers()) {
            sp.setFrozen(true);
            sp.setRequestingReset(false);
            sp.setRequestingEndgame(false);
            sp.setFireTicks(0);
            sp.teleport(this.data.get(sp).getSpawn());
        }
        BukkitRunnable br = new BukkitRunnable() {
            private int secondsLeft = 3;

            @Override
            public void run() {
                if (secondsLeft > 0) {
                    ChatManager.sendMessage(SuperSpleef.getInstance().getChatPrefix(), secondsLeft + "...", cc);
                    secondsLeft--;
                }
                else {
                    ChatManager.sendMessage(SuperSpleef.getInstance().getChatPrefix(), "GO!", cc);
                    for (SpleefPlayer sp : getActivePlayers()) {
                        sp.setFrozen(false);
                    }
                    onDone();
                    super.cancel();
                }
            }

            public void onDone() {
                for (SpleefPlayer sp : getActivePlayers()) {
                    sp.setFrozen(false);
                }
                removeSpawnCages();
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
            if (spleefMode == SpleefMode.MULTI) {
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
                if (!isInCountdown()) {
                    ticksPassed++;
                    updateScoreboardTime();
                }
            }
        };
        clock.runTaskTimer(SuperSpleef.getInstance(), 0, 1);
    }

    private void createSpawnCages() {
        spawnCages.clear();
        spawnCages.add(getSpawnCageBlocks(Material.GLASS));
        FakeBlockHandler.update(spawnCages);
    }
    
    private void removeSpawnCages() {
        spawnCages.clear();
        spawnCages.add(getSpawnCageBlocks(Material.AIR));
        FakeBlockHandler.update(spawnCages);
    }
    
    private FakeArea getSpawnCageBlocks(Material m) {
        FakeArea area = new FakeArea();
        for(Location spawn : arena.getSpawns()) {
            area.add(getCageBlocks(spawn, m));
        }
        return area;
    }
    
    private FakeArea getCageBlocks(Location loc, Material m) {
        loc = loc.getBlock().getLocation();
        FakeArea area = new FakeArea();
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                if (x == 0 && z == 0) {
                    area.addBlock(new FakeBlock(loc.clone().add(x, 2, z), m));
                } else {
                    for (int y = 0; y <= 2; y++) {
                        area.addBlock(new FakeBlock(loc.clone().add(x, y, z), m));
                    }
                }
            }
        }
        return area;
    }

    public boolean isInCountdown() {
        return inCountdown;
    }
    
    public FakeArea getField() {
        return field;
    }

    private void applyRatingChange(SpleefPlayer winner) {
        if (spleefMode == SpleefMode.NORMAL) {
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
            ChatManager.sendMessage(SuperSpleef.getInstance().getChatPrefix(), ChatColor.GREEN + "Game in arena " + ChatColor.WHITE + arena.getName() + ChatColor.GREEN + " is over. " + playerList, SuperSpleef.getInstance().getEndMessageChannel());
        }
//        else if (spleefMode == SpleefMode.MULTI) {
//            
//        }
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
