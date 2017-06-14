package tv.ismar.app.core;

import android.os.Build;

import java.net.NetworkInterface;

import tv.ismar.library.util.C;

public class VodUserAgent {

	/**
	 * getMACAddress == getSn
	 * 
	 * @return Sn
	 */
	public static  String getMACAddress(){
		String mac = "001122334455";
			try{
				byte addr[];
				addr=NetworkInterface.getByName("eth0").getHardwareAddress();
				mac="";
				for(int i=0; i<6; i++){
					mac+=String.format("%02X",addr[i]);
				}
			}catch(Exception e){
				return mac;
			}
		return mac;
	}

	/**
	 * getHttpUserAgent
	 * 
	 * @return UserAgent
	 */
	public static String getHttpUserAgent(){
		return Build.MODEL.replaceAll(" ", "_") + "/" + C.versionCode + " " + C.snToken;
	}

	public static String getModelName(){
		if(Build.PRODUCT.length() > 20){
			return Build.PRODUCT.replaceAll(" ", "_").toLowerCase().substring(0,19);
		}else {
			return Build.PRODUCT.replaceAll(" ", "_").toLowerCase();
		}
	}
}
