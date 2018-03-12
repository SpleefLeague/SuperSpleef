/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.spleefleague.superspleef.game.powerspleef;

import com.spleefleague.superspleef.player.SpleefPlayer;

/**
 *
 * @author jonas
 */
public abstract class PassivePower extends Power {

    public PassivePower(SpleefPlayer player, PowerType powerType) {
        super(player, powerType);
    }
    
    @Override
    public boolean tryExecute() {
        return false;
    }
}
