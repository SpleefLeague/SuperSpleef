/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.spleefleague.superspleef.game.power.powers;

import com.spleefleague.superspleef.SuperSpleef;
import com.spleefleague.superspleef.game.SpleefBattle;
import com.spleefleague.superspleef.game.power.CooldownPower;
import com.spleefleague.superspleef.game.power.PowerType;
import com.spleefleague.superspleef.player.SpleefPlayer;
import com.spleefleague.virtualworld.api.FakeBlock;
import com.spleefleague.virtualworld.api.FakeWorld;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

/**
 *
 * @author jonas
 */
public class Nuke extends CooldownPower {

    private BukkitTask activeTask;
    private final double outerRange = 5.5;
    private final double innerRange = 1.5;
    private final int explotionDelay = 30;
    private final int regenDelay = 25;
    
    public Nuke(SpleefPlayer sp, int cooldown) {
        super(sp, PowerType.NUKE, cooldown);
    }

    @Override
    public void execute() {
        resetTask();
        FakeWorld fworld = getBattle().getFakeWorld();
        fworld.playSound(getPlayer().getLocation(), Sound.ENTITY_BLAZE_SHOOT, 1.0f, 0.4f);
        activeTask = Bukkit.getScheduler().runTaskTimer(SuperSpleef.getInstance(), () -> {
            getTargetedBlocks()
                .stream()
                .map(fb -> fb.getLocation().clone().add(new Vector(0.5, 1, 0.5)))
                .forEach(v -> fworld.spawnParticle(Particle.FLAME, v, 1, 0, 0, 0, 0));
        }, 0, 5);
        this.showDuration("Fuse", explotionDelay);
        Bukkit.getScheduler().runTaskLater(SuperSpleef.getInstance(), () -> explode(), explotionDelay);
    }
    
    @Override
    public void cleanupRound() {
        super.cleanupRound();
        resetTask();
    }
    
    private void resetTask() {
        if(activeTask != null) {
            activeTask.cancel();
            activeTask = null;
        }
    }
    
    private List<FakeBlock> getTargetedBlocks() {
        SpleefPlayer sp = getPlayer();
        return getBattle().getFieldBlocks()
                .stream()
                .filter(fb -> fb.getType() == Material.SNOW_BLOCK)
                .filter(fb -> {
                        Location fbl = fb.getLocation().clone();
                        fbl.setY(0);
                        Location spl = sp.getLocation().getBlock().getLocation();
                        spl.setY(0);
                        //Horizontal distance
                        double distance = fbl.distanceSquared(spl);
                        return distance <= outerRange * outerRange && distance >= innerRange * innerRange;
                })
                .collect(Collectors.toList());
    }
    
    private void explode() {
        if(activeTask != null) {
            resetTask();
        }
        else {
            //Flame task was cancelled, no more explotion pls
            return;
        }
        SpleefPlayer sp = getPlayer();
        SpleefBattle battle = getBattle();
        List<FakeBlock> blocks = getTargetedBlocks();
        blocks.forEach(fb -> fb.setType(Material.AIR));
        FakeWorld fworld = battle.getFakeWorld();
        fworld.spawnParticle(Particle.EXPLOSION_LARGE, sp.getLocation(), 10, outerRange / 2, outerRange / 2, outerRange / 2);
        fworld.playSound(sp.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 1, 1);
        Collections.shuffle(blocks);
        Collections.sort(blocks, (f1, f2) -> Double.compare(
                        f1.getLocation().distanceSquared(sp.getLocation().getBlock().getLocation()), 
                        f2.getLocation().distanceSquared(sp.getLocation().getBlock().getLocation())));
        activeTask = Bukkit.getScheduler().runTaskTimer(SuperSpleef.getInstance(), getRegenRunnable(blocks), regenDelay, 1);
    }
    
    private Runnable getRegenRunnable(Collection<FakeBlock> regenBlocks) {
        return new Runnable() {
            
            private int skippedTicks = 0;
            private final int maxSkippedTicks = 10;
            private final double skipProbability = 0.5;
            private final int minRegen = 1;
            private final int maxRegen = 3;
            private final Random rand = new Random();
            private final Iterator<FakeBlock> blocksToRegen = regenBlocks.iterator();
            
            @Override
            public void run() {
                if(skippedTicks < maxSkippedTicks && Math.random() < skipProbability) {
                   skippedTicks++; 
                   return;
                }
                skippedTicks = 0;
                int regen = rand.nextInt(maxRegen - minRegen) + minRegen;
                while(regen > 0 && blocksToRegen.hasNext()) {
                    regen--;
                    blocksToRegen.next().setType(Material.SNOW_BLOCK);
                }
                if(activeTask != null && !blocksToRegen.hasNext()) {
                    activeTask.cancel();
                }
            }
        };
    }
    
    public static Function<SpleefPlayer, Nuke> getSupplier() {
        return sp -> new Nuke(sp, 600);
    }
}
