package com.spleefleague.superspleef.game.powerspleef.powers;

import com.spleefleague.core.SpleefLeague;
import com.spleefleague.superspleef.SuperSpleef;
import com.spleefleague.superspleef.game.powerspleef.Power;
import com.spleefleague.superspleef.game.powerspleef.PowerType;
import com.spleefleague.superspleef.player.SpleefPlayer;
import com.spleefleague.fakeblocks.chunk.BlockData;
import com.spleefleague.fakeblocks.packet.FakeBlock;
import java.util.HashSet;
import java.util.Objects;
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
    private final int degenerationDuration = 1 * 20;
    private final BlockData[] transition = new BlockData[]{
        
        new BlockData(Material.DIAMOND_BLOCK, (byte)0),
        new BlockData(Material.GOLD_BLOCK, (byte)0),
        new BlockData(Material.IRON_BLOCK, (byte)0)
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
            private boolean modified;
            private final SpleefPlayer player = getPlayer();
            
            @Override
            public void run() {
                System.out.println("Timer: " + timer);
                timer--;
                if(timer <= 0 && affected.isEmpty()) {
                    task.cancel();
                    return;
                }
                modified = false;
                System.out.println("Transition: " + transition.length);
                for(DegeneratingBlock db : affected) {
                    if(db.durationLeft == 0) {
                        db.state++;
                        db.durationLeft = degenerationDuration;
                        if(db.state < transition.length) {
                            db.backingBlock.setType(transition[db.state].getType());
                            db.backingBlock.setDamageValue(transition[db.state].getDamage());
                        }
                        else {
                            db.backingBlock.setType(Material.AIR);
                            db.backingBlock.setDamageValue((byte)0);
                        }
                        modified = true;
                    }
                    System.out.println("State: " + db.state);
                    System.out.println(db.backingBlock.getType());
                }
                affected.removeIf(db -> db.state >= transition.length);
                affected.forEach(db -> db.durationLeft--);
                if(timer > 0) {
                    Location loc = player.getLocation().subtract(0, 1, 0).getBlock().getLocation();
                    player
                            .getCurrentBattle()
                            .getField()
                            .getBlocks()
                            .stream()
                            .filter((block) -> (block.getLocation().equals(loc)))
                            .findAny()
                            .ifPresent(fb -> {
                                if(fb.getType() != Material.AIR) {
                                    DegeneratingBlock db = new DegeneratingBlock(0, degenerationDuration, fb);
                                    if(!affected.contains(db)) {
                                        System.out.println("New");
                                        affected.add(db);
                                        fb.setType(transition[0].getType());
                                        fb.setDamageValue(transition[0].getDamage());
                                        modified = true;
                                    }
                                    
                                }
                            });
                }
                System.out.println("Affected: " + affected.size());
                if(modified) {
                    System.out.println("Updating");
                    SpleefLeague.getInstance().getFakeBlockHandler().update(player.getCurrentBattle().getField());
                }
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

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 59 * hash + Objects.hashCode(this.backingBlock);
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final DegeneratingBlock other = (DegeneratingBlock) obj;
            if (!Objects.equals(this.backingBlock, other.backingBlock)) {
                return false;
            }
            return true;
        }
        
        
    }
}
