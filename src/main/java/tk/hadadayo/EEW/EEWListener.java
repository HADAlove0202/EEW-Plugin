package tk.hadadayo.EEW;

import tk.hadadayo.EEW.EEW;
import tk.hadadayo.EEW.EEWAlarm;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginLogger;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.entity.Player;
import org.bukkit.Sound;

import java.util.HashMap;
import java.util.ArrayList;
import org.json.JSONObject;

import java.util.concurrent.Future;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import org.apache.http.HttpStatus;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.apache.http.util.EntityUtils;

public class EEWListener extends BukkitRunnable{
	private static String earthquake = "";
	private static String calcintensity = "";
	private static String region_name = "";
	private static String report_time = "";
	private static boolean error = false;
	private static boolean alert = false;

	@Override
	public void run(){
		try {
			this.listen();
		} catch (Exception e) {
			if(error == false){
				Bukkit.getLogger().warning(EEW.getText("eew-error"));
				EEW.status = 2;
			}
			error = true;
		}
	}
	public static void listen() throws Exception { 
		Charset charset = StandardCharsets.UTF_8;
		CloseableHttpAsyncClient httpclient = HttpAsyncClients.createDefault();
		try {
			SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
			format.setTimeZone(TimeZone.getTimeZone("Asia/Tokyo"));
			Date date = new Date();
			String t = format.format(date);

			httpclient.start();
			HttpGet request = new HttpGet("http://www.kmoni.bosai.go.jp/new/webservice/hypo/eew/" + t + ".json");
			Future<HttpResponse> future = httpclient.execute(request , null);
			HttpResponse response = future.get();
			if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK){				
				String responseData = EntityUtils.toString(response.getEntity(),charset);				
				JSONObject json = new JSONObject(responseData);
				if(json.has("alertflg") && !json.getString("report_time").equals(report_time) && !json.getBoolean("is_training")){
					Boolean isalert;
					if(json.getString("alertflg").equals("\u8B66\u5831")){
						isalert = true;
					}else{
						isalert = false;
					}
					String region = json.getString("region_name");
					String intensity = json.getString("calcintensity");
					String magunitude = json.getString("magunitude");
					String depth = json.getString("depth");
					String latitude = json.getString("latitude");
					String longitude = json.getString("longitude");
					String report_num = "\u7B2C" + json.getString("report_num") + "\u5831";
					Boolean is_final = json.getBoolean("is_final");
					SimpleDateFormat origin_time1 = new SimpleDateFormat("yyyyMMddHHmmss");
					origin_time1.setTimeZone(TimeZone.getTimeZone("Asia/Tokyo"));
					Date origin_time2 = origin_time1.parse(json.getString("origin_time"));
					String origin_time = new SimpleDateFormat(EEW.config.getString("origin_time_format")).format(origin_time2);
					
					if(is_final){
						report_num = "\u6700\u7D42\u5831";
					}
					if(!json.getString("report_id").equals(earthquake)){
						EEW.earthquakecount++;
					}
					EEW.reportcount++;
					if(json.getBoolean("is_cancel")){
						if(isalert){
							if(EEW.config.getInt("alert.broadcast.mode") <= 3 || (EEW.config.getInt("alert.console.mode") <= 3 && (EEW.config.getInt("alert.message.mode") <= 3 ||EEW.config.getInt("alert.title.mode") <= 3 ))){
								Bukkit.broadcastMessage(EEW.getText("cancelled"));
							}else if(EEW.config.getInt("alert.console.mode") <= 3){
								Bukkit.getLogger().info(EEW.getText("cancelled"));
							}else if(EEW.config.getInt("alert.message.mode") <= 3 || EEW.config.getInt("alert.title.mode") <= 3){
								for(Player player : Bukkit.getServer().getOnlinePlayers()){
									player.sendMessage(EEW.getText("cancelled"));
								}
							}
						}else{
							if(EEW.config.getInt("forecast.broadcast.mode") <= 3 || (EEW.config.getInt("forecast.console.mode") <= 3 && (EEW.config.getInt("forecast.message.mode") <= 3 ||EEW.config.getInt("forecast.title.mode") <= 3 ))){
								Bukkit.broadcastMessage(EEW.getText("cancelled"));
							}else if(EEW.config.getInt("forecast.console.mode") <= 3){
								Bukkit.getLogger().info(EEW.getText("cancelled"));
							}else if(EEW.config.getInt("forecast.message.mode") <= 3 ||EEW.config.getInt("forecast.title.mode") <= 3){
								for(Player player : Bukkit.getServer().getOnlinePlayers()){
									player.sendMessage(EEW.getText("cancelled"));
								}
							}
						}
						for(Player player : Bukkit.getServer().getOnlinePlayers()){
							player.resetTitle();
						}
					}else if(!json.getString("report_id").equals(earthquake) || !isalert.equals(alert)){
						if(isalert){
							sendAlert(3,is_final, region, intensity, magunitude, depth, latitude, longitude, origin_time, report_num);
						}else{
							sendForecast(3, is_final, region, intensity, magunitude, depth, latitude, longitude, origin_time, report_num);
						}
					}else if(!calcintensity.equals(intensity)){
						if(alert){
							sendAlert(2, is_final, region, intensity, magunitude, depth, latitude, longitude, origin_time, report_num);
						}else{
							sendForecast(2, is_final, region, intensity, magunitude, depth, latitude, longitude, origin_time, report_num);
						}
					}else if(!region_name.equals(region)){
						if(alert){
							sendAlert(1, is_final, region, intensity, magunitude, depth, latitude, longitude, origin_time, report_num);
						}else{
							sendForecast(1, is_final, region, intensity, magunitude, depth, latitude, longitude, origin_time, report_num);
						}
					}else{
						if(alert){
							sendAlert(0, is_final, region, intensity, magunitude, depth, latitude, longitude, origin_time, report_num);
						}else{
							sendForecast(0, is_final, region, intensity, magunitude, depth, latitude, longitude, origin_time, report_num);
						}
					}
					alert = isalert; 
					report_time = json.getString("report_time");
					earthquake = json.getString("report_id");
					region_name = region;
					calcintensity = intensity;
				}
				if(error == true){
					EEW.status = 0;
					Bukkit.getLogger().info(EEW.getText("eew-restart"));
				}
				error = false;
			}else{
				if(error == false){
					Bukkit.getLogger().warning(EEW.getText("eew-error"));
					EEW.status = 2;
				}
				error = true;
			}
		} catch (IOException e) {
			throw new Exception(e);
		} finally {
			httpclient.close();
		}
	}
	private static void sendAlert(int mode, boolean is_final, String region, String intensity, String magunitude, String depth, String latitude, String longitude, String origin_time, String report_num){
		if((mode != 4 && EEW.config.getInt("alert.console.mode") <= mode) || (EEW.config.getBoolean("alert.message.final") && is_final)){
			Bukkit.getLogger().info(EEW.getText("alert-console").replaceAll("%region%", region).replaceAll("%intensity%", intensity).replaceAll("%magunitude%", magunitude).replaceAll("%depth%", depth).replaceAll("%latitude%", latitude).replaceAll("%longitude%", longitude).replaceAll("%origin_time%", origin_time).replaceAll("%report_num%", report_num));
		}
		if((mode != 4 && EEW.config.getInt("alert.broadcast.mode") <= mode) || (EEW.config.getBoolean("alert.broadcast.final") && is_final)){
			Bukkit.broadcastMessage(EEW.getText("alert-broadcast").replaceAll("%region%", region).replaceAll("%intensity%", intensity).replaceAll("%magunitude%", magunitude).replaceAll("%depth%", depth).replaceAll("%latitude%", latitude).replaceAll("%longitude%", longitude).replaceAll("%origin_time%", origin_time).replaceAll("%report_num%", report_num));
		}
		for(Player player : Bukkit.getServer().getOnlinePlayers()){
			if((mode != 4 && EEW.config.getInt("alert.message.mode") <= mode) || (EEW.config.getBoolean("alert.message.final") && is_final)){
				player.sendMessage(EEW.getText("alert-message").replaceAll("%region%", region).replaceAll("%intensity%", intensity).replaceAll("%magunitude%", magunitude).replaceAll("%depth%", depth).replaceAll("%latitude%", latitude).replaceAll("%longitude%", longitude).replaceAll("%origin_time%", origin_time).replaceAll("%report_num%", report_num));
			}
			if((mode != 4 && EEW.config.getInt("alert.title.mode") <= mode) || (EEW.config.getBoolean("alert.title.final") && is_final)){
				player.sendTitle(EEW.getText("alert-title").replaceAll("%region%", region).replaceAll("%intensity%", intensity).replaceAll("%magunitude%", magunitude).replaceAll("%depth%", depth).replaceAll("%latitude%", latitude).replaceAll("%longitude%", longitude).replaceAll("%origin_time%", origin_time).replaceAll("%report_num%", report_num), EEW.getText("alert-subtitle").replaceAll("%region%", region).replaceAll("%intensity%", intensity).replaceAll("%magunitude%", magunitude).replaceAll("%depth%", depth).replaceAll("%latitude%", latitude).replaceAll("%longitude%", longitude).replaceAll("%report_num%", report_num));
			}
		}

		if(EEW.config.getInt("alert.alarm.mode") <= mode || (EEW.config.getBoolean("alert.alarm.final") && is_final)){
			for(Player player : Bukkit.getServer().getOnlinePlayers()){
				if (Bukkit.getVersion().contains("1.8")){
					player.playSound(player.getLocation(), "note.pling", 1000f , 1f);
				}else{
					player.playSound(player.getLocation(), Sound.valueOf("BLOCK_NOTE_PLING"), 1000f, 1f);
				}
			}
			EEWAlarm.on = true;
		}
	}
	private static void sendForecast(int mode, boolean is_final, String region, String intensity, String magunitude, String depth, String latitude, String longitude, String origin_time, String report_num){
		if((mode != 4 && EEW.config.getInt("forecast.console.mode") <= mode) || (EEW.config.getBoolean("forecast.console.final") && is_final)){
			Bukkit.getLogger().info(EEW.getText("forecast-console").replaceAll("%region%", region).replaceAll("%intensity%", intensity).replaceAll("%magunitude%", magunitude).replaceAll("%depth%", depth).replaceAll("%latitude%", latitude).replaceAll("%longitude%", longitude).replaceAll("%origin_time%", origin_time).replaceAll("%report_num%", report_num));
		}
		if((mode != 4 && EEW.config.getInt("forecast.broadcast.mode") <= mode) || (EEW.config.getBoolean("forecast.broadcast.final") && is_final)){
			Bukkit.broadcastMessage(EEW.getText("forecast-broadcast").replaceAll("%region%", region).replaceAll("%intensity%", intensity).replaceAll("%magunitude%", magunitude).replaceAll("%depth%", depth).replaceAll("%latitude%", latitude).replaceAll("%longitude%", longitude).replaceAll("%origin_time%", origin_time).replaceAll("%report_num%", report_num));
		}
		for(Player player : Bukkit.getServer().getOnlinePlayers()){
			if((mode != 4 && EEW.config.getInt("forecast.message.mode") <= mode) || (EEW.config.getBoolean("forecast.message.final") && is_final)){
				player.sendMessage(EEW.getText("forecast-message").replaceAll("%region%", region).replaceAll("%intensity%", intensity).replaceAll("%magunitude%", magunitude).replaceAll("%depth%", depth).replaceAll("%latitude%", latitude).replaceAll("%longitude%", longitude).replaceAll("%origin_time%", origin_time).replaceAll("%report_num%", report_num));
			}
			if((mode != 4 && EEW.config.getInt("forecast.title.mode") <= mode) || (EEW.config.getBoolean("forecast.title.final") && is_final)){
				player.sendTitle(EEW.getText("forecast-title").replaceAll("%region%", region).replaceAll("%intensity%", intensity).replaceAll("%magunitude%", magunitude).replaceAll("%depth%", depth).replaceAll("%latitude%", latitude).replaceAll("%longitude%", longitude).replaceAll("%origin_time%", origin_time).replaceAll("%report_num%", report_num), EEW.getText("forecast-subtitle").replaceAll("%region%", region).replaceAll("%intensity%", intensity).replaceAll("%magunitude%", magunitude).replaceAll("%depth%", depth).replaceAll("%latitude%", latitude).replaceAll("%longitude%", longitude).replaceAll("%report_num%", report_num));
			}
		}
	}
}