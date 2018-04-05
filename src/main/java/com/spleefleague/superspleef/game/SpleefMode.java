/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.spleefleague.superspleef.game;

import org.bukkit.ChatColor;

/**
 *
 * @author Jonas
 */
public enum SpleefMode {
    CLASSIC("ClassicSpleef"),
    TEAM("TeamSpleef"),
    MULTI("MultiSpleef"),
    POWER("PowerSpleef"),
    SWC("SWC");
    
    private final String chatPrefixName;
    
    private SpleefMode(String chatPrefixName) {
        this.chatPrefixName = chatPrefixName;
    }
    
    public String getChatPrefix() {
        return ChatColor.GRAY + "[" + ChatColor.GOLD + chatPrefixName + ChatColor.GRAY + "]" + ChatColor.RESET;
    }
}
