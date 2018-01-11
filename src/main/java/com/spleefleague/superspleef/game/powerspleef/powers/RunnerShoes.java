/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.spleefleague.superspleef.game.powerspleef.powers;

import com.spleefleague.superspleef.game.powerspleef.Power;
import com.spleefleague.superspleef.game.powerspleef.PowerType;
import com.spleefleague.superspleef.player.SpleefPlayer;
import java.util.function.Function;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/**
 *
 * @author jonas
 */
public class RunnerShoes extends Power {
    
    private final PotionEffect effect;
    
    private RunnerShoes(SpleefPlayer sp) {
        super(PowerType.ROLLER_SPADES, sp, 20 * 20);
        effect = new PotionEffect(PotionEffectType.SPEED, 10 * 20, 2, true, false);
    }
    
    @Override
    public boolean execute() {
        this.getPlayer().addPotionEffect(effect);
        return true;
    }

    @Override
    public void cancel() {
        this.getPlayer().removePotionEffect(PotionEffectType.SPEED);
    }

    @Override
    public void destroy() {
        cancel();
    }
    
    public static Function<SpleefPlayer, ? extends Power> getSupplier() {
        return (sp) -> new RunnerShoes(sp);
    }
}
