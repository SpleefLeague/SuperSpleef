/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.spleefleague.superspleef.menu;

import com.spleefleague.core.SpleefLeague;
import com.spleefleague.core.events.BattleStartEvent;
import com.spleefleague.core.player.PlayerState;
import com.spleefleague.core.player.SLPlayer;
import com.spleefleague.core.queue.Challenge;
import com.spleefleague.core.queue.GameQueue;
import com.spleefleague.core.utils.Value;
import static com.spleefleague.core.utils.inventorymenu.InventoryMenuAPI.dialog;
import static com.spleefleague.core.utils.inventorymenu.InventoryMenuAPI.dialogMenu;
import static com.spleefleague.core.utils.inventorymenu.InventoryMenuAPI.item;
import static com.spleefleague.core.utils.inventorymenu.InventoryMenuAPI.menu;
import com.spleefleague.core.utils.inventorymenu.InventoryMenuComponentAlignment;
import com.spleefleague.core.utils.inventorymenu.InventoryMenuFlag;
import com.spleefleague.core.utils.inventorymenu.InventoryMenuItemTemplateBuilder;
import com.spleefleague.core.utils.inventorymenu.InventoryMenuTemplateBuilder;
import com.spleefleague.core.utils.inventorymenu.dialog.InventoryMenuDialogHolderTemplateBuilder;
import com.spleefleague.core.utils.inventorymenu.dialog.InventoryMenuDialogButtonTemplateBuilder;
import com.spleefleague.core.utils.inventorymenu.dialog.InventoryMenuDialogTemplateBuilder;
import com.spleefleague.superspleef.SuperSpleef;
import com.spleefleague.superspleef.game.Arena;
import com.spleefleague.superspleef.game.SpleefMode;
import com.spleefleague.superspleef.player.SpleefPlayer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import static com.spleefleague.core.utils.inventorymenu.InventoryMenuAPI.dialogButton;
import com.spleefleague.core.utils.inventorymenu.InventoryMenuComponentFlag;
import com.spleefleague.core.utils.inventorymenu.dialog.InventoryMenuDialogFlag;
import com.spleefleague.superspleef.game.powerspleef.PowerType;
import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;

/**
 *
 * @author jonas
 */
public class SpleefMenu {
    
    public static InventoryMenuTemplateBuilder createSpleefMenu(InventoryMenuTemplateBuilder base) {
        return base
                .title("Spleef Menu")
                .displayName("Spleef Menu")
                //.flags(InventoryMenuFlag.SKIP_SINGLE_SUBMENU)
                .displayIcon(Material.DIAMOND_SPADE)
                .component(2, () -> createClassicSpleefMenu().build())
                .component(3, () -> createPowerSpleefMenu().build())
//                .component(5, createPowerSpleefMenu())
//                .component(6, createPowerSpleefMenu())
                .flags(InventoryMenuFlag.MENU_CONTROL);
                
    }
    
    
    public static InventoryMenuTemplateBuilder createClassicSpleefMenu() {
        return createArenaMenu(SuperSpleef.getInstance().getNormalSpleefBattleManager().getGameQueue(), true)
                .title("Arenas")
                .displayIcon(Material.DIAMOND_HOE)
                .displayName("Classic Spleef")
                .component(
                        new InventoryMenuComponentAlignment(InventoryMenuComponentAlignment.Direction.LEFT, InventoryMenuComponentAlignment.Direction.UP), 
                        createChallengeDialog(createArenaChallengeDialog(SpleefMode.NORMAL, true), SpleefMode.NORMAL)
                    .displayIcon(Material.GOLD_SPADE)
                    .displayName("Challenge")
                    .onDone((slp, builder) -> performChallenge(builder, SpleefMode.NORMAL))
                );
    }
    
    public static InventoryMenuTemplateBuilder createPowerSpleefMenu() {
        return createArenaMenu(SuperSpleef.getInstance().getPowerSpleefBattleManager().getGameQueue(), true)
                .title("Arenas")
                .displayIcon(Material.DIAMOND_PICKAXE)
                .displayName("Power Spleef")
                .component(
                        new InventoryMenuComponentAlignment(InventoryMenuComponentAlignment.Direction.LEFT, InventoryMenuComponentAlignment.Direction.UP), 
                        createChallengeDialog(createArenaChallengeDialog(SpleefMode.POWER, true), SpleefMode.POWER)
                    .displayIcon(Material.GOLD_SPADE)
                    .displayName("Challenge")
                    .onDone((slp, builder) -> performChallenge(builder, SpleefMode.POWER))
                )
                .component(
                        new InventoryMenuComponentAlignment(InventoryMenuComponentAlignment.Direction.LEFT, InventoryMenuComponentAlignment.Direction.UP), 
                        createPowerSelectMenu()
                            .displayIcon(Material.GOLD_HOE)
                            .displayName("Powers")
                );
    }
    
//    public static InventoryMenuTemplateBuilder createTeamSpleefMenu() {
//        
//    }
//    
//    public static InventoryMenuTemplateBuilder createMultiSpleefMenu() {
//        
//    }
    
    private static void performChallenge(MenuChallenge builder, SpleefMode mode) {
        Collection<SLPlayer> targets = Arrays.asList(builder.getTarget());
        Challenge challenge = new Challenge(builder.getSource(), builder.getTarget()) {
            @Override
            public void start(SLPlayer[] accepted) {
                List<SpleefPlayer> players = new ArrayList<>();
                for (SLPlayer slpt : accepted) {
                    players.add(getSP(slpt));
                }
                Arena arena = builder.getArena();
                if (arena == null) {
                    List<Arena> potentialArenas = Arena.getAll()
                            .stream()
                            .filter(a -> a.getSpleefMode() == mode)
                            .filter(a -> !a.isOccupied() && !a.isPaused() && a.isRated() && a.isQueued())
                            .filter(a -> a.getRequiredPlayers() <= players.size())
                            .filter(a -> {
                                for (SpleefPlayer sp : players) {
                                    if (!a.isAvailable(sp)) {
                                        return false;
                                    }
                                }
                                return true;
                            })
                            .collect(Collectors.toList());
                    if (potentialArenas.isEmpty()) {
                        builder.getSource().sendMessage(SuperSpleef.getInstance().getChatPrefix() + ChatColor.RED + " There are currently no arenas available.");
                    }
                    Collections.shuffle(potentialArenas);
                    arena = potentialArenas.get(0);
                }
                arena.startBattle(players, BattleStartEvent.StartReason.CHALLENGE);
            }
        };
        targets.forEach(s -> s.addChallenge(challenge));
        challenge.sendMessages(SuperSpleef.getInstance().getChatPrefix(), builder.getArena() == null ? null : builder.getArena().getName(), targets);
    }
    
    private static InventoryMenuDialogTemplateBuilder<MenuChallenge> createChallengeDialog(InventoryMenuDialogHolderTemplateBuilder<MenuChallenge> arenaSelector, SpleefMode mode) {
        InventoryMenuDialogHolderTemplateBuilder<MenuChallenge> challengeSelectPlayerMenu = dialogMenu(MenuChallenge.class)
                .title("Select a player")
                .unsetFlags(InventoryMenuComponentFlag.EXIT_ON_NO_PERMISSION);
        SuperSpleef.getInstance().getPlayerManager()
                .getAll()
                .stream()
                .sorted((p1, p2) -> p1.getName().compareTo(p2.getName()))
                .forEach(p -> {
                    ItemStack skull = new ItemStack(Material.SKULL_ITEM);
                    skull.setDurability((short) 3);
                    SkullMeta skullMeta = (SkullMeta) skull.getItemMeta();
                    skullMeta.setOwner(p.getName());
                    skull.setItemMeta(skullMeta);
                    challengeSelectPlayerMenu.component(dialogButton(MenuChallenge.class)
                            .displayItem(skull)
                            .visibilityController(slp -> getSP(slp) != p)
                            .displayName(p.getName())
                            .description(x -> {
                                List<String> lines = new ArrayList<>();
                                SLPlayer slp = SpleefLeague.getInstance().getPlayerManager().get(p);
                                if(slp.getState() == PlayerState.INGAME) {
                                    lines.add(ChatColor.RED + "" + ChatColor.ITALIC + "Ingame");
                                }
                                lines.add(ChatColor.DARK_GRAY + "Player: " + ChatColor.GRAY + p.getName());
                                lines.add(ChatColor.DARK_GRAY + "Rating: " + ChatColor.GRAY + p.getRating(mode));
                                lines.add(ChatColor.DARK_GRAY + "Ping: " + ChatColor.GRAY + p.getPing());
                                return lines;
                            })
                            .accessController(slp -> SpleefLeague.getInstance().getPlayerManager().get(p).getState() != PlayerState.INGAME)
                            .onClick(e -> {
                                e.getBuilder().setTarget(p);
                            })
                    );
                });
        challengeSelectPlayerMenu.next(arenaSelector);
        return dialog(MenuChallenge.class)
                .start(challengeSelectPlayerMenu)
                .flags(InventoryMenuDialogFlag.EXIT_ON_COMPLETE_DIALOG)
                .builder(slp -> new MenuChallenge(getSP(slp)));
                
    }
    
    private static <A extends Arena> InventoryMenuTemplateBuilder createArenaMenu(GameQueue<A, SpleefPlayer> queue, boolean includeRandom) {
        InventoryMenuTemplateBuilder builder = menu();
        for(A arena : queue.getRegisteredArenas()) {
            InventoryMenuItemTemplateBuilder itemBuilder = item()
                    .displayIcon(Material.MAP)
                    .displayName(arena.getName())
                    .onClick(e -> {
                            SpleefPlayer sp = getSP(e.getPlayer());
                            queue.queuePlayer(sp, arena);
                            sp.sendMessage(SuperSpleef.getInstance().getChatPrefix() + ChatColor.GREEN + " You have been added to the queue for: " + org.bukkit.ChatColor.GREEN + arena.getName());
                            sp.closeInventory();
                    });
            int queueSize = Optional
                    .ofNullable(queue.getQueues().get(arena))
                    .map(s -> s.size())
                    .orElse(0);
            itemBuilder.description(x -> Arrays.asList("Players in queue: " + queueSize));
            builder.component(itemBuilder);
        }
        if(!includeRandom) {
            return builder;
        }
        InventoryMenuItemTemplateBuilder itemBuilder = item()
                .displayIcon(Material.EMPTY_MAP)
                .displayName("Random")
                .onClick(e -> {
                        SpleefPlayer sp = SuperSpleef.getInstance().getPlayerManager().get(e.getPlayer());
                        queue.queuePlayer(sp);
                        sp.sendMessage(SuperSpleef.getInstance().getChatPrefix() + ChatColor.GREEN + " You have been added to the queue.");
                        sp.closeInventory();
                })
                .description(x -> Arrays.asList("Queue up for", "a random, rated", "Spleef match."));
        int queueSize = Optional
                .ofNullable(queue.getQueues().get(null))
                .map(s -> s.size())
                .orElse(0);
        itemBuilder.description(x -> Arrays.asList(ChatColor.GRAY + "Players in queue: " + queueSize));
        builder.component(itemBuilder);
        return builder;
    }
    
    private static InventoryMenuDialogHolderTemplateBuilder<MenuChallenge> createArenaChallengeDialog(SpleefMode mode, boolean includeRandom) {
        InventoryMenuDialogHolderTemplateBuilder<MenuChallenge> builder = dialogMenu(MenuChallenge.class);
        GameQueue<? extends Arena, SpleefPlayer> queue = null;
        switch (mode) {
            case NORMAL:
                queue = SuperSpleef.getInstance().getNormalSpleefBattleManager().getGameQueue();
                break;
            case MULTI:
                queue = SuperSpleef.getInstance().getMultiSpleefBattleManager().getGameQueue();
                break;
            case TEAM:
                queue = SuperSpleef.getInstance().getTeamSpleefBattleManager().getGameQueue();
                break;
            case POWER:
                queue = SuperSpleef.getInstance().getPowerSpleefBattleManager().getGameQueue();
                break;
        }
        List<Arena> arenasSorted = new ArrayList<>(Arena.getAll());
        Collections.sort(arenasSorted, (a1, a2) -> a1.getName().compareTo(a2.getName()));
        for(Arena arena : arenasSorted) {
            if(arena.getSpleefMode() == mode) {
                InventoryMenuDialogButtonTemplateBuilder<MenuChallenge> itemBuilder = dialogButton(MenuChallenge.class)
                        .displayIcon(Material.MAP)
                        .displayName(arena.getName());
                if(queue != null) {
                    int queueSize = Optional
                            .ofNullable(queue.getQueues().get(arena))
                            .map(s -> s.size())
                            .orElse(0);
                    itemBuilder.description(x -> Arrays.asList("Players in queue: " + queueSize));
                }
                itemBuilder.onClick(b -> b.getBuilder().setArena(arena));
                builder.component(itemBuilder);
            }
        }
        if(!includeRandom) {
            return builder;
        }
        InventoryMenuDialogButtonTemplateBuilder<MenuChallenge> itemBuilder = dialogButton(MenuChallenge.class)
                        .displayIcon(Material.EMPTY_MAP)
                        .displayName("Random")
                        .description(x -> Arrays.asList("Queue up for", "a random, rated", "Spleef match."))
                        .onClick(b -> b.getBuilder().setArena(null));
        if(queue != null) {
            int queueSize = Optional
                    .ofNullable(queue.getQueues().get(null))
                    .map(s -> s.size())
                    .orElse(0);
            itemBuilder.description(x -> Arrays.asList(ChatColor.GRAY + "Players in queue: " + queueSize));
        }
        builder.component(itemBuilder);
        return builder;
    }
    
    private static InventoryMenuDialogTemplateBuilder createPowerSelectMenu() {
        InventoryMenuDialogHolderTemplateBuilder<Value<PowerType>> powerDialog = dialogMenu();
        for(PowerType powerType : PowerType.values()) {
            InventoryMenuDialogButtonTemplateBuilder<Value<PowerType>> button = dialogButton();
            button
                    .displayItem(powerType.getItem())
                    .description((slp) -> {
                        SpleefPlayer sp = getSP(slp);
                        List<String> description = powerType.getDescription();
                        if(sp.getPowerType() == powerType) {
                            description.add(ChatColor.GREEN + "Enabled");
                        }
                        else {   
                            description.add(ChatColor.RED + "Disabled");
                        }
                        return description;
                    })
                    .visibilityController((slp) -> {
//                            SpleefPlayer sp = playerManager.get(slp);
//                            if(sp != null) {
//                                return sp.getAvailablePowers().contains(powerType);
//                            }
//                            return false;
                        return true;
                    })

                    .onClick((e) -> {
                e.getBuilder().set(powerType);
            });
            powerDialog.component(button);
        }
        InventoryMenuDialogTemplateBuilder<Value<PowerType>> dialog = dialog();
        return dialog
                .start(powerDialog)
                .builder((slp) -> new Value<>())
                .unsetFlags(InventoryMenuDialogFlag.EXIT_ON_COMPLETE_DIALOG)
                .onDone((slp, v) -> {
                    getSP(slp).setActivePower(v.get());
                });
    }
    
    private static SpleefPlayer getSP(Player p) {
        return SuperSpleef.getInstance().getPlayerManager().get(p);
    }
    
    private static class MenuChallenge {
        
        private SLPlayer target;
        private final SLPlayer source;
        private Arena arena;

        public MenuChallenge(Player source) {
            this.source = SpleefLeague.getInstance().getPlayerManager().get(source);
        }

        public SLPlayer getSource() {
            return source;
        }
        
        public SLPlayer getTarget() {
            return target;
        }

        public void setTarget(Player player) {
            this.target = SpleefLeague.getInstance().getPlayerManager().get(player);
        }

        public Arena getArena() {
            return arena;
        }

        public void setArena(Arena arena) {
            this.arena = arena;
        }
    }
}
