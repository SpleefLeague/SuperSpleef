/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.spleefleague.superspleef.game.power.powers;

import com.spleefleague.superspleef.game.power.CooldownPower;
import com.spleefleague.superspleef.game.power.PowerType;
import com.spleefleague.superspleef.player.SpleefPlayer;
import java.util.function.Function;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/**
 *
 * @author jonas
 */
public class SpeedBoost extends CooldownPower {

    private final int maxDuration;
    
    public SpeedBoost(SpleefPlayer sp, int cooldown, int maxDuration) {
        super(sp, PowerType.SPEED_BOOST, cooldown);
        this.maxDuration = maxDuration;
    }
    
    @Override
    public boolean tryExecute() {
        int currentSpeed = -1;
        PotionEffect pe = getPlayer().getPotionEffect(PotionEffectType.SPEED);
        if(pe != null) {
            currentSpeed = pe.getAmplifier();
        }
        if(currentSpeed > -1 && currentSpeed < 2) {
            execute();
            return true;
        }
        else if(currentSpeed == -1) {
            return super.tryExecute();
        }
        else {
            return false;
        }
    }

    @Override
    public void execute() {
        SpleefPlayer sp = getPlayer();
        PotionEffect speed = sp.getPotionEffect(PotionEffectType.SPEED);
        if(speed == null) {
            speed = PotionEffectType.SPEED.createEffect(maxDuration, 0);
        }
        else {
            int durationLeft = speed.getDuration();
            int strength = speed.getAmplifier();
            if(durationLeft > 1) {
                speed = PotionEffectType.SPEED.createEffect((int)Math.floor(durationLeft * (2/3.0)), strength + 1);
            }
        }
        showDuration("Speed " + getRomanNumeral(speed.getAmplifier() + 1), speed.getDuration());
        sp.addPotionEffect(speed, true);
    }
    
    private String getRomanNumeral(int i) {
        switch(i) {
            case 1: return "I";
            case 2: return "II";
            case 3: return "III";
            case 4: return "IV";
            case 5: return "V";
            default: return Integer.toString(i);
        }
    }
    
    public static Function<SpleefPlayer, SpeedBoost> getSupplier() {
        return s -> new SpeedBoost(s, 350, 200);
    }
}
