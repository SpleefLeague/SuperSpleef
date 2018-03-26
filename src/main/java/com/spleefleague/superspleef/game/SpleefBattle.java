/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.spleefleague.superspleef.game;

import com.comphenix.packetwrapper.WrapperPlayServerPlayerInfo;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.comphenix.protocol.wrappers.PlayerInfoData;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import com.comphenix.protocol.wrappers.WrappedGameProfile;
import com.google.common.collect.Lists;
import com.spleefleague.core.SpleefLeague;
import com.spleefleague.core.chat.ChatChannel;
import com.spleefleague.core.chat.ChatManager;
import com.spleefleague.core.chat.Theme;
import com.spleefleague.core.events.BattleEndEvent;
import com.spleefleague.core.events.BattleEndEvent.EndReason;
import com.spleefleague.core.events.BattleStartEvent;
import com.spleefleague.core.events.BattleStartEvent.StartReason;
import com.spleefleague.core.player.PlayerState;
import com.spleefleague.core.player.Rank;
import com.spleefleague.core.player.SLPlayer;
import com.spleefleague.core.plugin.GamePlugin;
import com.spleefleague.core.queue.Battle;
import com.spleefleague.core.utils.Area;
import com.spleefleague.entitybuilder.EntityBuilder;
import com.spleefleague.superspleef.SuperSpleef;
import com.spleefleague.superspleef.game.cosmetics.Shovel;
import com.spleefleague.superspleef.player.SpleefPlayer;
import com.spleefleague.virtualworld.VirtualWorld;
import com.spleefleague.virtualworld.api.FakeBlock;
import com.spleefleague.virtualworld.api.FakeWorld;
import net.minecraft.server.v1_12_R1.NBTTagCompound;
import net.minecraft.server.v1_12_R1.NBTTagList;
import net.minecraft.server.v1_12_R1.NBTTagString;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.util.Vector;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_12_R1.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Scoreboard;

import java.util.*;
import java.util.stream.Collectors;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Team;

/**
 *
 * @author Jonas
 */
public abstract class SpleefBattle<A extends Arena> implements Battle<A, SpleefPlayer> {

    private static final int FAKE_WORLD_PRIORITY = 10;
    private final A arena;
    private final List<SpleefPlayer> players, spectators;
    private final Map<SpleefPlayer, PlayerData> data;
    private final ChatChannel cc;
    private int ticksPassed = 0, round = 0, pointsCap = 0;
    private BukkitRunnable clock;
    private Scoreboard scoreboard;
    private boolean inCountdown = false;
    private final SpleefMode spleefMode;
    private final FakeWorld fakeWorld;
    private final Collection<FakeBlock> fieldBlocks;
    private final Collection<Vector> spawnCageDefinition;
    
    protected SpleefBattle(A arena, List<SpleefPlayer> players) {
        this(arena, players, arena.getSpleefMode());
    }

    private SpleefBattle(A arena, List<SpleefPlayer> players, SpleefMode spleefMode) {
        this.spleefMode = spleefMode;
        this.arena = arena;
        this.players = players;
        this.spectators = new ArrayList<>();
        this.data = new LinkedHashMap<>();
        this.spawnCageDefinition = generateSpawnCageDefinition();
        this.fakeWorld = VirtualWorld.getInstance().getFakeWorldManager().createWorld(arena.getSpawns()[0].getWorld());
        this.fieldBlocks = new HashSet<>();
        for (Area f : arena.getField().getAreas()) {
            for (Block block : f.getBlocks()) {
                FakeBlock fb = this.fakeWorld.getBlockAt(block.getLocation());
                fb.setType(Material.SNOW_BLOCK);
                this.fieldBlocks.add(fb);
            }
        }
        this.cc = ChatChannel.createTemporaryChannel("GAMECHANNEL" + this.hashCode(), null, Rank.DEFAULT, false, false);
        this.pointsCap = this.arena.getMaxRating();
    }
    
    protected abstract void addToBattleManager();
    protected abstract void removeFromBattleManager();
    
    
    public void removePlayer(SpleefPlayer sp, RemoveReason reason) {
        if (reason == RemoveReason.QUIT) {
            for (SpleefPlayer pl : getActivePlayers()) {
                pl.sendMessage(spleefMode.getChatPrefix() + " " + Theme.ERROR.buildTheme(false) + sp.getName() + " has left the game!");
            }
            for (SpleefPlayer pl : getSpectators()) {
                pl.sendMessage(spleefMode.getChatPrefix() + " " + Theme.ERROR.buildTheme(false) + sp.getName() + " has left the game!");
            }
        }
        resetPlayer(sp);
        List<SpleefPlayer> activePlayers = getActivePlayers();
        if (activePlayers.size() == 1) {
            end(activePlayers.get(0), EndReason.valueOf(reason.name()));
        } else {
            getPlayers().remove(sp);
        }
    }
    
    private String getPlayToString() {
        return ChatColor.GOLD + "Playing to: ";
    }
    
    public void onScoreboardUpdate() {
        reInitScoreboard();
    }
    
    private void reInitScoreboard() {
        if(getScoreboard() == null) return;
        getScoreboard().getObjective("rounds").unregister();
        Objective objective = getScoreboard().registerNewObjective("rounds", "dummy");
        String s = DurationFormatUtils.formatDuration(getTicksPassed() * 50, "HH:mm:ss", true);
        objective.setDisplayName(ChatColor.GRAY.toString() + s + " | " + ChatColor.RED + "Score:");
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);
        objective.getScore(getPlayToString()).setScore(getPlayTo());
        Set<String> requestingReset = new HashSet();
        Set<String> requestingEnd = new HashSet();
        for (SpleefPlayer sp : this.getPlayers()) {
            if (sp.isRequestingReset()) {
                requestingReset.add(sp.getName());
            }
            if (sp.isRequestingEndgame()) {
                requestingEnd.add(sp.getName());
            }
            objective.getScore(sp.getName()).setScore(getData(sp).getPoints());
        }
        if (!requestingEnd.isEmpty() || !requestingReset.isEmpty()) {
            objective.getScore(ChatColor.BLACK + "-----------").setScore(-1);
        }
        if (!requestingReset.isEmpty()) {
            objective.getScore(ChatColor.GOLD + "Reset requested").setScore(-2);
            for (String name : requestingReset) {
                objective.getScore(ChatColor.LIGHT_PURPLE + "> " + name).setScore(-3);
            }
        }
        if (!requestingEnd.isEmpty()) {
            objective.getScore(ChatColor.RED + "End requested").setScore(-4);
            for (String name : requestingEnd) {
                objective.getScore(ChatColor.AQUA + "> " + name).setScore(-5);
            }
        }
    }
    
    protected void onStart() {
        Scoreboard scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
        Team team = scoreboard.registerNewTeam("NO_COLLISION");
        team.setOption(Team.Option.COLLISION_RULE, Team.OptionStatus.NEVER);
        Objective objective = scoreboard.registerNewObjective("rounds", "dummy");
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);
        objective.setDisplayName(ChatColor.GRAY + "00:00:00 | " + ChatColor.RED + "Score:");
        for (SpleefPlayer sp : getPlayers()) {
            objective.getScore(sp.getName()).setScore(0);
            sp.setScoreboard(scoreboard);
            team.addEntry(sp.getName());
        }
        objective.getScore(getPlayToString()).setScore(getPlayTo());
        setScoreboard(scoreboard);        
    }
    
    public void end(SpleefPlayer winner, EndReason reason) {
        saveGameHistory(winner, reason);
        if (reason == EndReason.CANCEL) {
            if (reason == EndReason.CANCEL) {
                ChatManager.sendMessage(spleefMode.getChatPrefix(), Theme.INCOGNITO.buildTheme(false) + "The battle has been cancelled by a moderator.", getGameChannel());
            }
        } else if (reason != EndReason.ENDGAME) {
            if (getArena().isRated()) {
                applyRatingChange(winner);
            }
            applyCoinChange(winner);
        }
        if(!this.getArena().isRated() && winner != null) {
            ChatManager.sendMessage(spleefMode.getChatPrefix(), ChatColor.RED + winner.getName() + Theme.INFO.buildTheme(false) + " has won the match!", getGameChannel());
        }
        Lists.newArrayList(getSpectators()).forEach(this::resetPlayer);
        List<SpleefPlayer> active = getActivePlayers();//Change order -> preserve inventory if match is started with one player in multiple slots
        for (int i = active.size() - 1; i >= 0; i--) {
            resetPlayer(active.get(i));
        }
        Lists.newArrayList(getActivePlayers()).forEach(this::resetPlayer);
        Bukkit.getPluginManager().callEvent(new BattleEndEvent(this, reason));
        cleanup();
    }
    
    public void onArenaLeave(SpleefPlayer player) {
        if(player.isDead()) {
            return;
        }
        if (isInCountdown()) {
            player.teleport(getData(player).getSpawn());
        } 
        else {
            player.setDead(true);
            int maxScore = 0;
            SpleefPlayer winner = null;
            List<SpleefPlayer> alive = getActivePlayers().stream().filter(sp -> !sp.isDead()).collect(Collectors.toList());
            for (SpleefPlayer sp : getActivePlayers()) {
                PlayerData playerdata = getData(sp);
                if (!sp.isDead()) {
                    playerdata.increasePoints();
                    getScoreboard().getObjective("rounds").getScore(sp.getName()).setScore(playerdata.getPoints());
                }
                if (playerdata.getPoints() >= getPlayTo()) {
                        if (maxScore == playerdata.getPoints()) {
                            winner = null;
                        } else if (maxScore < playerdata.getPoints()) {                        
                            maxScore = playerdata.getPoints();
                            winner = sp;
                        }
                    }
            }
            if(winner == null && maxScore >= this.getPlayTo()) {
                changePointsCup(maxScore + 1);
            }
            if (alive.size() == 1 && winner != null) {
                end(winner, EndReason.NORMAL);
            } else {
                if (alive.size() == 1) {
                    setRound(getRound() + 1);
                    ChatManager.sendMessage(spleefMode.getChatPrefix(), Theme.INFO.buildTheme(false) + alive.get(0).getName() + " has won round " + getRound(), getGameChannel());
                    startRound();
                }
            }
            if (getArena().getScoreboards() != null) {
                int[] score = new int[getArena().getSize()];
                int i = 0;
                for (SpleefPlayer sp : getActivePlayers()) {
                    score[i++] = getData(sp).getPoints();
                }
                for (com.spleefleague.superspleef.game.scoreboards.Scoreboard scoreboard : getArena().getScoreboards()) {
                    scoreboard.setScore(score);
                }
            }
        }
        reInitScoreboard();
    }
    
    protected void giveTempSpectator(SpleefPlayer sp, Player target) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.getSpectatorTarget() != null &&
                player.getSpectatorTarget().getUniqueId().equals(sp.getUniqueId())) {
                player.setSpectatorTarget(target);
            }
        }
        sp.getInventory().setArmorContents(new ItemStack[4]);
        sp.setGameMode(GameMode.SPECTATOR);
        sp.setSpectatorTarget(target);
    }
    
    protected void updateScoreboardTime() {
        if (getScoreboard() != null) {
            Objective objective = getScoreboard().getObjective("rounds");
            if (objective != null) {
                String s = DurationFormatUtils.formatDuration(getTicksPassed() * 50, "HH:mm:ss", true);
                objective.setDisplayName(ChatColor.GRAY.toString() + s + " | " + ChatColor.RED + "Score:");
            }
        }
    }
    
    protected void applyCoinChange(SpleefPlayer winner) {
        int maxPoints = getPlayers()
                .stream()
                .map(sp -> getData(sp))
                .mapToInt(PlayerData::getPoints)
                .max()
                .orElse(0);
        if(maxPoints < 5) return;
        for(SpleefPlayer sp : getPlayers()) {
            int coins = 2;
            if(sp == winner) {
                coins = 5;
            }
            SpleefLeague.getInstance().getPlayerManager().get(sp).changeCoins(coins);
        }
    }
    
    protected abstract void applyRatingChange(SpleefPlayer winner);
    
    @Override
    public A getArena() {
        return arena;
    }
    
    public FakeWorld getFakeWorld() {
        return fakeWorld;
    }
    
    public Collection<FakeBlock> getFieldBlocks() {
        return fieldBlocks;
    }
    
    public Collection<Vector> getCageDefinition() {
        return spawnCageDefinition;
    }
    
    public SpleefMode getSpleefMode() {
        return spleefMode;
    }

    protected Scoreboard getScoreboard() {
        return scoreboard;
    }

    protected void setScoreboard(Scoreboard scoreboard) {
        this.scoreboard = scoreboard;
    }

    public ChatChannel getGameChannel() {
        return cc;
    }

    public int getRound() {
        return round;
    }

    public void setRound(int round) {
        this.round = round;
    }

    public int getTicksPassed() {
        return ticksPassed;
    }

    @Override
    public List<SpleefPlayer> getPlayers() {
        return players;
    }

    @Override
    public Collection<SpleefPlayer> getSpectators() {
        return spectators;
    }

    public boolean isNormalSpleef() {
        return players.size() == 2;
    }

    public int getPlayTo() {
        return this.pointsCap;
    }
    
    public void addSpectator(SpleefPlayer sp) {
        Location spawn = arena.getSpectatorSpawn();
        if (spawn != null) {
            sp.teleport(spawn);
        }
        sp.setScoreboard(scoreboard);
        sp.sendMessage(Theme.INCOGNITO + "You are now spectating the battle on " + ChatColor.GREEN + arena.getName());
        VirtualWorld.getInstance().getFakeWorldManager().addWorld(sp.getPlayer(), fakeWorld, FAKE_WORLD_PRIORITY);
        SLPlayer slp = SpleefLeague.getInstance().getPlayerManager().get(sp.getPlayer());
        slp.setState(PlayerState.SPECTATING);
        slp.addChatChannel(cc);
        for (SpleefPlayer spl : getActivePlayers()) {
            spl.showPlayer(sp.getPlayer());
            sp.showPlayer(spl.getPlayer());
        }
        for (SpleefPlayer spl : spectators) {
            spl.showPlayer(sp.getPlayer());
            sp.showPlayer(spl.getPlayer());
        }
        spectators.add(sp);
        setVisibility(sp);
    }

    public boolean isSpectating(SpleefPlayer sjp) {
        return spectators.contains(sjp);
    }

    public void removeSpectator(SpleefPlayer sp) {
        List<Player> ingamePlayers = new ArrayList<>();
        for (SpleefPlayer p : getActivePlayers()) {
            sp.getPlayer().hidePlayer(p.getPlayer());
            p.getPlayer().hidePlayer(sp.getPlayer());
            ingamePlayers.add(p.getPlayer());
        }
        Bukkit.getScheduler().runTaskLater(SuperSpleef.getInstance(), () -> {
            if(sp.getPlayer() == null) {
                return;
            }
            List<PlayerInfoData> list = new ArrayList<>();
            SpleefLeague.getInstance().getPlayerManager().getAll().forEach((SLPlayer slPlayer) -> list.add(new PlayerInfoData(WrappedGameProfile.fromPlayer(slPlayer.getPlayer()), ((CraftPlayer) slPlayer.getPlayer()).getHandle().ping, EnumWrappers.NativeGameMode.SURVIVAL, WrappedChatComponent.fromText(slPlayer.getRank().getColor() + slPlayer.getName()))));
            WrapperPlayServerPlayerInfo packet = new WrapperPlayServerPlayerInfo();
            packet.setAction(EnumWrappers.PlayerInfoAction.ADD_PLAYER);
            packet.setData(list);
            ingamePlayers.forEach((Player p) -> packet.sendPacket(p));

            list.clear();
            ingamePlayers.forEach((Player p) -> {
                SLPlayer generalPlayer = SpleefLeague.getInstance().getPlayerManager().get(p);
                list.add(new PlayerInfoData(WrappedGameProfile.fromPlayer(p), ((CraftPlayer) p).getHandle().ping, EnumWrappers.NativeGameMode.SURVIVAL, WrappedChatComponent.fromText(generalPlayer.getRank().getColor() + generalPlayer.getName())));
            });
            packet.setData(list);
            packet.sendPacket(sp.getPlayer());
        }, 10);
        resetPlayer(sp);
    }

    public List<SpleefPlayer> getAlivePlayers() {
        return getActivePlayers()
                .stream()
                .filter(sp -> !sp.isDead())
                .collect(Collectors.toList());
    }

    @Override
    public List<SpleefPlayer> getActivePlayers() {
        return players
                .stream()
                .filter(SpleefPlayer::isIngame)
                .collect(Collectors.toList());
    }

    protected void resetPlayer(SpleefPlayer sp) {
        SLPlayer slp = SpleefLeague.getInstance().getPlayerManager().get(sp.getPlayer());
        VirtualWorld.getInstance().getFakeWorldManager().removeWorld(sp.getPlayer(), fakeWorld);
        if (spectators.contains(sp)) {
            spectators.remove(sp);
        } else {
            sp.setIngame(false);
            sp.setDead(false);
            sp.setFrozen(false);
            sp.setRequestingReset(false);
            sp.setRequestingEndgame(false);
            sp.invalidatePlayToRequest();
            sp.closeInventory();
            data.get(sp).restoreOldData();
        }
        if (sp.getPlayer().getGameMode() == GameMode.SPECTATOR) {
            sp.getPlayer().setSpectatorTarget(null);
        }
        sp.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
        sp.teleport(SpleefLeague.getInstance().getSpawnManager().getNext().getLocation());
        slp.removeChatChannel(cc);
        slp.setState(PlayerState.IDLE);
        slp.resetVisibility();
    }

    public void cleanup() {
        clock.cancel();
        resetField();
        arena.registerGameEnd();
        ChatManager.unregisterChannel(cc);
        removeFromBattleManager();
    }

    public void resetField() {
        fieldBlocks.forEach(fb -> fb.setType(Material.SNOW_BLOCK));
    }

    public void cancel() {
        end(null, EndReason.CANCEL);
    }

    public void start(StartReason reason) {
        for (SpleefPlayer player : players) {
            GamePlugin.unspectateGlobal(player);
            GamePlugin.dequeueGlobal(player);
        }
        BattleStartEvent event = new BattleStartEvent(this, reason);
        Bukkit.getPluginManager().callEvent(event);
        if (!event.isCancelled()) {
            addToBattleManager();
            arena.registerGameStart();
            if (arena.getScoreboards() != null) {
                for (com.spleefleague.superspleef.game.scoreboards.Scoreboard scoreboard : arena.getScoreboards()) {
                    scoreboard.setScore(new int[arena.getSize()]);
                }
            }
            ChatManager.registerChannel(cc);
            String playerNames = "";
            for (int i = 0; i < players.size(); i++) {
                SpleefPlayer sp = players.get(i);
                VirtualWorld.getInstance().getFakeWorldManager().addWorld(sp.getPlayer(), fakeWorld, FAKE_WORLD_PRIORITY);
                if (i == 0) {
                    playerNames = ChatColor.RED + sp.getName();
                } else if (i == players.size() - 1) {
                    playerNames += ChatColor.GREEN + " and " + ChatColor.RED + sp.getName();
                } else {
                    playerNames += ChatColor.GREEN + ", " + ChatColor.RED + sp.getName();
                }
                Player p = sp.getPlayer();
                SLPlayer slp = SpleefLeague.getInstance().getPlayerManager().get(p);
                PlayerData pd = new PlayerData(sp, arena.getSpawns()[i]);
                this.data.putIfAbsent(sp, pd);
                p.eject();
                p.teleport(arena.getSpawns()[i]);
                p.setHealth(p.getMaxHealth());
                p.setFoodLevel(20);
                sp.setIngame(true);
                sp.setDead(false);
                p.setGameMode(GameMode.ADVENTURE);
                p.closeInventory();
                p.getInventory().clear();
                p.getInventory().addItem(getShovel(sp));
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
                slp.setState(PlayerState.INGAME);
            }
            SpleefBattle.this.onStart();
            ChatManager.sendMessage(spleefMode.getChatPrefix(), Theme.SUCCESS.buildTheme(false) + "Beginning match on " + ChatColor.WHITE + arena.getName() + ChatColor.GREEN + " between " + ChatColor.RED + playerNames + ChatColor.GREEN + "!", SuperSpleef.getInstance().getStartMessageChannel());
            setSpawnCageBlock(Material.GLASS);
            setVisibilityAllPlayers();
            startClock();
            startRound();
        }
    }

    private void setVisibilityAllPlayers() {
        List<SpleefPlayer> battlePlayers = getActivePlayers();
        battlePlayers.addAll(spectators);
        for (SpleefPlayer sjp : SuperSpleef.getInstance().getPlayerManager().getAll()) {
            setVisibility(sjp);
        }
    }

    public void setVisibility(SpleefPlayer target) {
        List<SpleefPlayer> battlePlayers = getActivePlayers();
        battlePlayers.addAll(spectators);
        for (SpleefPlayer active : battlePlayers) {
            if (!battlePlayers.contains(target)) {
                target.hidePlayer(active.getPlayer());
                active.hidePlayer(target.getPlayer());
            }
            else {
                target.showPlayer(active.getPlayer());
                active.showPlayer(target.getPlayer());
            }
        }
    }

    public void startRound() {
        inCountdown = true;
        resetField();
        setSpawnCageBlock(Material.GLASS);
        for (SpleefPlayer sp : getActivePlayers()) {
            sp.setFrozen(true);
            sp.setGameMode(GameMode.ADVENTURE);
            sp.setRequestingReset(false);
            sp.setRequestingEndgame(false);
            sp.setDead(false);
            sp.setFireTicks(0);
            sp.teleport(this.data.get(sp).getSpawn());
            Player c = sp.getPlayer();
            for (SpleefPlayer sp2 : getActivePlayers()) {
                if (sp != sp2) {
                    c.showPlayer(sp2.getPlayer());
                }
            }
        }
        BukkitRunnable br = new BukkitRunnable() {
            private int secondsLeft = 3;

            @Override
            public void run() {
                if (secondsLeft > 0) {
                    ChatManager.sendMessage(spleefMode.getChatPrefix(), secondsLeft + "...", cc);
                    secondsLeft--;
                } else {
                    ChatManager.sendMessage(spleefMode.getChatPrefix(), "GO!", cc);
                    for (SpleefPlayer sp : getActivePlayers()) {
                        sp.setFrozen(false);
                    }
                    onRoundStart();
                    super.cancel();
                }
            }
        };
        br.runTaskTimer(SuperSpleef.getInstance(), 20, 20);
    }

    protected void onRoundStart() {
        for (SpleefPlayer sp : getActivePlayers()) {
            sp.setFrozen(false);
        }
        setSpawnCageBlock(Material.AIR);
        inCountdown = false;
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

    private void setSpawnCageBlock(Material type) {
        for (Location spawn : this.arena.getSpawns()) {
            for (Vector vector : spawnCageDefinition) {
                fakeWorld.getBlockAt(spawn.clone().add(vector)).setType(type);
            }
        }
    }

    public Collection<Vector> generateSpawnCageDefinition() {
        Collection<Vector> col = new ArrayList<>();
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                if (x == 0 && z == 0) {
                    col.add(new Vector(x, 2, z));
                } else {
                    for (int y = 0; y <= 2; y++) {
                        col.add(new Vector(x, y, z));
                    }
                }
            }
        }
        return col;
    }

    public boolean isInCountdown() {
        return inCountdown;
    }

    protected void saveGameHistory(SpleefPlayer winner, EndReason reason) {
        GameHistory gh = new GameHistory(this, winner, reason);
        Bukkit.getScheduler().runTaskAsynchronously(SuperSpleef.getInstance(), () -> EntityBuilder.save(gh, SuperSpleef.getInstance().getPluginDB().getCollection("GameHistory")));
    }

    public PlayerData getData(SpleefPlayer sp) {
        return data.get(sp);
    }

    public int getDuration() {
        return ticksPassed;
    }

    public void changePointsCup(int value) {
        pointsCap = value;
        for(SpleefPlayer sp : getActivePlayers()) {
            PlayerData pd = this.data.get(sp);
            if(pd.getPoints() >= pointsCap) {
                end(sp, EndReason.NORMAL);
                break;
            }
        }
        reInitScoreboard();
    }
    
    /**
     * Calculates the rating change according to modified
     * version of this formula 
     * https://en.wikipedia.org/wiki/Elo_rating_system
     * 
     * @param p1 The rating of player 1
     * @param p2 The rating of player 2
     * @param compare -1 if p1 won, 0 if draw, 1 if p2 won
     * @return The rating change from the position of p1, negate for p2
    */
    public static double calculateEloRatingChange(double p1, double p2, int compare) {
        final double MAX_RATING = 40;
        double elo = (1f / (1f + Math.pow(2f, ((p2 - p1) / 250f))));
        double s;
        if(compare < 0) {
            s = 1;
        }
        else if(compare > 0) {
            s = 0;
        }
        else {
            s = 0.5;
        }
        double ratingChange = MAX_RATING * (s - elo);
        if(compare != 0) {
            double sign = Math.signum(ratingChange);
            ratingChange = sign * Math.abs(ratingChange);
        }
        return ratingChange;
    }

    private static ItemStack getShovel(SpleefPlayer sp) {
        Shovel shovel = sp.getActiveShovel();
        net.minecraft.server.v1_12_R1.ItemStack stack = CraftItemStack.asNMSCopy(shovel.toItemStack());
        NBTTagCompound tag = stack.hasTag() ? stack.getTag() : new NBTTagCompound();
        NBTTagList list = new NBTTagList();
        list.add(new NBTTagString("minecraft:snow"));
        tag.set("CanDestroy", list);
        stack.setTag(tag);
        ItemStack is = CraftItemStack.asBukkitCopy(stack);
        is.setDurability(shovel.getDamage());
        ItemMeta im = is.getItemMeta();
        im.setUnbreakable(true);
        im.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_DESTROYS, ItemFlag.HIDE_UNBREAKABLE);
        is.setItemMeta(im);
        return is;
    }

    public static class PlayerData {

        private int points;
        private final Location spawn;
        private final SpleefPlayer sp;
        private final GameMode oldGamemode;
        private final ItemStack[] oldInventory;
        private final ItemStack[] oldArmor;

        public PlayerData(SpleefPlayer sp, Location spawn) {
            this.sp = sp;
            this.spawn = spawn;
            this.points = 0;
            Player p = sp.getPlayer();
            oldGamemode = p.getGameMode();
            oldInventory = p.getInventory().getContents();
            oldArmor = p.getInventory().getArmorContents();
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
            p.getInventory().setArmorContents(oldArmor);
        }
    }
}
