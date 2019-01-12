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

public class EEWSyncTime extends BukkitRunnable{
	public static Player player = null;

	@Override
	public void run(){
		try {
			if(player == null){
				Bukkit.getLogger().info("[EEW-Plugin] " + this.sync());
			}else{
				this.player.sendMessage(this.sync());
			}
		} catch (Exception e) {
			if(player == null){
				Bukkit.getLogger().info("[EEW-Plugin] " + EEW.getText("time-sync-error"));
			}else{
				this.player.sendMessage(EEW.getText("time-sync-error"));
			}
		}
	}
	public static String sync() throws Exception { 
		CloseableHttpAsyncClient httpclient = HttpAsyncClients.createDefault();
		String msg = "";
		try {
			httpclient.start();
			long systemtime = System.currentTimeMillis();
			HttpGet request = new HttpGet("http://ntp-a1.nict.go.jp/cgi-bin/json");
			Future<HttpResponse> future = httpclient.execute(request , null);
			HttpResponse response = future.get();
			if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK){	
				String responseData = EntityUtils.toString(response.getEntity(),StandardCharsets.UTF_8);
				JSONObject json = new JSONObject(responseData);
				if(json.has("st")){
					long time_ = EEW.time;
					EEW.time = (json.getLong("st") * 1000) - systemtime;
					if(EEW.time > time_){
						msg = EEW.getText("time-fixed-1").replaceAll("%time%",String.valueOf(((double)EEW.time - (double)time_) / 1000));
					}else{
						msg = EEW.getText("time-fixed-2").replaceAll("%time%",String.valueOf(((double)time_ - (double)EEW.time) / 1000));
					}
				}else{
					msg = EEW.getText("time-sync-error");
				}
			}else{
				msg = EEW.getText("time-sync-error");
			}
		} catch (IOException e) {
			throw new Exception(e);
		} finally {
			httpclient.close();
			return msg;
		}
	}
}