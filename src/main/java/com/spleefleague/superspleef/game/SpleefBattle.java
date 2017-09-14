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
import com.spleefleague.core.SpleefLeague;
import com.spleefleague.core.chat.ChatChannel;
import com.spleefleague.core.chat.ChatManager;
import com.spleefleague.core.chat.Theme;
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
import com.spleefleague.superspleef.game.signs.GameSign;
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

/**
 *
 * @author Jonas
 */
public abstract class SpleefBattle implements Battle<Arena, SpleefPlayer> {

    private static final int FAKE_WORLD_PRIORITY = 10;
    private final Arena arena;
    private final List<SpleefPlayer> players, spectators;
    private final Map<SpleefPlayer, PlayerData> data;
    private final ChatChannel cc;
    private int ticksPassed = 0, round = 0;
    private BukkitRunnable clock;
    private Scoreboard scoreboard;
    private boolean inCountdown = false;
    private final SpleefMode spleefMode;
    private final FakeWorld fakeWorld;
    private final Collection<FakeBlock> fieldBlocks;
    private int pointsCup = 0;
    private final Collection<Vector> spawnCageDefinition;
    
    protected SpleefBattle(Arena arena, List<SpleefPlayer> players) {
        this(arena, players, arena.getSpleefMode());
    }

    private SpleefBattle(Arena arena, List<SpleefPlayer> players, SpleefMode spleefMode) {
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
        this.pointsCup = this.arena.getMaxRating();
    }
    
    public abstract void removePlayer(SpleefPlayer sp, boolean surrender);

    public abstract void end(SpleefPlayer winner, EndReason reason);

    protected abstract void onStart();

    protected abstract void updateScoreboardTime();

    public abstract void onArenaLeave(SpleefPlayer player);

    public abstract void onScoreboardUpdate();

    @Override
    public Arena getArena() {
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
        return this.pointsCup;
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
        hidePlayers(sp);
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

    public ArrayList<SpleefPlayer> getAlivePlayers() {
        return this.getActivePlayers();
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
        if(arena.getSpleefMode() == SpleefMode.MULTI) {
            SuperSpleef.getInstance().getMultiSpleefBattleManager().remove(this);
        }
        else {
            SuperSpleef.getInstance().getNormalSpleefBattleManager().remove(this);   
        }
        ChatManager.unregisterChannel(cc);
        GameSign.updateGameSigns(arena);
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
            arena.registerGameStart();
            if (arena.getScoreboards() != null) {
                for (com.spleefleague.superspleef.game.scoreboards.Scoreboard scoreboard : arena.getScoreboards()) {
                    scoreboard.setScore(new int[arena.getSize()]);
                }
            }
            GameSign.updateGameSigns(arena);
            ChatManager.registerChannel(cc);
            if(arena.getSpleefMode() == SpleefMode.MULTI) {
                SuperSpleef.getInstance().getMultiSpleefBattleManager().add(this);
            }
            else {
                SuperSpleef.getInstance().getNormalSpleefBattleManager().add(this);   
            }
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
                this.data.put(sp, new PlayerData(sp, arena.getSpawns()[i]));
                p.eject();
                p.teleport(arena.getSpawns()[i]);
                p.setHealth(p.getMaxHealth());
                p.setFoodLevel(20);
                sp.setIngame(true);
                sp.setDead(false);
                p.setGameMode(GameMode.ADVENTURE);
                p.closeInventory();
                p.getInventory().clear();
                p.getInventory().addItem(getShovel());
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
            ChatManager.sendMessage(SuperSpleef.getInstance().getChatPrefix(), Theme.SUCCESS.buildTheme(false) + "Beginning match on " + ChatColor.WHITE + arena.getName() + ChatColor.GREEN + " between " + ChatColor.RED + playerNames + ChatColor.GREEN + "!", SuperSpleef.getInstance().getStartMessageChannel());
            setSpawnCageBlock(Material.GLASS);
            hidePlayers();
            startClock();
            startRound();
        }
    }

    private void hidePlayers() {
        List<SpleefPlayer> battlePlayers = getActivePlayers();
        battlePlayers.addAll(spectators);
        for (SpleefPlayer sjp : SuperSpleef.getInstance().getPlayerManager().getAll()) {
            hidePlayers(sjp);
        }
    }

    protected void hidePlayers(SpleefPlayer target) {
        List<SpleefPlayer> battlePlayers = getActivePlayers();
        battlePlayers.addAll(spectators);
        for (SpleefPlayer active : battlePlayers) {
            if (!battlePlayers.contains(target)) {
                target.hidePlayer(active.getPlayer());
                active.hidePlayer(target.getPlayer());
            }
        }
    }

    public void startRound() {
        inCountdown = true;
        resetField();
        setSpawnCageBlock(Material.GLASS);
        for (SpleefPlayer sp : getActivePlayers()) {
            sp.setFrozen(true);
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
                    ChatManager.sendMessage(SuperSpleef.getInstance().getChatPrefix(), secondsLeft + "...", cc);
                    secondsLeft--;
                } else {
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
                    //sp.teleport(getData(sp).getSpawn().clone().add(0, 0.3, 0));
                    sp.setFrozen(false);
                }
                setSpawnCageBlock(Material.AIR);
                inCountdown = false;
            }
        };
        br.runTaskTimer(SuperSpleef.getInstance(), 20, 20);
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

    protected void saveGameHistory(SpleefPlayer winner) {
        GameHistory gh = new GameHistory(this, winner);
        Bukkit.getScheduler().runTaskAsynchronously(SuperSpleef.getInstance(), () -> EntityBuilder.save(gh, SuperSpleef.getInstance().getPluginDB().getCollection("GameHistory")));
    }

    public PlayerData getData(SpleefPlayer sp) {
        return data.get(sp);
    }

    public int getDuration() {
        return ticksPassed;
    }

    public void changePointsCup(int value) {
        pointsCup = value;
        for(SpleefPlayer sp : getActivePlayers()) {
            PlayerData pd = this.data.get(sp);
            if(pd.getPoints() >= pointsCup) {
                end(sp, EndReason.NORMAL);
                break;
            }
        }
    }

    private static ItemStack getShovel() {
        net.minecraft.server.v1_12_R1.ItemStack stack = CraftItemStack.asNMSCopy(new ItemStack(Material.DIAMOND_SPADE));
        NBTTagCompound tag = stack.hasTag() ? stack.getTag() : new NBTTagCompound();
        NBTTagList list = new NBTTagList();
        list.add(new NBTTagString("minecraft:snow"));
        tag.set("CanDestroy", list);
        tag.setBoolean("Unbreakable", true);
        stack.setTag(tag);
        return CraftItemStack.asBukkitCopy(stack);
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
