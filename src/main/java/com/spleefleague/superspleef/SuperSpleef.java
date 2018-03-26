/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.spleefleague.superspleef;

import com.mongodb.client.MongoDatabase;
import com.spleefleague.core.SpleefLeague;
import com.spleefleague.core.chat.ChatChannel;
import com.spleefleague.core.chat.ChatManager;
import com.spleefleague.core.chat.Theme;
import com.spleefleague.commands.command.CommandLoader;
import com.spleefleague.core.events.BattleEndEvent.EndReason;
import com.spleefleague.core.events.BattleStartEvent.StartReason;
import com.spleefleague.core.menus.SLMenu;
import com.spleefleague.core.player.PlayerManager;
import com.spleefleague.core.plugin.GamePlugin;
import com.spleefleague.core.plugin.PlayerHandling;
import com.spleefleague.core.queue.BattleManager;
import com.spleefleague.core.queue.RatedBattleManager;
import com.spleefleague.superspleef.game.Arena;
import com.spleefleague.superspleef.game.SpleefBattle;
import com.spleefleague.superspleef.game.SpleefMode;
import com.spleefleague.superspleef.game.teamspleef.TeamSpleefArena;
import com.spleefleague.superspleef.listener.ConnectionListener;
import com.spleefleague.superspleef.listener.EnvironmentListener;
import com.spleefleague.superspleef.listener.GameListener;
import com.spleefleague.superspleef.player.SpleefPlayer;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.List;

import com.spleefleague.entitybuilder.EntityBuilder;
import com.spleefleague.superspleef.game.Field;
import com.spleefleague.superspleef.game.RemoveReason;
import com.spleefleague.superspleef.game.cosmetics.Shovel;
import com.spleefleague.superspleef.game.multispleef.MultiSpleefArena;
import com.spleefleague.superspleef.game.multispleef.MultiSpleefBattle;
import com.spleefleague.superspleef.game.powerspleef.Power;
import com.spleefleague.superspleef.game.powerspleef.PowerSpleefArena;
import com.spleefleague.superspleef.game.powerspleef.PowerSpleefBattle;
import com.spleefleague.superspleef.game.spleef.NormalSpleefArena;
import com.spleefleague.superspleef.game.spleef.NormalSpleefBattle;
import com.spleefleague.superspleef.game.teamspleef.TeamSpleefBattle;
import com.spleefleague.superspleef.menu.SpleefMenu;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * @author Jonas
 */
public class SuperSpleef extends GamePlugin implements PlayerHandling {

    private static SuperSpleef instance;
    private PlayerManager<SpleefPlayer> playerManager;
    private BattleManager<NormalSpleefArena, SpleefPlayer, NormalSpleefBattle> battleManagerNormalSpleef;
    private BattleManager<MultiSpleefArena, SpleefPlayer, MultiSpleefBattle> battleManagerMultiSpleef;
    private BattleManager<TeamSpleefArena, SpleefPlayer, TeamSpleefBattle> battleManagerTeamSpleef;
    private BattleManager<PowerSpleefArena, SpleefPlayer, PowerSpleefBattle> battleManagerPowerSpleef;
    
    private boolean queuesOpen = true;
    private ChatChannel start, end;

    public SuperSpleef() {
        super(ChatColor.GRAY + "[" + ChatColor.GOLD + "SuperSpleef" + ChatColor.GRAY + "]" + ChatColor.RESET);
    }

    @Override
    public void start() {
        instance = this;
        this.playerManager = new PlayerManager(this, SpleefPlayer.class);
        this.battleManagerNormalSpleef = new RatedBattleManager<NormalSpleefArena, SpleefPlayer, NormalSpleefBattle>(sp -> sp.getRating(SpleefMode.NORMAL)) {
            @Override
            public void startBattle(NormalSpleefArena arena, List<SpleefPlayer> players) {
                arena.startBattle(players, StartReason.QUEUE);
            }
        };
        this.battleManagerMultiSpleef = new RatedBattleManager<MultiSpleefArena, SpleefPlayer, MultiSpleefBattle>(sp -> sp.getRating(SpleefMode.MULTI)) {
            @Override
            public void startBattle(MultiSpleefArena arena, List<SpleefPlayer> players) {
                arena.startBattle(players, StartReason.QUEUE);
            }
        };
        this.battleManagerTeamSpleef = new RatedBattleManager<TeamSpleefArena, SpleefPlayer, TeamSpleefBattle>(sp -> sp.getRating(SpleefMode.TEAM)) {
            @Override
            public void startBattle(TeamSpleefArena arena, List<SpleefPlayer> players) {
                arena.startBattle(players, StartReason.QUEUE);
            }
        };
        this.battleManagerPowerSpleef = new RatedBattleManager<PowerSpleefArena, SpleefPlayer, PowerSpleefBattle>(sp -> sp.getRating(SpleefMode.POWER)) {
            @Override
            public void startBattle(PowerSpleefArena arena, List<SpleefPlayer> players) {
                arena.startBattle(players, StartReason.QUEUE);
            }
        };
        Field.init();
        Arena.init();
        start = ChatChannel.valueOf("GAME_MESSAGE_SPLEEF_START");
        end = ChatChannel.valueOf("GAME_MESSAGE_SPLEEF_END");
        ConnectionListener.init();
        GameListener.init();
        EnvironmentListener.init();
        Shovel.init();
        Power.startSchedulers();
        CommandLoader.loadCommands(this, "com.spleefleague.superspleef.commands");
        createGameMenu();
    }

    @Override
    public void stop() {
        Arrays.stream(this.getBattleManagers())
                .flatMap(bm -> bm.getAll().stream())
                .forEach(SpleefBattle::cancel);
        playerManager.saveAll();
    }

    @Override
    public MongoDatabase getPluginDB() {
        return SpleefLeague.getInstance().getMongo().getDatabase("SuperSpleef");
    }

    public PlayerManager<SpleefPlayer> getPlayerManager() {
        return playerManager;
    }

    public BattleManager<NormalSpleefArena, SpleefPlayer, NormalSpleefBattle> getNormalSpleefBattleManager() {
        return battleManagerNormalSpleef;
    }

    public BattleManager<MultiSpleefArena, SpleefPlayer, MultiSpleefBattle> getMultiSpleefBattleManager() {
        return battleManagerMultiSpleef;
    }

    public BattleManager<TeamSpleefArena, SpleefPlayer, TeamSpleefBattle> getTeamSpleefBattleManager() {
        return battleManagerTeamSpleef;
    }

    public BattleManager<PowerSpleefArena, SpleefPlayer, PowerSpleefBattle> getPowerSpleefBattleManager() {
        return battleManagerPowerSpleef;
    }

    public static SuperSpleef getInstance() {
        return instance;
    }

    public ChatChannel getStartMessageChannel() {
        return start;
    }

    public ChatChannel getEndMessageChannel() {
        return end;
    }

    @Override
    public boolean spectate(Player target, Player p) {
        SpleefPlayer tsjp = getPlayerManager().get(target);
        SpleefPlayer sjp = getPlayerManager().get(p);
        if (tsjp.getCurrentBattle().getArena().getSpleefMode() == SpleefMode.TEAM) {
            Arena arena = tsjp.getCurrentBattle().getArena();
            if (arena == null) {
                p.sendMessage(SuperSpleef.getInstance().getChatPrefix() + Theme.ERROR.buildTheme(false)
                        + " You are unable to spectate this teamspleef match.");
                return false;
            }
            tsjp.getCurrentBattle().addSpectator(sjp);
            return true;
        } else if (tsjp.getCurrentBattle().getArena().isAvailable(sjp)) {
            tsjp.getCurrentBattle().addSpectator(sjp);
            return true;
        } else {
            p.sendMessage(SuperSpleef.getInstance().getChatPrefix() + Theme.ERROR.buildTheme(false)
                    + " You can only spectate arenas you have already visited!");
            return false;
        }
    }

    @Override
    public void unspectate(Player p) {
        SpleefPlayer sjp = getPlayerManager().get(p);
        for (BattleManager<? extends Arena, SpleefPlayer, ? extends SpleefBattle> bm : getBattleManagers()) {
            for (SpleefBattle battle : bm.getAll()) {
                if (battle.isSpectating(sjp)) {
                    battle.removeSpectator(sjp);
                }
            }
        }
    }

    @Override
    public boolean isSpectating(Player p) {
        SpleefPlayer sjp = getPlayerManager().get(p);
        for (BattleManager<? extends Arena, SpleefPlayer, ? extends SpleefBattle> bm : getBattleManagers()) {
            if (bm.getBattleForSpectator(sjp) != null) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void dequeue(Player p) {
        SpleefPlayer sp = getPlayerManager().get(p);
        for (BattleManager bm : getBattleManagers()) {
            bm.dequeue(sp);
        }
    }

    @Override
    public void cancel(Player p) {
        SpleefPlayer sp = getPlayerManager().get(p);
        SpleefBattle battle = sp.getCurrentBattle();
        if (battle != null) {
            battle.cancel();
            ChatManager.sendMessage(
                    SuperSpleef.getInstance().getChatPrefix() + Theme.SUPER_SECRET.buildTheme(false)
                    + " The battle on " + battle.getArena().getName() + " has been cancelled.",
                    ChatChannel.STAFF_NOTIFICATIONS
            );
        }
    }

    @Override
    public void surrender(Player p) {
        SpleefPlayer sp = getPlayerManager().get(p);
        SpleefBattle<?> battle = sp.getCurrentBattle();
        if (battle != null) {
            for (SpleefPlayer active : battle.getActivePlayers()) {
                active.sendMessage(
                        SuperSpleef.getInstance().getChatPrefix() + Theme.SUPER_SECRET.buildTheme(false) + " "
                        + p.getName() + " has surrendered!");
            }
            battle.removePlayer(sp, RemoveReason.SURRENDER);
        }
    }

    @Override
    public boolean isQueued(Player p) {
        SpleefPlayer sp = getPlayerManager().get(p);
        for (BattleManager<? extends Arena, SpleefPlayer, ? extends SpleefBattle> bm : SuperSpleef.getInstance().getBattleManagers()) {
            if (bm.isQueued(sp)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isIngame(Player p) {
        return getPlayerManager().get(p).isIngame();
    }

    @Override
    public void cancelAll() {
        Arrays.stream(getBattleManagers())
                .flatMap(bm -> bm.getAll().stream())
                .collect(Collectors.toSet())//Avoid concurrent modification
                .forEach(SpleefBattle::cancel);
    }

    @Override
    public void printStats(Player p, Player target) {
        SpleefPlayer sp = playerManager.get(target);
        p.sendMessage(Theme.INFO + sp.getName() + "'s Spleef stats");
        p.sendMessage(Theme.INCOGNITO + "Rating: " + ChatColor.YELLOW + sp.getRating(SpleefMode.NORMAL));
        p.sendMessage(Theme.INCOGNITO + "Rank: " + ChatColor.YELLOW + sp.getRank());
    }

    @Override
    public void requestEndgame(Player p) {
        SpleefPlayer sp = SuperSpleef.getInstance().getPlayerManager().get(p);
        SpleefBattle<?> battle = sp.getCurrentBattle();
        if (battle != null) {
            sp.setRequestingEndgame(true);
            boolean shouldEnd = true;
            for (SpleefPlayer spleefplayer : battle.getActivePlayers()) {
                if (!spleefplayer.isRequestingEndgame()) {
                    shouldEnd = false;
                    break;
                }
            }
            if (shouldEnd) {
                battle.end(null, EndReason.ENDGAME);
            } else {
                battle.onScoreboardUpdate();
                for (SpleefPlayer spleefplayer : battle.getActivePlayers()) {
                    if (!spleefplayer.isRequestingEndgame()) {
                        spleefplayer.sendMessage(
                                SuperSpleef.getInstance().getChatPrefix() + " " + Theme.WARNING.buildTheme(false)
                                + "Your opponent wants to end this game. To agree enter " + ChatColor.YELLOW
                                + "/endgame.");
                    }
                }
                sp.sendMessage(SuperSpleef.getInstance().getChatPrefix() + " " + Theme.WARNING.buildTheme(false)
                        + "You requested this game to be cancelled.");
            }
        }
    }

    @Override
    public void setQueueStatus(boolean open) {
        queuesOpen = open;
    }

    @Override
    public void syncSave(Player p) {
        SpleefPlayer slp = playerManager.get(p);
        if (slp != null) {
            EntityBuilder.save(slp, getPluginDB().getCollection("Players"));
        }
    }

    public boolean queuesOpen() {
        return queuesOpen;
    }
    
    private void createGameMenu() {
        SpleefMenu.createSpleefMenu(SLMenu.getNewGamemodeMenu());
    }

//    private void createGameMenu() {
//        InventoryMenuTemplateBuilder menu = SLMenu
//                .getNewGamemodeMenu()
//                .displayName("Spleef")
//                .displayIcon(Material.SNOW_BLOCK)
//                .flags(InventoryMenuFlag.EXIT_ON_CLICK_OUTSIDE);
//        menu.component(SpleefMenu.createSpleefMenu());
//        InventoryMenuTemplateBuilder arenaMenu = menu()
//                .displayName("Arenas")
//                .displayIcon(Material.MAP)
//                .visibilityController((slp) -> (queuesOpen));
//        Arena.getAll().stream().forEach((arena) -> {
//            arenaMenu.component(item()
//                    .displayName(arena.getName())
//                    .description(arena.getDynamicDescription())
//                    .displayIcon(
//                            (slp) -> (arena.isAvailable(playerManager.get(slp)) ? Material.MAP : Material.EMPTY_MAP))
//                    .onClick((event) -> {
//                        SpleefPlayer sp = getPlayerManager().get(event.getPlayer());
//                        if (arena.isAvailable(sp)) {
//                            if (arena.isOccupied()) {
//                                battleManagerNormalSpleef.getBattle(arena).addSpectator(sp);
//                            } else if (!arena.isPaused()) {
//                                battleManagerNormalSpleef.queue(sp, arena);
//                                event.getItem().getParent().update();
//                            }
//                        }
//                    }));
//        });
//        InventoryMenuTemplateBuilder shovelMenu = menu()
//                .displayName("Shovels")
//                .displayIcon(Material.GOLD_SPADE)
//                .staticComponent(4, 5, item()
//                        .displayItem((slp) -> {
//                            SpleefPlayer sp = playerManager.get(slp);
//                            return sp.getActiveShovel().toItemStack();
//                        })
//                );
//                
//        InventoryMenuTemplateBuilder powerMenu = menu()
//                .displayName("Powers")
//                .displayIcon(Material.BOOK)
//                .visibilityController((slp) -> SpleefLeague.getInstance().getServerType() == ServerType.TEST);
//        Shovel.getAll()
//                .stream()
//                .sorted((s1, s2) -> Short.compare(s1.getDamage(), s2.getDamage()))
//                .forEach((shovel) -> {
//                    shovelMenu.component(item()
//                            .displayItem((slp) -> {
//                                SpleefPlayer sp = playerManager.get(slp);
//                                if(shovel.isIsDefault() || sp.getAvailableShovels().contains(shovel)) {
//                                    return shovel.toItemStack();
//                                }
//                                ItemStack lock = new ItemStack(Material.DIAMOND_AXE, 1, (short)12);
//                                ItemMeta meta = lock.getItemMeta();
//                                meta.setDisplayName(ChatColor.GRAY + "" + ChatColor.ITALIC + "Locked");
//                                lock.setItemMeta(meta);
//                                return lock;
//                            })
//                            .onClick((event) -> {
//                                SpleefPlayer sp = playerManager.get(event.getPlayer());
//                                if(!shovel.isIsDefault() && !sp.getAvailableShovels().contains(shovel)) {
//                                    return;
//                                }
//                                sp.setActiveShovel(shovel);
//                                event.getItem().getParent().update();
//                            })
//                    );
//                });
//        for(PowerType powerType : PowerType.values()) {
//            powerMenu.component(item()
//                    .displayItem(powerType.getItem())
//                    .description((slp) -> {
//                        SpleefPlayer sp = playerManager.get(slp);
//                        List<String> description = powerType.getDescription();
//                        if(sp.getPowerType() == powerType) {
//                            description.add(ChatColor.GREEN + "Enabled");
//                        }
//                        else {   
//                            description.add(ChatColor.RED + "Disabled");
//                        }
//                        return description;
//                    })
//                    .visibilityController((slp) -> {
////                            SpleefPlayer sp = playerManager.get(slp);
////                            if(sp != null) {
////                                return sp.getAvailablePowers().contains(powerType);
////                            }
////                            return false;
//                        return true;
//                    })
//                    .onClick((event) -> {
//                        SpleefPlayer sp = playerManager.get(event.getPlayer());
//                        sp.setActivePower(powerType);
//                        event.getItem().getParent().update();
//                    })
//            );
//        }
//        menu.component(arenaMenu);
//        menu.component(shovelMenu);
//        menu.component(powerMenu);
//    }

    @Override
    public BattleManager<? extends Arena, SpleefPlayer, ? extends SpleefBattle>[] getBattleManagers() {
        return new BattleManager[]{ 
            battleManagerNormalSpleef,
            battleManagerMultiSpleef,
            battleManagerTeamSpleef,
            battleManagerPowerSpleef
        };
    }
}
