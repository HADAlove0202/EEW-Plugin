package tk.hadadayo.EEW;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.FileInputStream;
import java.io.BufferedReader;
import java.nio.charset.StandardCharsets;
import java.io.FileNotFoundException;
import java.io.IOException;

public class EEW extends JavaPlugin{
	public static FileConfiguration config;
	private static FileConfiguration lang;
	private static int id;
	private static int alert_id;
	public static int status;
	public static int earthquakecount = 0;
	public static int reportcount = 0;
	@Override
	public void onEnable(){
		loadConfig();
		loadLang();
		save(config,"config.yml");
		save(lang,"lang.yml");
		if(this.config.getBoolean("EEW")){
			id = new EEWListener().runTaskTimer(this, 0, this.config.getInt("tick")).getTaskId();
			new EEWAlarm().runTaskTimer(this, 0, 2);
			getLogger().info(getText("eew-on"));
			this.status = 0;
		}else{
			getLogger().info(getText("eew-off"));
			this.status = 1;
		}
		super.onEnable();
	}
	public void onDisable(){
		super.onDisable();
	}
	public static String getText(String text){
		return lang.getString(text).replaceAll("%n","\n").replaceAll("&","\u00A7");
	}
	private void loadConfig(){
		File configfile = new File(getDataFolder(), "config.yml");
		if (!configfile.exists()) {
			saveResource("config.yml", false);
		}
		try {
			config = YamlConfiguration.loadConfiguration(new BufferedReader(new InputStreamReader(new FileInputStream(new File(getDataFolder(), "config.yml")),StandardCharsets.UTF_8)));
		} catch (FileNotFoundException e) {
			getLogger().warning(e.getMessage());
		}
		final InputStream defConfigStream = this.getResource("config.yml");
		config.setDefaults(YamlConfiguration.loadConfiguration(new BufferedReader(new InputStreamReader(defConfigStream, StandardCharsets.UTF_8))));
		config.options().copyDefaults(true);
	}
	private void loadLang(){
		File langfile = new File(getDataFolder(), "lang.yml");
		if (!langfile.exists()) {
			saveResource("lang.yml", false);
		}
		try {
			lang = YamlConfiguration.loadConfiguration(new BufferedReader(new InputStreamReader(new FileInputStream(new File(getDataFolder(), "lang.yml")),StandardCharsets.UTF_8)));
		} catch (FileNotFoundException e) {
			getLogger().warning(e.getMessage());
		}
		final InputStream defConfigStream = this.getResource("lang.yml");
		lang.setDefaults(YamlConfiguration.loadConfiguration(new BufferedReader(new InputStreamReader(defConfigStream, StandardCharsets.UTF_8))));
		lang.options().copyDefaults(true);
	}
	private void save(FileConfiguration file, String name){
		try {
			file.save(new File(getDataFolder(), name));
		} catch (IOException e) {
			getLogger().warning(e.getMessage());
		}
	}
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
		if (args.length == 1) {
			if(args[0].equalsIgnoreCase("on")){
				if(status == 1){
					config.set("EEW", true);
					save(config,"config.yml");
					id = new EEWListener().runTaskTimer(this, 0, this.config.getInt("tick")).getTaskId();
					status = 0;
					sender.sendMessage(getText("command-eew-on"));
					getLogger().info(getText("command-eew-on"));
				}else{
					sender.sendMessage(getText("command-eew-on-already"));
				}
				return true;
			}else if(args[0].equalsIgnoreCase("off")){
				if(status != 1){
					config.set("EEW", false);
					save(config,"config.yml");
					Bukkit.getScheduler().cancelTask(id);
					status = 1;
					sender.sendMessage(getText("command-eew-off"));
					getLogger().info(getText("command-eew-off"));
				}else{
					sender.sendMessage(getText("command-eew-off-already"));
				}
				return true;
			}else if(args[0].equalsIgnoreCase("status")){
				if(status == 0){
					sender.sendMessage(getText("command-status").replaceAll("%earthquakecount%", String.valueOf(earthquakecount)).replaceAll("%reportcount%",String.valueOf(reportcount)));
				}else if(status == 1){
					sender.sendMessage(getText("command-status").replaceAll("%earthquakecount%", String.valueOf(earthquakecount)).replaceAll("%reportcount%",String.valueOf(reportcount)) + "\n" + getText("command-status-off"));
				}else if(status == 2){
					sender.sendMessage(getText("command-status").replaceAll("%earthquakecount%", String.valueOf(earthquakecount)).replaceAll("%reportcount%",String.valueOf(reportcount)) + "\n" + getText("command-status-error"));
				}
				return true;
			}else if(args[0].equalsIgnoreCase("reloadconfig")){
				loadConfig();
				save(config,"config.yml");
				sender.sendMessage(getText("command-eew-reloadconfig"));
				if(this.config.getBoolean("EEW")){
					if(status == 0){
						Bukkit.getScheduler().cancelTask(id);
					}else{
						getLogger().info(getText("eew-on"));
					}
					id = new EEWListener().runTaskTimer(this, 0, this.config.getInt("tick")).getTaskId();
				}else{
					if(status != 1){
						getLogger().info(getText("eew-off"));
					}
				}
				return true;
			}else if(args[0].equalsIgnoreCase("reloadlang")){
				loadLang();
				save(lang,"lang.yml");
				sender.sendMessage(getText("command-eew-reloadlang"));
				return true;
			}
		}
		return false;
	}
}