package com.spleefleague.superspleef.game.powerspleef.powers;

import com.spleefleague.superspleef.SuperSpleef;
import com.spleefleague.superspleef.game.powerspleef.Power;
import com.spleefleague.superspleef.game.powerspleef.PowerType;
import com.spleefleague.superspleef.player.SpleefPlayer;
import com.spleefleague.superspleef.game.scoreboards.BlockData;
import com.spleefleague.virtualworld.api.FakeBlock;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.scheduler.BukkitTask;

/**
 *
 * @author balsfull
 */
public class RollerSpades extends Power {

    private final int duration = 8 * 20;
    private final int degenerationDuration = 2 * 20;
    private final Material[] transition = new Material[]{
        Material.STONE,
        Material.COAL
    };
    private BukkitTask task;
    
    private RollerSpades(SpleefPlayer sp) {
        super(PowerType.ROLLER_SPADES, sp, 20 * 20);
    }

    @Override
    public boolean execute() {
        task = Bukkit.getScheduler().runTaskTimer(SuperSpleef.getInstance(), new Runnable() {
            
            private final Set<DegeneratingBlock> affected = new HashSet<>();
            private int timer = duration;
            private final SpleefPlayer player = getPlayer();
            
            @Override
            public void run() {
                timer--;
                if(timer <= 0 && affected.isEmpty()) {
                    task.cancel();
                    return;
                }
                for(DegeneratingBlock db : affected) {
                    if(db.durationLeft == 0) {
                        db.state++;
                        db.durationLeft = degenerationDuration;
                        if(db.state < transition.length) {
                            db.backingBlock.setType(transition[db.state]);
                        }
                        else {
                            db.backingBlock.setType(Material.AIR);
                            db.backingBlock.setData((byte)0);
                        }
                    }
                }
                affected.removeIf(db -> db.state > transition.length);
                affected.forEach(db -> db.durationLeft--);
                Location loc = player.getLocation().subtract(0, -1, 0).getBlock().getLocation();
                player
                        .getCurrentBattle()
                        .getFieldBlocks()
                        .stream()
                        .filter((block) -> (block.getLocation().equals(loc)))
                        .findAny()
                        .ifPresent(fb -> {
                            affected.add(new DegeneratingBlock(0, degenerationDuration, fb));
                            fb.setType(transition[0]);
                        });
            }
        }, 0, 1);
        return true;
    }

    @Override
    public void cancel() {
        if(task != null) {
            task.cancel();
        }
    }
    
    public static Function<SpleefPlayer, ? extends Power> getSupplier() {
        return (sp) -> new RollerSpades(sp);
    }
    
    private class DegeneratingBlock {
        
        private int state;
        private int durationLeft;
        private final FakeBlock backingBlock;

        public DegeneratingBlock(int state, int durationLeft, FakeBlock backingBlock ) {
            this.state = state;
            this.durationLeft = durationLeft;
            this.backingBlock = backingBlock;
        }

        public FakeBlock getBackingBlock() {
            return backingBlock;
        }

        public int getState() {
            return state;
        }

        public void setState(int state) {
            this.state = state;
        }

        public int getDurationLeft() {
            return durationLeft;
        }

        public void setDurationLeft(int durationLeft) {
            this.durationLeft = durationLeft;
        }
    }
}
