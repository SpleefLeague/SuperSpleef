/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.spleefleague.superspleef.game.team;

import com.google.common.collect.Lists;
import com.spleefleague.core.SpleefLeague;
import com.spleefleague.core.chat.ChatManager;
import com.spleefleague.core.chat.Theme;
import com.spleefleague.gameapi.events.BattleEndEvent;
import com.spleefleague.gameapi.events.BattleEndEvent.EndReason;
import com.spleefleague.core.player.SLPlayer;
import com.spleefleague.gameapi.queue.Battle;
import com.spleefleague.superspleef.SuperSpleef;
import com.spleefleague.superspleef.game.GameHistory;
import com.spleefleague.superspleef.game.GameHistoryPlayerData;
import com.spleefleague.superspleef.game.RemoveReason;
import com.spleefleague.superspleef.game.SpleefBattle;
import com.spleefleague.superspleef.player.SpleefPlayer;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

import java.util.*;
import java.util.Map.Entry;
import org.bukkit.craftbukkit.libs.org.apache.commons.lang3.time.DurationFormatUtils;

/**
 * @author Jonas
 */
public class TeamSpleefBattle extends SpleefBattle<TeamSpleefArena> {

    private static final Color[] colors = {
            Color.BLUE,
            Color.RED,
            Color.YELLOW,
            Color.GREEN,
            Color.PURPLE,
            Color.ORANGE
    };
    public static final ItemStack[] teamBlocks = {
            new ItemStack(Material.BLUE_WOOL),
            new ItemStack(Material.RED_WOOL),
            new ItemStack(Material.YELLOW_WOOL),
            new ItemStack(Material.GREEN_WOOL),
            new ItemStack(Material.PURPLE_WOOL),
            new ItemStack(Material.ORANGE_WOOL)
    };
    private static final ChatColor[] chatColors = {
            ChatColor.BLUE,
            ChatColor.RED,
            ChatColor.YELLOW,
            ChatColor.GREEN,
            ChatColor.LIGHT_PURPLE,
            ChatColor.GOLD
    };
    private static final ChatColor[] chatHighlightColors = {
            ChatColor.DARK_BLUE,
            ChatColor.DARK_RED,
            ChatColor.GOLD,
            ChatColor.DARK_GREEN,
            ChatColor.DARK_PURPLE,
            ChatColor.WHITE
    };
    public static final String[] names = {"Blue", "Red", "Yellow", "Green", "Purple", "Gold"};
    private Team[] teams;
    private Map<SpleefPlayer, Team> playerTeams;

    protected TeamSpleefBattle(TeamSpleefArena arena, List<SpleefPlayer> players) {
        super(arena, players);
    }

    @Override
    public void removePlayer(SpleefPlayer sp, RemoveReason reason) {
        if (reason == RemoveReason.QUIT) {
            for (SpleefPlayer pl : getActivePlayers()) {
                pl.sendMessage(
                        getSpleefMode().getChatPrefix() + " " + Theme.ERROR.buildTheme(false) + sp.getName() +
                        " has left the game!");
            }
            for (SpleefPlayer pl : getSpectators()) {
                pl.sendMessage(
                        getSpleefMode().getChatPrefix() + " " + Theme.ERROR.buildTheme(false) + sp.getName() +
                        " has left the game!");
            }
        }
        resetTeamColor(sp);
        handlePlayerDeath(sp, true);
        playerTeams.remove(sp);
        resetPlayer(sp);
        List<SpleefPlayer> activePlayers = getActivePlayers();
        if (activePlayers.size() == 1) {
            end(activePlayers.get(0), EndReason.valueOf(reason.name()));
        }else
            getPlayers().remove(sp);
    }

    @Override
    protected void addToBattleManager() {
        SuperSpleef.getInstance().getTeamSpleefBattleManager().add(this);
    }

    @Override
    protected void removeFromBattleManager() {
        SuperSpleef.getInstance().getTeamSpleefBattleManager().remove(this);
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
                SpleefPlayer p = getPlayers().get(pid);
                team.addPlayer(p);
                playerTeams.put(p, team);
                applyTeamColor(p, team, true);
            }
        }
        Scoreboard scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
        org.bukkit.scoreboard.Team scoreboardTeam = scoreboard.registerNewTeam("NO_COLLISION");
        scoreboardTeam.setOption(org.bukkit.scoreboard.Team.Option.COLLISION_RULE, org.bukkit.scoreboard.Team.OptionStatus.NEVER);
        Objective objective = scoreboard.registerNewObjective("rounds", "dummy");
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);
        objective.setDisplayName(ChatColor.GRAY + "00:00:00 | " + ChatColor.RED + "Score:");
        for (Team team : teams) {
            scoreboard.getObjective("rounds").getScore(team.getName()).setScore(0);
            for (SpleefPlayer sp : team.getAlivePlayers()) {
                sp.setScoreboard(scoreboard);
                scoreboardTeam.addEntry(sp.getName());
            }
        }
        objective.getScore(getPlayToString()).setScore(getPlayTo());
        setScoreboard(scoreboard);
        scoreboard.getTeams().forEach(t -> t.setOption(org.bukkit.scoreboard.Team.Option.COLLISION_RULE, org.bukkit.scoreboard.Team.OptionStatus.NEVER));
    }

    private String getPlayToString() {
        return ChatColor.GOLD + "Playing to: ";
    }

    @Override
    public void onScoreboardUpdate() {
        reInitScoreboard();
    }

    private void reInitScoreboard() {
        getScoreboard().getObjective("rounds").unregister();
        Objective objective = getScoreboard().registerNewObjective("rounds", "dummy");
        String s = DurationFormatUtils.formatDuration(getTicksPassed() * 50, "HH:mm:ss", true);
        objective.setDisplayName(ChatColor.GRAY.toString() + s + " | " + ChatColor.RED + "Score:");
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);
        objective.getScore(getPlayToString()).setScore(getPlayTo());
        Set<String> requestingReset = new HashSet();
        Set<String> requestingEnd = new HashSet();
        for (SpleefPlayer sp : this.getPlayers()) {
            Team t = this.playerTeams.get(sp);
            if (sp.isRequestingReset()) {
                requestingReset.add(t.getChatColor() + sp.getName());
            }
            if (sp.isRequestingEndgame()) {
                requestingEnd.add(t.getChatColor() + sp.getName());
            }
            getScoreboard().getObjective("rounds").getScore(t.getName()).setScore(t.getPoints());
        }
        if (!requestingEnd.isEmpty() || !requestingReset.isEmpty()) {
            objective.getScore(ChatColor.BLACK + "-----------").setScore(-1);
        }
        if (!requestingReset.isEmpty()) {
            objective.getScore(ChatColor.GOLD + "Reset requested").setScore(-2);
            for (String name : requestingReset) {
                objective.getScore("> " + name).setScore(-3);
            }
        }
        if (!requestingEnd.isEmpty()) {
            objective.getScore(ChatColor.RED + "End requested").setScore(-4);
            for (String name : requestingEnd) {
                objective.getScore("* " + name).setScore(-5);
            }
        }
    }

    @Override
    public ArrayList<SpleefPlayer> getAlivePlayers() {
        ArrayList<SpleefPlayer> pls = new ArrayList();
        for (Team t : this.teams) {
            pls.addAll(t.getAlivePlayers());
        }
        return pls;
    }

    @Override
    public void end(SpleefPlayer winner, EndReason reason) {
        end((Team) null, reason);
    }

    private void end(Team winner, EndReason reason) {
        SpleefPlayer winnerPlayer = playerTeams
                .entrySet()
                .stream()
                .filter(e -> e.getValue() == winner)
                .map(Entry::getKey)
                .findAny()
                .orElse(null);
        saveGameHistory(winnerPlayer, reason);
        if (reason == EndReason.CANCEL) {
            ChatManager.sendMessage(
                    getSpleefMode().getChatPrefix(),
                    Theme.INCOGNITO.buildTheme(false) + "The battle has been cancelled by a moderator.",
                    getGameChannel()
            );
        } else if (reason != EndReason.ENDGAME) {
            ChatManager.sendMessage(
                    getSpleefMode().getChatPrefix(),
                    Theme.INFO.buildTheme(false) + "Team " + winner.getName() + " has won the match.", getGameChannel()
            );
        }
        Lists.newArrayList(getSpectators()).forEach(this::resetPlayer);
        Lists.newArrayList(getActivePlayers()).forEach((p) -> {
            resetPlayer(p);
            resetTeamColor(p);
            p.invalidatePlayToRequest();
        });
        Bukkit.getPluginManager().callEvent(new BattleEndEvent(this, reason));

        String playerNames = "";
        List<SpleefPlayer> alivePlayers = getAllInTeam(winner);
        for (int i = 0; i < alivePlayers.size(); i++) {
            SpleefPlayer sp = alivePlayers.get(i);
            if (i == 0) {
                playerNames = ChatColor.RED + sp.getName();
            } else if (i == alivePlayers.size() - 1) {
                playerNames += ChatColor.GREEN + " and " + ChatColor.RED + sp.getName();
            } else {
                playerNames += ChatColor.GREEN + ", " + ChatColor.RED + sp.getName();
            }
        }
        String wStr = playerNames + ChatColor.GREEN + " have won!";
        if (winner == null) {
            wStr = "";
        }
        ChatManager.sendMessage(
                getSpleefMode().getChatPrefix(),
                ChatColor.GREEN + "Game in arena " + ChatColor.WHITE + getArena().getName() + ChatColor.GREEN +
                " is over. " + wStr, SuperSpleef.getInstance().getEndMessageChannel()
        );
        if(reason == EndReason.NORMAL) {
            applyRatingChange(winner);
        }
        cleanup();
    }
    
    @Override
    protected GameHistory modifyGameHistory(GameHistory history) {
        Optional<Team> winner = Arrays
                .stream(history.getPlayers())
                .filter(ghpd -> ghpd.isWinner())
                .map(ghpd -> ghpd.getPlayer())
                .findAny()
                .map(sp -> playerTeams.get(sp));
        if(winner.isPresent()) {
            Team team = winner.get();
            for(GameHistoryPlayerData ghpd : history.getPlayers()) {
                if(playerTeams.get(ghpd.getPlayer()) == team) {
                    ghpd.setWinner(true);
                }
            }
        }
        return history;
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
            if (!playerTeams.get(player).getAlivePlayers().contains(player)) {
                return;
            }
            playerTeams.get(player).getAlivePlayers().remove(player);
            if (team.getAlivePlayerCount() >= 1) {
                if (!leftGame) {
                    giveTempSpectator(player);
                    applyTeamColor(player, team, false);
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
                    for (Entry<SpleefPlayer, Team> e : this.playerTeams.entrySet()) {
                        if (e.getValue() == winner) {
                            getData(e.getKey()).increasePoints();
                        }
                    }
                    getScoreboard().getObjective("rounds").getScore(winner.getName()).setScore(winner.getPoints());
                    if (winner.getPoints() < getPlayTo()) {
                        setRound(getRound() + 1);
                        ChatManager.sendMessage(
                                getSpleefMode().getChatPrefix(),
                                Theme.INFO.buildTheme(false) + "Team " + winner.getName() + ChatColor.YELLOW +
                                " has won round " + getRound(), getGameChannel()
                        );
                        startRound();
                    } else {
                        end(winner, EndReason.NORMAL);
                    }
                } else {
                    if (!leftGame) {
                        giveTempSpectator(player);
                    }
                    ChatManager.sendMessage(
                            getSpleefMode().getChatPrefix(),
                            Theme.INFO.buildTheme(false) + "Team " + team.getName() + ChatColor.YELLOW + " died.",
                            getGameChannel()
                    );
                }
            }
        }
        reInitScoreboard();
    }

    @Override
    public void startRound() {
        for (Entry<SpleefPlayer, Team> entry : playerTeams.entrySet()) {
            SpleefPlayer sp = entry.getKey();
            entry.getValue().addPlayer(sp);
            sp.setDead(false);
            sp.setGameMode(GameMode.ADVENTURE);
            sp.getInventory().setArmorContents(entry.getValue().getArmor());
            applyTeamColor(sp, playerTeams.get(sp), true);
        }
        super.startRound();
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

    public List<SpleefPlayer> getAllInTeam(Team team) {
        ArrayList<SpleefPlayer> result = new ArrayList<>();
        playerTeams.forEach((SpleefPlayer slPlayer, Team slTeam) -> {
            if (slTeam.equals(team)) {
                result.add(slPlayer);
            }
        });
        return result;
    }

    public void resetTeamColor(SpleefPlayer p) {
        SLPlayer sp = SpleefLeague.getInstance().getPlayerManager().get(p);
        if (sp == null) {
            return;
        }
        sp.setTabName(null);
        sp.resetChatArrowColor();
    }

    public void applyTeamColor(SpleefPlayer p, Team t, boolean alive) {
        SLPlayer sp = SpleefLeague.getInstance().getPlayerManager().get(p);
        if (sp != null) {
            if (alive) {
                sp.setTabName(
                        t.getChatHighlightColor() + "[+ " +
                        t.getChatColor() + p.getName() +
                        t.getChatHighlightColor() + " +]"
                );
            } else {
                sp.setTabName(
                        t.getChatHighlightColor() + ChatColor.ITALIC.toString() + "[- " +
                        t.getChatColor() + ChatColor.ITALIC.toString() + p.getName() +
                        t.getChatHighlightColor() + ChatColor.ITALIC.toString() + " -]"
                );
            }
            sp.setChatArrowColor(t.getChatColor());
        }
    }
    

    @Override
    protected void applyRatingChange(SpleefPlayer winner) {
        applyRatingChange(playerTeams.get(winner));
    }
    
    private void applyRatingChange(Team winner) {
        double[] teamElo = new double[teams.length];
        for (int i = 0; i < teams.length; i++) {
            Team team = teams[i];
            teamElo[i] = playerTeams.entrySet()
                    .stream()
                    .filter(e -> e.getValue() == team)
                    .mapToInt(e -> e.getKey().getRating(this.getSpleefMode()))
                    .average()
                    .getAsDouble();
        }
        double[] teamRatingChanges = new double[teams.length];
        for (int i = 0; i < teamElo.length; i++) {
            for (int j = i + 1; j < teamElo.length; j++) {
                double t1 = teamElo[i];
                double t2 = teamElo[j];
                int compare = Integer.compare(teams[j].points, teams[i].points);
                double ratingChange = Battle.calculateEloRatingChange(t1, t2, compare);
                teamRatingChanges[i] += ratingChange;
                teamRatingChanges[j] -= ratingChange;
            }
        }
        for (int i = 0; i < teams.length; i++) {
            teamRatingChanges[i] /= teamRatingChanges.length;
            Team team = teams[i];
            int ratingChange = (int)Math.ceil(teamRatingChanges[i]);
            playerTeams
                    .entrySet()
                    .stream()
                    .filter(e -> e.getValue() == team)
                    .map(e -> e.getKey())
                    .forEach(sp -> {
                        sp.setRating(getSpleefMode(), sp.getRating(getSpleefMode()) + ratingChange);
                        sp.sendMessage(getSpleefMode().getChatPrefix() + ChatColor.GREEN + " You got " + ChatColor.WHITE + ratingChange + " points.");
                    });
        }
    }

    private class Team {

        private final Color color;
        private final ChatColor chatColor;
        private final ChatColor chatHighlightColor;
        private final Set<SpleefPlayer> alivePlayers;
        private final String name;
        private int points = 0;

        public Team(int id) {
            this.color = colors[id];
            this.chatColor = chatColors[id];
            this.chatHighlightColor = chatHighlightColors[id];
            this.alivePlayers = new HashSet<>();
            this.name = chatColor + names[id];
        }

        public Color getColor() {
            return color;
        }

        public ChatColor getChatColor() {
            return chatColor;
        }

        public ChatColor getChatHighlightColor() {
            return chatHighlightColor;
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
