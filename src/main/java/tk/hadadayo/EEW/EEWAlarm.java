package tk.hadadayo.EEW;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.Sound;
import org.bukkit.scheduler.BukkitRunnable;

public class EEWAlarm extends BukkitRunnable{
	public static boolean on = false;
	private static int count = 0;
	@Override
	public void run(){
		if(on){
			count = 50;
			on = false;
		}
		if(count > 0){
			for(Player player : Bukkit.getServer().getOnlinePlayers()){
				if (Bukkit.getVersion().contains("1.8")){
					player.playSound(player.getLocation(), "note.pling", 1000f , 1f);
				}else{
					player.playSound(player.getLocation(), Sound.valueOf("BLOCK_NOTE_PLING"), 1000f, 1f);
				}
			}	
			count--;
		}
	}
}