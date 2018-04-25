/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.spleefleague.superspleef.menu;

import com.spleefleague.core.SpleefLeague;
import com.spleefleague.gameapi.events.BattleStartEvent;
import com.spleefleague.core.player.PlayerState;
import com.spleefleague.core.player.SLPlayer;
import com.spleefleague.gameapi.queue.Challenge;
import com.spleefleague.gameapi.queue.GameQueue;
import static com.spleefleague.core.utils.inventorymenu.InventoryMenuAPI.dialog;
import static com.spleefleague.core.utils.inventorymenu.InventoryMenuAPI.dialogMenu;
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
import static com.spleefleague.core.utils.inventorymenu.InventoryMenuAPI.item;
import static com.spleefleague.core.utils.inventorymenu.InventoryMenuAPI.menu;
import com.spleefleague.core.utils.inventorymenu.InventoryMenuComponentFlag;
import com.spleefleague.core.utils.inventorymenu.dialog.InventoryMenuDialogFlag;
import com.spleefleague.gameapi.GamePlugin;
import com.spleefleague.superspleef.cosmetics.Shovel;
import com.spleefleague.superspleef.game.power.PowerType;
import com.spleefleague.superspleef.game.team.TeamSpleefArena;
import com.spleefleague.superspleef.game.team.TeamSpleefBattle;
import com.spleefleague.superspleef.game.team.TeamSpleefQueue;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.bukkit.Color;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;

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
                .component(5, () -> createTeamSpleefMenu().build())
                .component(6, () -> createMultiSpleefMenu().build())
                .component(8, () -> createShovelMenu().build())
                .flags(InventoryMenuFlag.MENU_CONTROL);
                
    }
    
    public static InventoryMenuTemplateBuilder createTeamSpleefMenu() {
        ItemStack helmet = new ItemStack(Material.LEATHER_HELMET, 1/*, (short)55*/);
        LeatherArmorMeta meta = (LeatherArmorMeta) helmet.getItemMeta();
        meta.setColor(Color.BLUE);
        helmet.setItemMeta(meta);
        return menu()
                .displayItem(helmet)
                .displayName("Team spleef")
                .component(createTeamSpleefArenaMenu())
                .flags(InventoryMenuFlag.SKIP_SINGLE_SUBMENU);
    }
    
    
    public static InventoryMenuTemplateBuilder createClassicSpleefMenu() {
        return createArenaMenu(SuperSpleef.getInstance().getClassicSpleefBattleManager().getGameQueue(), true)
                .title("Arenas")
                .displayItem(new ItemStack(Material.DIAMOND_SPADE, 1/*, (short)1561*/))
                .displayName("Classic Spleef")
                .component(
                        new InventoryMenuComponentAlignment(InventoryMenuComponentAlignment.Direction.LEFT, InventoryMenuComponentAlignment.Direction.UP), 
                        createChallengeDialog(createArenaChallengeDialog(SpleefMode.CLASSIC, true), SpleefMode.CLASSIC)
                    .displayItem(new ItemStack(Material.GOLD_HOE, 1, (short)32))
                    .displayName("Challenge")
                    .onDone((slp, builder) -> performChallenge(builder, SpleefMode.CLASSIC))
                );
    }
    
    public static InventoryMenuTemplateBuilder createPowerSpleefMenu() {
        return createArenaMenu(SuperSpleef.getInstance().getPowerSpleefBattleManager().getGameQueue(), true)
                .title("Arenas")
                .displayItem(new ItemStack(Material.GOLD_SPADE, 1/*, (short)32*/))
                .displayName("Power Spleef")
                .component(
                        new InventoryMenuComponentAlignment(InventoryMenuComponentAlignment.Direction.LEFT, InventoryMenuComponentAlignment.Direction.UP), 
                        createChallengeDialog(createArenaChallengeDialog(SpleefMode.POWER, true), SpleefMode.POWER)
                                .displayItem(new ItemStack(Material.GOLD_HOE, 1, (short)32))
                                .displayName("Challenge")
                                .unsetFlags(InventoryMenuComponentFlag.EXIT_ON_NO_PERMISSION)
                                .accessController(slp -> getSP(slp).getPowerType() != PowerType.EMPTY_POWER)
                                .description(slp -> {
                                    if(getSP(slp).getPowerType() == PowerType.EMPTY_POWER) {
                                        return Arrays.asList(ChatColor.RED + "Please select a power first");
                                    }
                                    else {
                                        return new ArrayList<>();
                                    }
                                })
                                .onDone((slp, builder) -> performChallenge(builder, SpleefMode.POWER))
                )
                .component(
                        new InventoryMenuComponentAlignment(InventoryMenuComponentAlignment.Direction.LEFT, InventoryMenuComponentAlignment.Direction.UP), 
                        createPowerSelectMenu()
                            .displayItem(slp -> getSP(slp).getPowerType().getItem())
                            .displayName("Powers")
                            .description(slp -> Collections.emptyList())
                );
    }
    
    public static InventoryMenuTemplateBuilder createMultiSpleefMenu() {
        return createArenaMenu(SuperSpleef.getInstance().getMultiSpleefBattleManager().getGameQueue(), true)
                .title("Arenas")
                .displayItem(new ItemStack(Material.SHEARS, 1/*, (short)238*/))
                .displayName("Multi Spleef");
    }
    
    private static void performChallenge(MenuChallenge builder, SpleefMode mode) {
        Collection<SLPlayer> targets = Arrays.asList(builder.getTarget());
        SpleefPlayer spSource = getSP(builder.getSource());
        SpleefPlayer spTarget = getSP(builder.getTarget());
        Challenge<SpleefPlayer> challenge = new Challenge<SpleefPlayer>(spSource, Arrays.asList(spTarget)) {
            @Override
            public void start(List<SpleefPlayer> players) {
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
        challenge.sendMessages(mode.getChatPrefix(), builder.getArena() == null ? "a random arena" : builder.getArena().getName(), targets);
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
        queue.getRegisteredArenas()
                .stream()
                .sorted((a1, a2) -> a1.getName().compareTo(a2.getName()))
                .forEach(arena -> {
                    InventoryMenuItemTemplateBuilder itemBuilder = item()
                            .displayIcon(Material.MAP)
                            .displayName(arena.getName())
                            .onClick(e -> {
                                    SpleefPlayer sp = getSP(e.getPlayer());
                                    GamePlugin.dequeueGlobal(sp);
                                    queue.queuePlayer(sp, arena);
                                    sp.sendMessage(SuperSpleef.getInstance().getChatPrefix() + ChatColor.GREEN + " You have been added to the queue for: " + org.bukkit.ChatColor.GREEN + arena.getName());
                                    sp.closeInventory();
                            });
                    int queueSize = Optional
                            .ofNullable(queue.getQueues().get(arena))
                            .map(s -> s.size())
                            .orElse(0);
                    List<String> description = new ArrayList<>(arena.getDescription());
                    description.add(ChatColor.GRAY + "Players in queue: " + queueSize);
                    itemBuilder.description(x -> description);
                    builder.component(itemBuilder);
                });
        if(!includeRandom) {
            return builder;
        }
        InventoryMenuItemTemplateBuilder itemBuilder = item()
                .displayIcon(Material.EMPTY_MAP)
                .displayName("Random")
                .onClick(e -> {
                        SpleefPlayer sp = SuperSpleef.getInstance().getPlayerManager().get(e.getPlayer());
                        GamePlugin.dequeueGlobal(sp);
                        queue.queuePlayer(sp);
                        sp.sendMessage(SuperSpleef.getInstance().getChatPrefix() + ChatColor.GREEN + " You have been added to the queue.");
                        sp.closeInventory();
                });
        int queueSize = Optional
                .ofNullable(queue.getQueues().get(null))
                .map(s -> s.size())
                .orElse(0);
        List<String> description = new ArrayList<>(Arrays.asList("Queue up for", "a random, rated", "Spleef match."));
        description.add(ChatColor.GRAY + "Players in queue: " + queueSize);
        itemBuilder.description(x -> description);
        builder.component(itemBuilder);
        return builder;
    }
    
    private static InventoryMenuDialogHolderTemplateBuilder<MenuChallenge> createArenaChallengeDialog(SpleefMode mode, boolean includeRandom) {
        InventoryMenuDialogHolderTemplateBuilder<MenuChallenge> builder = dialogMenu(MenuChallenge.class);
        GameQueue<? extends Arena, SpleefPlayer> queue = null;
        switch (mode) {
            case CLASSIC:
                queue = SuperSpleef.getInstance().getClassicSpleefBattleManager().getGameQueue();
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
        List<Arena<?>> arenasSorted = new ArrayList<>(Arena.getAll());
        Collections.sort(arenasSorted, (a1, a2) -> a1.getName().compareTo(a2.getName()));
        for(Arena arena : arenasSorted) {
            if(arena.getSpleefMode() == mode) {
                InventoryMenuDialogButtonTemplateBuilder<MenuChallenge> itemBuilder = dialogButton(MenuChallenge.class)
                        .displayIcon(Material.MAP)
                        .displayName(arena.getName())
                        .description(s -> arena.getDescription());
                if(queue != null) {
                    int queueSize = Optional
                            .ofNullable(queue.getQueues().get(arena))
                            .map(s -> s.size())
                            .orElse(0);
                    List<String> description = new ArrayList<>(arena.getDescription());
                    description.add(ChatColor.GRAY + "Players in queue: " + queueSize);
                    itemBuilder.description(x -> description);
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
            List<String> description = new ArrayList<>(Arrays.asList("Queue up for", "a random, rated", "Spleef match."));
            description.add(ChatColor.GRAY + "Players in queue: " + queueSize);
            itemBuilder.description(x -> description);
        }
        builder.component(itemBuilder);
        return builder;
    }
    
    private static InventoryMenuDialogTemplateBuilder createTeamSpleefArenaMenu() {
        InventoryMenuDialogHolderTemplateBuilder<TeamspleefQueueElement> arenaHolder = dialogMenu(TeamspleefQueueElement.class);
        arenaHolder.title("Arenas");
        TeamSpleefQueue queue = (TeamSpleefQueue)SuperSpleef.getInstance().getTeamSpleefBattleManager().getGameQueue();
        for(TeamSpleefArena arena : queue.getRegisteredArenas()) {
            InventoryMenuDialogButtonTemplateBuilder<TeamspleefQueueElement> itemBuilder = dialogButton(TeamspleefQueueElement.class)
                    .displayIcon(Material.MAP)
                    .displayName(arena.getName())
                    .onClick(e -> e.getBuilder().setArena(arena));
            int queueSize = Optional
                    .ofNullable(queue.getArenaQueues().get(arena))
                    .map(s -> s.size())
                    .orElse(0);
            List<String> description = new ArrayList<>(arena.getDescription());
            description.add(ChatColor.GRAY + "Players in queue: " + queueSize);
            itemBuilder.description(x -> description);
            InventoryMenuDialogHolderTemplateBuilder<TeamspleefQueueElement> teamSelector = generateTeamSelector(arena);
            itemBuilder.next(teamSelector);
            arenaHolder.component(itemBuilder);
        }
        InventoryMenuDialogButtonTemplateBuilder<TeamspleefQueueElement> itemBuilder = dialogButton(TeamspleefQueueElement.class)
                .displayIcon(Material.EMPTY_MAP)
                .displayName("Random");
        int queueSize = Optional
                .ofNullable(queue.getArenaQueues().get(null))
                .map(s -> s.size())
                .orElse(0);
        List<String> description = new ArrayList<>(Arrays.asList("Queue up for", "a random, rated", "Spleef match."));
        description.add(ChatColor.GRAY + "Players in queue: " + queueSize);
        itemBuilder.description(x -> description);
        arenaHolder.component(itemBuilder);
        return dialog(TeamspleefQueueElement.class)
                .start(arenaHolder)
                .builder(slp -> new TeamspleefQueueElement(getSP(slp)))
                .flags(InventoryMenuDialogFlag.EXIT_ON_COMPLETE_DIALOG)
                .onDone((slp, tqe) -> {
                    GamePlugin.dequeueGlobal(slp);
                    if(tqe.getTeam() == null) {
                        queue.queuePlayer(tqe.getPlayer(), tqe.getArena());
                    }
                    else {
                        queue.queuePlayer(tqe.getPlayer(), tqe.getArena(), tqe.getTeam());
                    }
                });
    }
    
    private static InventoryMenuDialogHolderTemplateBuilder<TeamspleefQueueElement> generateTeamSelector(TeamSpleefArena arena) {
        InventoryMenuDialogHolderTemplateBuilder<TeamspleefQueueElement> teamHolder = dialogMenu();
        teamHolder.title("Teams");
        Map<Integer, Set<SpleefPlayer>> teams = ((TeamSpleefQueue)SuperSpleef
                .getInstance()
                .getTeamSpleefBattleManager()
                .getGameQueue())
                .getTeamQueues()
                .get(arena);
        for(int i = 0; i < arena.getTeamSizes().length; i++) {
            int teamId = i;
            InventoryMenuDialogButtonTemplateBuilder<TeamspleefQueueElement> button = dialogButton(TeamspleefQueueElement.class)
                    .displayItem(TeamSpleefBattle.teamBlocks[i])
                    .displayName(TeamSpleefBattle.names[i] + " team")
                    .onClick(e -> e.getBuilder().setTeam(teamId));
            teams.get(i)
                    .stream()
                    .sorted((p1, p2) -> p1.getName().compareTo(p2.getName()))
                    .forEach(p -> button.description(p.getName()));
            teamHolder.component(button);
        }
        return teamHolder;
    }
    
    private static InventoryMenuTemplateBuilder createPowerSelectMenu() {
        InventoryMenuTemplateBuilder powerDialog = menu();
        for(PowerType powerType : PowerType.values()) {
            InventoryMenuItemTemplateBuilder button = item();
            button
                    .displayItem(powerType.getItem())
                    .displayName((slp) -> {
                        SpleefPlayer sp = getSP(slp);
                        String name = powerType.getDisplayName();
                        if(sp.getPowerType() == powerType) {
                            return powerType.getDisplayName() + ChatColor.GREEN + " [Selected]";
                        }
                        return powerType.getDisplayName();
                    })
                    .description((slp) -> powerType.getDescription())
                    .visibilityController((slp) -> {
                        PowerType pt = getSP(slp).getPowerType();
                        return powerType != PowerType.EMPTY_POWER || pt == PowerType.EMPTY_POWER;
                    })
                    .onClick((e) -> {
                        getSP(e.getPlayer()).setActivePower(powerType);
                        e.getItem().getParent().update();
            });
            powerDialog.component(button);
        }
        return powerDialog;
    }
    
    private static InventoryMenuTemplateBuilder createShovelMenu() {
        InventoryMenuTemplateBuilder shovelMenu = menu()
        .displayName("Shovels")
        .displayItem((slp) -> {
            SpleefPlayer sp = getSP(slp);
            return sp.getActiveShovel().toItemStack();
        })
        .staticComponent(4, 5, item()
                .displayItem((slp) -> {
                    SpleefPlayer sp = getSP(slp);
                    return sp.getActiveShovel().toItemStack();
                })
        );

        Shovel.getAll()
                .stream()
                .sorted((s1, s2) -> Short.compare(s1.getDamage(), s2.getDamage()))
                .forEach((shovel) -> {
                    shovelMenu.component(item()
                            .displayItem((slp) -> {
                                SpleefPlayer sp = getSP(slp);
                                if(shovel.isIsDefault() || sp.getAvailableShovels().contains(shovel)) {
                                    return shovel.toItemStack();
                                }
                                ItemStack lock = new ItemStack(Material.DIAMOND_AXE, 1, (short)12);
                                ItemMeta meta = lock.getItemMeta();
                                meta.setDisplayName(org.bukkit.ChatColor.GRAY + "" + org.bukkit.ChatColor.ITALIC + "Locked");
                                lock.setItemMeta(meta);
                                return lock;
                            })
                            .onClick((event) -> {
                                SpleefPlayer sp = getSP(event.getPlayer());
                                if(!shovel.isIsDefault() && !sp.getAvailableShovels().contains(shovel)) {
                                    return;
                                }
                                sp.setActiveShovel(shovel);
                                event.getItem().getParent().update();
                            })
                    );
                });
        return shovelMenu;
    }
    
    private static SpleefPlayer getSP(Player p) {
        return SuperSpleef.getInstance().getPlayerManager().get(p);
    }
    
    private static class TeamspleefQueueElement {
        
        private final SpleefPlayer player;
        private TeamSpleefArena arena;
        private Integer team;

        public TeamspleefQueueElement(SpleefPlayer player) {
            this.player = player;
        }
        
        public SpleefPlayer getPlayer() {
            return player;
        }

        public TeamSpleefArena getArena() {
            return arena;
        }

        public void setArena(TeamSpleefArena arena) {
            this.arena = arena;
        }

        public Integer getTeam() {
            return team;
        }

        public void setTeam(Integer team) {
            this.team = team;
        }
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
