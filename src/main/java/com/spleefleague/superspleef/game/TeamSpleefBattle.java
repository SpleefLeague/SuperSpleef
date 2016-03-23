/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.spleefleague.superspleef.game;

import com.spleefleague.core.chat.ChatManager;
import com.spleefleague.core.chat.Theme;
import com.spleefleague.core.events.BattleEndEvent;
import com.spleefleague.core.events.BattleEndEvent.EndReason;
import com.spleefleague.superspleef.SuperSpleef;
import com.spleefleague.superspleef.player.SpleefPlayer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

/**
 *
 * @author Jonas
 */
public class TeamSpleefBattle extends SpleefBattle {

    private static final Color[] colors = {Color.BLUE, Color.RED, Color.YELLOW, Color.GREEN, Color.PURPLE, Color.ORANGE};
    private static final ChatColor[] chatColors = {ChatColor.BLUE, ChatColor.RED, ChatColor.YELLOW, ChatColor.GREEN, ChatColor.LIGHT_PURPLE, ChatColor.GOLD};
    private static final String[] names = {"Blue", "Red", "Yellow", "Green", "Purple", "Gold"};
    private Team[] teams;
    private Map<SpleefPlayer, Team> playerTeams;
    private TeamSpleefArena arena;

    protected TeamSpleefBattle(TeamSpleefArena arena, List<SpleefPlayer> players) {
        super(arena, players);
        this.arena = arena;
    }

    @Override
    public TeamSpleefArena getArena() {
        return arena;
    }

    @Override
    public void removePlayer(SpleefPlayer sp, boolean surrender) {
        if (!surrender) {
            for (SpleefPlayer pl : getActivePlayers()) {
                pl.sendMessage(SuperSpleef.getInstance().getChatPrefix() + " " + Theme.ERROR.buildTheme(false) + sp.getName() + " has left the game!");
            }
            for (SpleefPlayer pl : getSpectators()) {
                pl.sendMessage(SuperSpleef.getInstance().getChatPrefix() + " " + Theme.ERROR.buildTheme(false) + sp.getName() + " has left the game!");
            }
        }
        handlePlayerDeath(sp, true);
        playerTeams.remove(sp);
        resetPlayer(sp);
        ArrayList<SpleefPlayer> activePlayers = getActivePlayers();
        if (activePlayers.size() == 1) {
            end(activePlayers.get(0), surrender ? EndReason.SURRENDER : EndReason.QUIT);
        }
    }

    @Override
    protected void onStart() {
        playerTeams = new HashMap<>();
        int pid = 0;
        teams = new Team[getArena().getTeamSizes().length];
        for (int teamID = 0; teamID < getArena().getTeamSizes().length; teamID++) {
            Team team = new Team(teamID);
            teams[teamID] = team;
            for (int i = 0; i < getArena().getTeamSizes()[teamID]; i++, pid++) {
                team.addPlayer(getPlayers().get(pid));
                playerTeams.put(getPlayers().get(pid), team);
            }
        }
        Scoreboard scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
        Objective objective = scoreboard.registerNewObjective("rounds", "dummy");
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);
        objective.setDisplayName(ChatColor.GRAY + "00:00:00 | " + ChatColor.RED + "Score:");
        for (Team team : teams) {
            scoreboard.getObjective("rounds").getScore(team.getName()).setScore(0);
            for (SpleefPlayer sp : team.getAlivePlayers()) {
                sp.setScoreboard(scoreboard);
                sp.getInventory().setArmorContents(team.getArmor());
            }
        }
        setScoreboard(scoreboard);
    }

    @Override
    public void end(SpleefPlayer winner, EndReason reason) {
        end((Team) null, reason);
    }

    private void end(Team winner, EndReason reason) {
        if (reason == EndReason.CANCEL) {
            ChatManager.sendMessage(SuperSpleef.getInstance().getChatPrefix(), Theme.INCOGNITO.buildTheme(false) + "The battle has been cancelled by a moderator.", getGameChannel());
        } else if (reason != EndReason.ENDGAME) {
            ChatManager.sendMessage(SuperSpleef.getInstance().getChatPrefix(), Theme.INFO.buildTheme(false) + "Team " + winner.getName() + " has won the match.", getGameChannel());
        }
        for (SpleefPlayer sp : new ArrayList<>(getSpectators())) {
            resetPlayer(sp);
        }
        for (SpleefPlayer sp : getActivePlayers()) {
            resetPlayer(sp);
        }
        Bukkit.getPluginManager().callEvent(new BattleEndEvent(this, reason));
        cleanup();
    }

    @Override
    public void onArenaLeave(SpleefPlayer player) {
        handlePlayerDeath(player, false);
    }

    private void handlePlayerDeath(SpleefPlayer player, boolean leftGame) {
        if (!leftGame && isInCountdown()) {
            player.teleport(getData(player).getSpawn());
        } else {
            Team team = playerTeams.get(player);
            playerTeams.get(player).getAlivePlayers().remove(player);
            if (team.getAlivePlayerCount() >= 1) {
                if (!leftGame) {
                    giveTempSpectator(player);
                }
            } else {
                Team winner = null;
                for (Team t : teams) {
                    if (team != t) {
                        if (t.isAlive()) {
                            if (winner == null) {
                                winner = t;
                            } else {
                                winner = null;
                                break;
                            }
                        }
                    }
                }
                if (winner != null) {
                    winner.increasePoints();
                    getScoreboard().getObjective("rounds").getScore(winner.getName()).setScore(winner.getPoints());
                    if (winner.getPoints() < getArena().getMaxRating()) {
                        setRound(getRound() + 1);
                        ChatManager.sendMessage(SuperSpleef.getInstance().getChatPrefix(), Theme.INFO.buildTheme(false) + "Team " + winner.getName() + ChatColor.YELLOW + " has won round " + getRound(), getGameChannel());
                        startRound();
                    } else {
                        end(winner, EndReason.NORMAL);
                    }
                } else {
                    if (!leftGame) {
                        giveTempSpectator(player);
                    }
                    ChatManager.sendMessage(SuperSpleef.getInstance().getChatPrefix(), Theme.INFO.buildTheme(false) + "Team " + team.getName() + ChatColor.YELLOW + " died.", getGameChannel());
                }
            }
        }
    }

    @Override
    public void startRound() {
        for (Entry<SpleefPlayer, Team> entry : playerTeams.entrySet()) {
            SpleefPlayer sp = entry.getKey();
            entry.getValue().addPlayer(sp);
            sp.setGameMode(GameMode.ADVENTURE);
        }
        super.startRound();
    }

    private void giveTempSpectator(SpleefPlayer sp) {
        Optional<SpleefPlayer> o = playerTeams.get(sp).getAlivePlayers().stream().findAny();
        Player target = null;
        if (o.isPresent()) {
            target = o.get().getPlayer();
        } else {
            for (Team team : teams) {
                if (team.isAlive()) {
                    target = team.getAlivePlayers().stream().findAny().get().getPlayer();
                }
            }
        }
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.getSpectatorTarget() != null && player.getSpectatorTarget().getUniqueId().equals(sp.getUniqueId())) {
                player.setSpectatorTarget(target);
            }
        }
        sp.setGameMode(GameMode.SPECTATOR);
        sp.setSpectatorTarget(target);
    }

    @Override
    protected void updateScoreboardTime() {
        if (getScoreboard() == null) {
            return;
        }
        Objective objective = getScoreboard().getObjective("rounds");
        if (objective != null) {
            String s = DurationFormatUtils.formatDuration(getTicksPassed() * 50, "HH:mm:ss", true);
            objective.setDisplayName(ChatColor.GRAY.toString() + s + " | " + ChatColor.RED + "Score:");
        }
    }

    private class Team {

        private final Color color;
        private final ChatColor chatColor;
        private final Set<SpleefPlayer> alivePlayers;
        private final String name;
        private int points = 0;

        public Team(int id) {
            this.color = colors[id];
            this.chatColor = chatColors[id];
            this.alivePlayers = new HashSet<>();
            this.name = chatColor + names[id];
        }

        public Color getColor() {
            return color;
        }

        public ChatColor getChatColor() {
            return chatColor;
        }

        public Set<SpleefPlayer> getAlivePlayers() {
            return alivePlayers;
        }

        public void addPlayer(SpleefPlayer sp) {
            alivePlayers.add(sp);
        }

        public boolean isAlive() {
            return !alivePlayers.isEmpty();
        }

        public int getAlivePlayerCount() {
            return alivePlayers.size();
        }

        public String getName() {
            return name;
        }

        public int getPoints() {
            return points;
        }

        public void increasePoints() {
            this.points++;
        }

        public ItemStack[] getArmor() {
            ItemStack i1 = new ItemStack(Material.LEATHER_BOOTS);
            LeatherArmorMeta meta = (LeatherArmorMeta) i1.getItemMeta();
            meta.setColor(color);
            i1.setItemMeta(meta);
            ItemStack i2 = new ItemStack(Material.LEATHER_LEGGINGS);
            meta = (LeatherArmorMeta) i2.getItemMeta();
            meta.setColor(color);
            i2.setItemMeta(meta);
            ItemStack i3 = new ItemStack(Material.LEATHER_CHESTPLATE);
            meta = (LeatherArmorMeta) i3.getItemMeta();
            meta.setColor(color);
            i3.setItemMeta(meta);
            return new ItemStack[]{i1, i2, i3, null};
        }
    }
}
