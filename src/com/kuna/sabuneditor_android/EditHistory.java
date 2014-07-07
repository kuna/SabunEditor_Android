package com.kuna.sabuneditor_android;

import java.util.ArrayList;
import java.util.List;

import android.util.Log;

import com.kuna.sabuneditor_android.bms.BMSData;
import com.kuna.sabuneditor_android.bms.BMSKeyData;
import com.kuna.sabuneditor_android.bms.BMSUtil;

public class EditHistory {
	private static List<HistoryObject> historyArr;
	private static int historyIndex = 0;
	private static final int MAX_HISTORY_SIZE = 15;
	public static final int ACTION_ADD = 0;
	public static final int ACTION_DELETE = 1;
	
	private static class HistoryObject {
		public List<BMSKeyData> objs; 
		public int action;
	}
	
	public static void initHistory() {
		historyArr = new ArrayList<HistoryObject>();
	}
	
	public static void AddHistory(BMSKeyData bkd, int action) {
		List<BMSKeyData> objs = new ArrayList<BMSKeyData>();
		objs.add(bkd);
		
		AddHistory(objs, action);
	}
	
	public static void AddHistory(List<BMSKeyData> bkds, int action) {
		while (historyIndex < historyArr.size()) {
			historyArr.remove(historyIndex);
		}
		
		if (historyArr.size() > MAX_HISTORY_SIZE) {
			historyArr.remove(0);
			historyIndex--;
			if (historyIndex < 0)
				historyIndex = 0;
		}
		
		// must clone items
		HistoryObject obj = new HistoryObject();
		obj.objs = BMSUtil.cloneKeyArray((ArrayList<BMSKeyData>) bkds);
		obj.action = action;
		historyArr.add(obj);
		historyIndex++;
	}
	
	public static boolean doUndo(BMSData bd) {
		if (historyIndex <= 0)
			return false;
		
		historyIndex--;
		HistoryObject obj = historyArr.get(historyIndex);
		for (BMSKeyData bkd: obj.objs) {
			if (obj.action == EditHistory.ACTION_ADD) {
				BMSKeyData _bkd = bd.getNote((int)bkd.getBeat(), bkd.getNumerator(), bkd.getChannel(), bkd.getLayerNum());
				if (_bkd != null) {
					bd.removeNote(_bkd);
				}
			} else if (obj.action == EditHistory.ACTION_DELETE) {
				bd.addNote(bkd);
			}
		}
		
		return true;
	}
	
	public static boolean doRedo(BMSData bd) {
		if (historyIndex >= historyArr.size())
			return false;
		
		HistoryObject obj = historyArr.get(historyIndex);
		for (BMSKeyData bkd: obj.objs) {
			if (obj.action == EditHistory.ACTION_ADD) {
				bd.addNote(bkd);
			} else if (obj.action == EditHistory.ACTION_DELETE) {
				BMSKeyData _bkd = bd.getNote((int)bkd.getBeat(), bkd.getNumerator(), bkd.getChannel(), bkd.getLayerNum());
				if (_bkd != null)
					bd.removeNote(_bkd);
			}
		}
		historyIndex++;
		
		return true;
	}
}
