/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.spleefleague.superspleef.game;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import net.spleefleague.core.SpleefLeague;
import net.spleefleague.core.chat.Theme;
import net.spleefleague.core.player.PlayerState;
import net.spleefleague.superspleef.SuperSpleef;
import net.spleefleague.superspleef.player.SpleefPlayer;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
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
    private final HashSet<Block> destroyedBlocks;
    private final HashMap<SpleefPlayer, PlayerData> data;
    private int ticksPassed = 0;
    private BukkitRunnable clock;
    private Scoreboard scoreboard;
    
    protected Battle(Arena arena, List<SpleefPlayer> players) {
        this.arena = arena;
        this.players = players;
        this.data = new HashMap<>();
        this.destroyedBlocks = new HashSet<>();
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

    public void removePlayer(SpleefPlayer sp) {
        removePlayer(sp, sp.getName() + " has left the game!");
    }
    
    public void removePlayer(SpleefPlayer sp, String message) {
        resetPlayer(sp);
        ArrayList<SpleefPlayer> activePlayers = getActivePlayers();
        if(activePlayers.size() == 1) {
            end(players.get(0));
        }
        else if(activePlayers.size() > 1) {   
            for(SpleefPlayer pl : activePlayers) {
                pl.getPlayer().sendMessage(SuperSpleef.getInstance().getPrefix() + " " + Theme.ERROR.buildTheme(false) + message);
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

    private void resetPlayer(SpleefPlayer sjp) {
        sjp.getPlayer().teleport(SpleefLeague.DEFAULT_WORLD.getSpawnLocation());
        sjp.getPlayer().setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
        sjp.setIngame(false);
        sjp.setFrozen(false);
        SpleefLeague.getInstance().getPlayerManager().get(sjp.getPlayer()).setState(PlayerState.IDLE);
    }

    public void end(SpleefPlayer winner) {
        end(winner, arena.isRated());
    }

    public void end(SpleefPlayer winner, boolean rated) {
        Bukkit.broadcastMessage("winner: " + winner.getName());
        clock.cancel();
        if (rated) {
            applyRatingChange();
        }
        for (SpleefPlayer sjp : getActivePlayers()) {
            resetPlayer(sjp);
        }
        resetField();
        SuperSpleef.getInstance().getBattleManager().remove(this);
    }
    
    private void resetField() {
        for(Block block : destroyedBlocks) {
            block.setType(Material.SNOW);
        }
    }

    public void cancel() {
        for (SpleefPlayer sp : getActivePlayers()) {
            sp.getPlayer().sendMessage(SuperSpleef.getInstance().getPrefix() + " " + Theme.INCOGNITO.buildTheme(false) + "Your battle has been cancelled by a moderator.");
            if (sp.isIngame()) {
                resetPlayer(sp);
            }
        }
    }
    
    public void onArenaLeave(SpleefPlayer player) {
        //Normal 1v1 Spleef
        //Game will end when a player has >= arena.getMaxRating()
        if(isNormalSpleef()) {
            for(SpleefPlayer sp : getActivePlayers()) {
                if(sp != player) {
                    PlayerData playerdata = this.data.get(sp);
                    playerdata.increasePoints();
                    scoreboard.getObjective("rounds").getScore(sp.getName()).setScore(playerdata.getPoints());
                    if(playerdata.getPoints() < arena.getMaxRating()) {
                        startRound();
                    }
                    else {
                        end(sp);
                    }
                }
            }
        }
        //More MultiSpleef like
        //Game will end when only one player is left
        else {
            removePlayer(player);
            for(SpleefPlayer sp : getActivePlayers()) {
                if(sp != player) {
                    PlayerData playerdata = this.data.get(sp);
                    playerdata.increasePoints();
                    scoreboard.getObjective("rounds").getScore(sp.getName()).setScore(playerdata.getPoints());
                }
            }
        }
    }

    public void start() {
        SuperSpleef.getInstance().getBattleManager().add(this);
        scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
        Objective objective = scoreboard.registerNewObjective("rounds", "dummy");
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);
        objective.setDisplayName(ChatColor.GRAY + "0:0:0 | " + ChatColor.RED + "Score:");
        for (int i = 0; i < players.size(); i++) {
            SpleefPlayer sp = players.get(i);
            sp.setIngame(true);
            sp.setFrozen(true);
            sp.getPlayer().teleport(arena.getSpawns()[i]);
            this.data.put(sp, new PlayerData(sp, arena.getSpawns()[i]));
            sp.getPlayer().setScoreboard(scoreboard);
            scoreboard.getObjective("rounds").getScore(sp.getName()).setScore(data.get(sp).getPoints());
            SpleefLeague.getInstance().getPlayerManager().get(sp.getPlayer()).setState(PlayerState.INGAME);
        }
        startClock();
        startRound();
    }

    public void startRound() {
        for(SpleefPlayer sp : getActivePlayers()) {
            Location spawn = this.data.get(sp).getSpawn();
            createSpawnCage(spawn);
            sp.setFrozen(true);
            sp.getPlayer().teleport(this.data.get(sp).getSpawn());
        }
        BukkitRunnable br = new BukkitRunnable() {
            private int secondsLeft = 3;

            @Override
            public void run() {
                if (secondsLeft > 0) {
                    for (SpleefPlayer sp : getActivePlayers()) {
                        sp.getPlayer().sendMessage(SuperSpleef.getInstance().getChatPrefix() + " " + secondsLeft + "...");
                    }
                    secondsLeft--;
                } else {
                    for (SpleefPlayer sp : getActivePlayers()) {
                        sp.getPlayer().sendMessage(SuperSpleef.getInstance().getChatPrefix() + " GO!");
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
            String s = DurationFormatUtils.formatDuration(ticksPassed * 50, "H:m:s", true);
            objective.setDisplayName(ChatColor.GRAY.toString() + s + " | " + ChatColor.RED + "Times Fallen:");
        }
    }

    private void startClock() {
        clock = new BukkitRunnable() {
            @Override
            public void run() {
                ticksPassed++;
                updateScoreboardTime();
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
                if (x == s.getX() && z == s.getZ()) {
                    w.getBlockAt(x, s.getBlockY() + 2, z).setType(type);
                } else {
                    for (int y = s.getBlockY(); y <= s.getBlockY() + 2; y++) {
                        w.getBlockAt(x, y, z).setType(type);
                    }
                }
            }
        }
    }
    
    public static void main(String[] args) {
        ArrayList<SpleefPlayer> players = new ArrayList<>();
        FakePlayer p1,p2,p3,p4,p5;
        p1 = new FakePlayer("A", 1000);
        p2 = new FakePlayer("B", 1200);
        p3 = new FakePlayer("C", 1400);
        p4 = new FakePlayer("D", 1600);
        p5 = new FakePlayer("E", 1800);
        players.add(p1);
        players.add(p2);
        players.add(p3);
        players.add(p4);
        players.add(p5);
        PlayerData pd1 = new PlayerData(p1, null);
        pd1.points = 2;
        PlayerData pd2 = new PlayerData(p2, null);
        pd2.points = 3;
        PlayerData pd3 = new PlayerData(p3, null);
        pd3.points = 5;
        PlayerData pd4 = new PlayerData(p4, null);
        pd4.points = 3;
        PlayerData pd5 = new PlayerData(p5, null);
        pd5.points = 1;
        Battle battle = new Battle(null, players);
        battle.data.put(p1, pd1);
        battle.data.put(p2, pd2);
        battle.data.put(p3, pd3);
        battle.data.put(p4, pd4);
        battle.data.put(p5, pd5);
        battle.applyRatingChange();
        for(SpleefPlayer p : battle.getPlayers()) {
            System.out.println(p.getName() + ": " + p.getRating());
        }
    }
    
    private static class FakePlayer extends SpleefPlayer {
        
        private final String name;
        
        public FakePlayer(String name, int rating) {
            super();
            this.name = name;
            this.setRating(rating);
        }
        
        @Override
        public String getName() {
            return name;
        }
    }

    private void applyRatingChange() {
        final int MIN_RATING = 1, MAX_RATING = 20;
        for (SpleefPlayer sp1 : players) {
            int newRating = sp1.getRating();
            for(SpleefPlayer sp2 : players) {
                if(sp1 != sp2) {
                    int points1 = this.data.get(sp1).getPoints();
                    int points2 = this.data.get(sp2).getPoints();
                    float elo = (float) (1f / (1f + Math.pow(2f, ((sp2.getRating() - sp1.getRating()) / 400f))));
                    int ratingChange;
                    if(points1 > points2) {
                        ratingChange = (int) Math.round(MAX_RATING * (1f - elo));
                    }
                    else if(points1 < points2) {
                        ratingChange = (int) Math.round(MAX_RATING * (0f - elo));
                    }
                    else {
                        ratingChange = (int) Math.round(MAX_RATING * (0.5f - elo));
                    }
                    if(ratingChange > 0 && ratingChange < MIN_RATING) {
                        ratingChange = MIN_RATING;
                    }
                    else if(ratingChange < 0 && ratingChange > -MIN_RATING) {
                        ratingChange = -MIN_RATING;
                    }
                    newRating += ratingChange;
                }
            }
            sp1.setRating(newRating);
        }
    }

    private static class PlayerData {

        private int points;
        private final Location spawn;
        private final SpleefPlayer sp;

        public PlayerData(SpleefPlayer sjp, Location spawn) {
            this.sp = sjp;
            this.spawn = spawn;
            this.points = 0;
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
    }
}
