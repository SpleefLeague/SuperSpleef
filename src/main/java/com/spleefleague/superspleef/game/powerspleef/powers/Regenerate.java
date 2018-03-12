/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.spleefleague.superspleef.game.powerspleef.powers;

import com.spleefleague.superspleef.game.SpleefBattle;
import com.spleefleague.superspleef.game.powerspleef.CooldownPower;
import com.spleefleague.superspleef.game.powerspleef.PowerType;
import com.spleefleague.superspleef.player.SpleefPlayer;
import java.util.function.Function;
import org.bukkit.Material;

/**
 *
 * @author jonas
 */
public class Regenerate extends CooldownPower {

    private final int range = 4;
    
    public Regenerate(SpleefPlayer sp, int cooldown) {
        super(sp, PowerType.REGENERATE, cooldown);
    }

    @Override
    public void execute() {
        SpleefPlayer sp = getPlayer();
        SpleefBattle battle = getBattle();
        battle.getFieldBlocks()
                .stream()
                .filter(fb -> fb.getType() == Material.AIR)
                .filter(fb -> fb
                        .getLocation()
                        .clone()
                        .add(0.5, 0, 0.5)
                        .distanceSquared(sp.getLocation().getBlock().getLocation()) <= range * range)
                .forEach(fb -> fb.setType(Material.SNOW_BLOCK));
    }
    
    public static Function<SpleefPlayer, Regenerate> getSupplier() {
        return s -> new Regenerate(s, 40);
    }
}
