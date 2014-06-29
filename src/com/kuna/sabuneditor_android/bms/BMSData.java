package com.kuna.sabuneditor_android.bms;

import java.util.ArrayList;
import java.util.List;

public class BMSData {
	public int player;
	public String title;
	public String subtitle;
	public String genre;
	public String artist;
	public int BPM;
	public int playlevel;
	public int difficulty;
	public int rank;
	public int total;
	public int volwav;
	public String stagefile;
	public String[] str_wav = new String[1322];
	public String[] str_bg = new String[1322];
	public double[] str_bpm = new double[1322];
	public double[] str_stop = new double[1322];
	public boolean[] LNObj = new boolean[1322];
	public double[] length_beat = new double[1024];		// MAXIMUM_BEAT
	public List<BMSKeyData> bmsdata = new ArrayList<BMSKeyData>();	// MAXIMUM_OBJECT (Trans object+hit object+STOP+BPM)
	public List<BMSKeyData> bgadata = new ArrayList<BMSKeyData>();	// BGA
	public List<BMSKeyData> bgmdata = new ArrayList<BMSKeyData>();	// BGM
	public int notecnt;
	public double time;
	public int keycount;
	
	// bms file specific data
	public String hash;
	public String path;
	public String dir;
	
	// We dont store LNType
	// we always save LNTYPE 1 (with LNOBJ)
	

	public double getBeatFromTime(int millisec) {
		double bpm = BPM;
		double beat = 0;
		
		// for more precision set vals as Double
		double time = 0;
		double newtime = 0;
		
		for (int i=0; i<bmsdata.size(); i++) {
			BMSKeyData d = bmsdata.get(i);
			
			// Beat is effected by midi length ... check midi length
			while (d.beat > (int)beat+1) {
				newtime = time + ((int)beat+1-beat) * (1.0f/bpm*60*4) * 1000 * length_beat[(int)beat];	// millisec
				if (newtime >= millisec) {
					return beat + (millisec-time)*(bpm/60000/4.0f)/length_beat[(int)beat];
				}
				
				time = newtime;
				beat = (int)beat+1;
			}
			
			if (d.key == 9) {	// STOP
				time += d.value * 1000;
				if (time >= millisec)
					return beat;
				continue;
			}
			
			if (d.key == 3 || d.key == 8) {	// BPM
				newtime = time + (d.beat-beat) * (1.0f/bpm*60*4) * 1000 * length_beat[(int)beat];	// millisec
				if (newtime >= millisec) {
					return beat + (millisec-time)*(bpm/60000/4.0f)/length_beat[(int)beat];
				}
				
				beat = d.beat;
				bpm = d.value;
				time = newtime;
			}
		}
		
		// get beat from last beat
		beat += (millisec-time)*((double)bpm/60000/4.0f);
		
		// cannot be larger then last beat
		//double maxbeat = bmsdata.get(bmsdata.size()-1).beat;
		//if (beat > maxbeat)
		//	beat = maxbeat;
		
		return beat;
	}
	
	public double getTimeFromBeat(double beat) {
		double _bpm = BPM;		// BPM for parsing
		double _time = 0;		// time for parsing
		double _beat = 0;		// beat for parsing
		
		for (int i=0; i<bmsdata.size(); i++) {
			BMSKeyData d = bmsdata.get(i);
			
			// check midi length
			while (d.beat >= (int)_beat+1) {
				if ((int)_beat+1 > beat && beat > _beat) {
					return _time + ((int)_beat+1-beat) * (1.0f/_bpm*60*4) * length_beat[(int)_beat];
				}
				_time += ((int)_beat+1-_beat) * (1.0f/_bpm*60*4) * length_beat[(int)_beat];
				_beat = (int)_beat+1;
			}
			
			if (d.beat > beat && beat > _beat) {
				return _time + (beat - _beat) * (1.0f / _bpm * 60 * 4) * length_beat[(int)_beat];
			}
			_time += (d.beat - _beat) * (1.0f / _bpm * 60 * 4) * length_beat[(int)_beat];
			
			if (d.key == 3 || d.key == 8 )	// BPM
				_bpm = d.value;
			if (d.key == 9)
				_time += d.value;
			
			_beat = d.beat;
		}
		
		// time is over
		return _time;
	}
	
	public double getBPMFromBeat(double beat) {
		double bpm = BPM;
		for (int i=0; i<bmsdata.size(); i++) {
			if (bmsdata.get(i).beat > beat)
				break;
			if (bmsdata.get(i).key == 3 || bmsdata.get(i).key == 8)
				bpm = bmsdata.get(i).value;
		}
		return bpm;
	}
	
	public double getBPM(int val) {
		return str_bpm[val];
	}
	public double getSTOP(int val) {
		return str_stop[val];
	}
	public String getBGA(int val) {
		return str_bg[val];
	}
	public String getWAV(int val) {
		return str_wav[val];
	}
}
