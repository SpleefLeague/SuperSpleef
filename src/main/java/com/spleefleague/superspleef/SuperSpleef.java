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
import com.spleefleague.core.command.CommandLoader;
import com.spleefleague.core.events.BattleEndEvent.EndReason;
import com.spleefleague.core.events.BattleStartEvent.StartReason;
import com.spleefleague.core.io.EntityBuilder;
import com.spleefleague.core.menus.SLMenu;
import com.spleefleague.core.player.PlayerManager;
import com.spleefleague.core.plugin.GamePlugin;
import com.spleefleague.core.queue.BattleManager;
import com.spleefleague.core.queue.RatedBattleManager;
import static com.spleefleague.core.utils.inventorymenu.InventoryMenuAPI.item;
import com.spleefleague.core.utils.inventorymenu.InventoryMenuTemplateBuilder;
import com.spleefleague.superspleef.game.Arena;
import com.spleefleague.superspleef.game.Battle;
import com.spleefleague.superspleef.game.signs.GameSign;
import com.spleefleague.superspleef.listener.ConnectionListener;
import com.spleefleague.superspleef.listener.EnvironmentListener;
import com.spleefleague.superspleef.listener.GameListener;
import com.spleefleague.superspleef.listener.SignListener;
import com.spleefleague.superspleef.player.SpleefPlayer;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;

/**
 *
 * @author Jonas
 */
public class SuperSpleef extends GamePlugin {

    private static SuperSpleef instance;
    private PlayerManager<SpleefPlayer> playerManager;
    private BattleManager<Arena, SpleefPlayer, Battle> battleManagerSpleef;
    private boolean queuesOpen = true;
    private ChatChannel start, end;
    
    public SuperSpleef() {
        super("[SuperSpleef]", ChatColor.GRAY + "[" + ChatColor.GOLD + "SuperSpleef" + ChatColor.GRAY + "]" + ChatColor.RESET);
    }
    
    @Override
    public void start() {
        instance = this;
        this.playerManager = new PlayerManager(this, SpleefPlayer.class);
        this.battleManagerSpleef = new RatedBattleManager<Arena, SpleefPlayer, Battle>() {
            @Override
            public void startBattle(Arena queue, List<SpleefPlayer> players) {
                queue.startBattle(players, StartReason.QUEUE);
            }
        };
        Arena.init();
        createGameMenu();
        start = ChatChannel.valueOf("GAME_MESSAGE_SPLEEF_START");
        end = ChatChannel.valueOf("GAME_MESSAGE_SPLEEF_END");
        ConnectionListener.init();
        GameListener.init();
        SignListener.init();
        EnvironmentListener.init();
        GameSign.initialize();
        CommandLoader.loadCommands(this, "com.spleefleague.superspleef.commands");
    }
    
    @Override
    public void stop() {
        for(Battle battle : battleManagerSpleef.getAll()) {
            battle.cancel();
        }
        playerManager.saveAll();
    }

    @Override
    public MongoDatabase getPluginDB() {
        return SpleefLeague.getInstance().getMongo().getDatabase("SuperSpleef");
    }   
    
    public PlayerManager<SpleefPlayer> getPlayerManager() {
        return playerManager;
    }
    
    public BattleManager<Arena, SpleefPlayer, Battle> getBattleManager() {
        return battleManagerSpleef;
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
        if(sjp.getVisitedArenas().contains(tsjp.getCurrentBattle().getArena())) {
            tsjp.getCurrentBattle().addSpectator(sjp);
            return true;
        }
        else {
            p.sendMessage(SuperSpleef.getInstance().getChatPrefix() + Theme.ERROR.buildTheme(false) + " You can only spectate arenas you have already visited!");
            return false;
        }
    }
    
    @Override
    public void unspectate(Player p) {
        SpleefPlayer sjp = getPlayerManager().get(p);
        for(Battle battle : getBattleManager().getAll()) {
            if(battle.isSpectating(sjp)) {
                battle.removeSpectator(sjp);
            }
        }
//        for(Battle battle : getBattleManagerMultiSpleef().getAll()) {
//            if(battle.isSpectating(sjp)) {
//                battle.removeSpectator(sjp);
//            }
//        }
    }
    
    @Override
    public boolean isSpectating(Player p) {
        SpleefPlayer sjp = getPlayerManager().get(p);
        for(Battle battle : getBattleManager().getAll()) {
            if(battle.isSpectating(sjp)) {
                return true;
            }
        }
//        for(Battle battle : getBattleManagerMultiSpleef().getAll()) {
//            if(battle.isSpectating(sjp)) {
//                return true;
//            }
//        }
        return false;
    }

    @Override
    public void dequeue(Player p) {
        SpleefPlayer sp = getPlayerManager().get(p);
        getBattleManager().dequeue(sp);
//        getBattleManagerMultiSpleef().dequeue(sp);
    }

    @Override
    public void cancel(Player p) {
        SpleefPlayer sp = getPlayerManager().get(p);
        Battle battle = getBattleManager().getBattle(sp);
//        if(battle == null) {
//            battle = getBattleManagerMultiSpleef().getBattle(sp);
//        }
        if(battle != null) {
            battle.cancel();    
            ChatManager.sendMessage(SuperSpleef.getInstance().getChatPrefix() + Theme.SUPER_SECRET.buildTheme(false) + " The battle on " + battle.getArena().getName() + " has been cancelled.", ChatChannel.STAFF_NOTIFICATIONS);
        }
    }
    
    @Override
    public void surrender(Player p) {
        SpleefPlayer sp = getPlayerManager().get(p);
        Battle battle = getBattleManager().getBattle(sp);
//        if(battle == null) {
//            battle = getBattleManagerMultiSpleef().getBattle(sp);
//        }
        if(battle != null) {
            for(SpleefPlayer active : battle.getActivePlayers()) {
                active.sendMessage(SuperSpleef.getInstance().getChatPrefix() + Theme.SUPER_SECRET.buildTheme(false) + " " + p.getName() + " has surrendered!");
            }
            battle.removePlayer(sp, true);
        }
    }

    @Override
    public boolean isQueued(Player p) {
        SpleefPlayer sp = getPlayerManager().get(p);
        return getBattleManager().isQueued(sp);
    }

    @Override
    public boolean isIngame(Player p) {
        SpleefPlayer sp = getPlayerManager().get(p);
        return getBattleManager().isIngame(sp);
    }
    
    @Override
    public void cancelAll() {
        for(Battle battle : new ArrayList<>(battleManagerSpleef.getAll())) {
            battle.cancel();
        }
    }

    @Override
    public void printStats(Player p) {
        SpleefPlayer sp = playerManager.get(p);
        p.sendMessage(Theme.INFO + p.getName() + "'s SWC stats");
        p.sendMessage(Theme.ERROR + "SWC Rating: " + ChatColor.YELLOW + sp.getSwcRating());
        p.sendMessage(Theme.ERROR + "SWC Rank: " + ChatColor.YELLOW + sp.getSwcRank());
        p.sendMessage(Theme.INFO + p.getName() + "'s Spleef stats");
        p.sendMessage(Theme.INCOGNITO + "Rating: " + ChatColor.YELLOW + sp.getRating());
        p.sendMessage(Theme.INCOGNITO + "Rank: " + ChatColor.YELLOW + sp.getRank());
    }

    @Override
    public void requestEndgame(Player p) {
        SpleefPlayer sp = SuperSpleef.getInstance().getPlayerManager().get(p);
        Battle battle = sp.getCurrentBattle();
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
            }
            else {
                for (SpleefPlayer spleefplayer : battle.getActivePlayers()) {
                    if (!spleefplayer.isRequestingEndgame()) {
                        spleefplayer.sendMessage(SuperSpleef.getInstance().getChatPrefix() + " " + Theme.WARNING.buildTheme(false) + "Your opponent wants to end this game. To agree enter " + ChatColor.YELLOW + "/endgame.");
                    }
                }
                sp.sendMessage(SuperSpleef.getInstance().getChatPrefix() + " " + Theme.WARNING.buildTheme(false) + "You requested this game to be cancelled.");
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
        if(slp != null) {
            EntityBuilder.save(slp, getPluginDB().getCollection("Players"));
        }
    }
    
    public boolean queuesOpen() {
        return queuesOpen;
    }
    
     private void createGameMenu() {
        InventoryMenuTemplateBuilder menu = SLMenu.getNewGamemodeMenu()
                .displayName("SuperSpleef")
                .displayIcon(Material.SNOW_BLOCK)
                .exitOnClickOutside(true)
                .visibilityController((slp) -> (queuesOpen));
        Arena.getAll().stream().forEach((arena) -> {
            menu.component(item()
                    .displayName(arena.getName())
                    .description(arena.getDynamicDescription())
                    .displayIcon((slp) -> (arena.isAvailable(playerManager.get(slp)) ? Material.MAP : Material.EMPTY_MAP))
                    .onClick((event) -> {
                        SpleefPlayer sp = getPlayerManager().get(event.getPlayer());
                        if (arena.isAvailable(sp)) {
                            if (arena.isOccupied()) {
                                battleManagerSpleef.getBattle(arena).addSpectator(sp);
                            }
                            else {
                                if (!arena.isPaused()) {
                                    battleManagerSpleef.queue(sp, arena);
                                    event.getItem().getParent().update();
                                }
                            }
                        }
                    })
            );
        });
    }
}