
import com.spleefleague.core.SpleefLeague;
import com.spleefleague.core.utils.Debugger;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.weather.WeatherChangeEvent;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Jonas
 */
public class SpleefMoveTest implements Listener, Debugger {
    
    @Override
    public void debug() {
        SpleefLeague.DEFAULT_WORLD.setStorm(false);
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onWeather(WeatherChangeEvent event) {
        event.setCancelled(false);
    }
}
