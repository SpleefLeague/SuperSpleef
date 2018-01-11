package com.spleefleague.superspleef.game.powerspleef.powers;

import com.spleefleague.superspleef.SuperSpleef;
import com.spleefleague.superspleef.game.powerspleef.Power;
import com.spleefleague.superspleef.game.powerspleef.PowerType;
import com.spleefleague.superspleef.player.SpleefPlayer;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.util.Vector;

/**
 *
 * @author balsfull
 */
public class QuickStep extends Power implements Listener {
    
    private final double STRENGTH = 1.8;
    private Vector direction;
    
    private QuickStep(SpleefPlayer player) {
        super(PowerType.QUICK_STEP, player, 10);
        Bukkit.getPluginManager().registerEvents(this, SuperSpleef.getInstance());
        direction = player.getLocation().getDirection().normalize();
    }

    @Override
    public boolean execute() {
        getPlayer().setVelocity(direction.multiply(STRENGTH));
        return true;
    }
    
    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        if(event.getFrom().distanceSquared(event.getTo()) > 0) {
            direction = event.getTo()
                    .toVector()
                    .subtract(event.getFrom().toVector())
                    .setY(0)
                    .normalize();
        }
        else {
            direction = event.getPlayer()
                    .getLocation()
                    .getDirection()
                    .setY(0)
                    .normalize();
        }
        System.out.println(direction.getX() + " | " + direction.getZ());
    }
    
    public static Function<SpleefPlayer, ? extends Power> getSupplier() {
        return (sp) -> new QuickStep(sp);
    }

    @Override
    public void cancel() {
    
    }

    @Override
    public void destroy() {
        HandlerList.unregisterAll(this);
    }
}
