package com.kuna.sabuneditor_android;

import java.io.File;

import android.media.AudioManager;
import android.media.SoundPool;
import android.media.SoundPool.OnLoadCompleteListener;
import android.util.Log;

import com.kuna.sabuneditor_android.bms.BMSData;

public class Program {
	public static BMSData bmsdata;
	
	private static SoundPool sp = new SoundPool(1, AudioManager.STREAM_MUSIC, 0);
	public static void PlaySound(String path) {
		if (!new File(path).exists()) {
			path = path.substring(0, path.length()-4) + ".ogg";
		}
		final int sndId = sp.load(path, 1);
		sp.setOnLoadCompleteListener(new OnLoadCompleteListener() {
			@Override
			public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
				sp.play(sndId, 1, 1, 0, 0, 1);
			}
		});
	}
}
