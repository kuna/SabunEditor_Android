package com.kuna.sabuneditor_android.bms;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.regex.Pattern;

import android.util.Log;

public class BMSUtil {

	public static String GetHash(byte[] data) {
		MessageDigest md;
		String hash = null;
		try {
			md = MessageDigest.getInstance("MD5");
			hash = new BigInteger(1, md.digest( data )).toString(16);
		} catch (NoSuchAlgorithmException e) {
			Log.i("BMSUtil", "Hashing Error!");
			e.printStackTrace();
		}
		return hash;
	}
	
	public static boolean IsInteger(String str) {
		return Pattern.compile("-?[0-9]+").matcher(str).matches();
	}
	
	public static int HexToInt(String hex) {
		String sample = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";
		int r = 0;
		for (int i=0; i<hex.length(); i++) {
			r *= 36;
			for (int j=0; j<sample.length(); j++) {
				if (hex.substring(i, i+1).compareTo( sample.substring(j, j+1) )==0) {
					r += j;
					continue;
				}
			}
		}
		
		return r;
	}
	
	public static String IntToHex(int val) {
		String sample = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";
		return new Character(sample.charAt((int)(val/36))).toString() + new Character(sample.charAt(val%36)).toString();
	}
}
