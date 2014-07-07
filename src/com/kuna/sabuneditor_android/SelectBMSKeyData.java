package com.kuna.sabuneditor_android;

import java.util.ArrayList;
import java.util.List;

import com.kuna.sabuneditor_android.bms.BMSKeyData;

public class SelectBMSKeyData {
	public static List<BMSKeyData> selectData = new ArrayList<BMSKeyData>(); 
	
	public static void clearAll() {
		selectData.clear();
	}

	public static boolean isSelected() {
		return (selectData.size() > 0);
	}

	public static boolean isSelected(BMSKeyData bkd) {
		return (selectData.contains(bkd));
	}
	
	public static void addBMSData(BMSKeyData bkd) {
		if (bkd == null)
			return;
		if (!selectData.contains(bkd)) {
			selectData.add(bkd);
		}
	}
}
