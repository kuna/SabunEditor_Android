package com.kuna.sabuneditor_android.bms;

import java.util.Comparator;

public class BMSKeyData implements Comparable<BMSKeyData> {
	double beat;
	double ebeat;	// for LN
	
	int key;
	double value;
	double evalue;
	double time;
	double etime;
	int attr;
	
	@Override
	public int compareTo(BMSKeyData o) {
		if (this.beat < o.beat)
			return -1;
		else if (this.beat > o.beat)
			return 1;
		else
			return 0;
	}

	public static Comparator<BMSKeyData> KeyComparator 
                          = new Comparator<BMSKeyData>() {
	    public int compare(BMSKeyData d1, BMSKeyData d2) {
    		return d1.key - d2.key;
 
	      //ascending order
	      //return fruitName1.compareTo(fruitName2);
 
	      //descending order
	      //return fruitName2.compareTo(fruitName1);
	    }
 
	};
}
