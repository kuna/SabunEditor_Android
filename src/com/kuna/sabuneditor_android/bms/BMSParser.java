package com.kuna.sabuneditor_android.bms;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import android.util.Log;

public class BMSParser {
	public static int BMS_LOCALE_NONE = 0;
	public static int BMS_LOCALE_JP = 1;
	public static int BMS_LOCALE_KR = 2;

	private static int LNType;
	private static int[] LNprevVal = new int[1322];
	private static int BMS_PARSER_HEADER = 1;
	private static int BMS_PARSER_MAINDATA = 2;
	private static int BMS_PARSER_BGA = 3;

	private static int[] BMSKeyIndex = new int[]{1, 2, 3, 4, 5, 8, 9, 6};
	private static int[] BMSKeyCount = new int[14];
	private static int BMSParseMode;
	private static int randomStackCnt;
	private static int[] randomVal = new int[256];		// Maximum stack: 256
	private static int[] condition = new int[256];		// 0: read line, 1: ignore line, 2: executing command, 3: command already executed
	
	public static boolean LoadBMSFile(String path, BMSData bd) {
		Log.i("BMSParser", "Loading BMS File ...");
		File f = new File(path);
		int locale;
		
		bd.path = path;
		bd.dir = path.substring(0, path.length() - f.getName().length());
		
		//Read text from file
		long Filesize = f.length();
	    byte[] bytes = new byte[(int) Filesize];
	    try {
	        BufferedInputStream buf = new BufferedInputStream(new FileInputStream(new File(path)));
	        buf.read(bytes, 0, bytes.length);
	        buf.close();
	    } catch (FileNotFoundException e) {
	        // TODO Auto-generated catch block
	    	Log.i("BMSParser", "File not found");
	    	return false;
	    } catch (IOException e) {
	        // TODO Auto-generated catch block
	        e.printStackTrace();
	        return false;
	    }
		
		// check locale
	    String data;
	    try {
			locale = BMS_LOCALE_JP;
			data = new String(bytes, "SHIFT_JIS");
			byte[] b = data.getBytes();
			for (int i=0; i< ((data.length()>1000)?1000:data.length()) ; i++) {
				if (b[i] >= 44032 && b[i] <= 55203) {
					locale = BMS_LOCALE_KR;
				}
			}
			if (locale == BMS_LOCALE_KR) {
				data = new String(bytes, "CP949");
			}
	    } catch (UnsupportedEncodingException e) {
	    	Log.i("BMSParser", "Unsupported Encoding Exception");
	    	return false;
	    }
		
		bd.hash = BMSUtil.GetHash(bytes);
		
		// init before parshing
		for (int i=0; i<14; i++) {
			BMSKeyCount[i] = 0;
		}
		
		return ParseBMSData(data, bd);
	}
	
	public static boolean ParseBMSData(String data, BMSData bd) {
		// init
		for (int i=0; i<bd.length_beat.length; i++)
			bd.length_beat[i] = 1;
		
		bd.notecnt = 0;
		bd.total = 0;
		bd.rank = 3;	// EASY is default;
		bd.title = "";
		bd.subtitle = "";
		bd.genre = "";
		bd.artist = "";
		bd.stagefile = "";
		LNType = 1;	// 1 is default
		for (int i=0; i<1322; i++)
			bd.LNObj[i] = false;
		bd.bmsdata.clear();
		bd.bgmdata.clear();
		bd.bgadata.clear();
		
		String[] lines = data.split("\r\n");
		
		for (int i=0; i<lines.length; i++) {
			ProcessBMSLine(lines[i].trim(), bd);
		}
		
		// sort data
		Collections.sort(bd.bmsdata);
		Collections.sort(bd.bgadata);
		Collections.sort(bd.bgmdata);
		
		// when difficulty is not setted,
		// process automatic difficulty set
		if (bd.difficulty == 0) {
			// basically it is 5
			bd.difficulty = 5;
			
			String _title = bd.title.toUpperCase();
			String _path = bd.path.toUpperCase();
			if (_title.indexOf("BEGINNER")>0 || _path.indexOf("BEGINNER")>0 ||
					_title.indexOf("LIGHT")>0 || _path.indexOf("LIGHT")>0 ||
					_title.indexOf("EASY")>0 || _path.indexOf("EASY")>0) {
				bd.difficulty = 1;
			}
			if (_title.indexOf("NORMAL")>0 || _path.indexOf("NORMAL")>0 ||
					_title.indexOf("STANDARD")>0 || _path.indexOf("STANDARD")>0) {
				bd.difficulty = 2;
			}
			if (_title.indexOf("HARD")>0 || _path.indexOf("HARD")>0 ||
					_title.indexOf("HYPER")>0 || _path.indexOf("HYPER")>0) {
				bd.difficulty = 3;
			}
			if (_title.indexOf("ANOTHER")>0 || _path.indexOf("ANOTHER")>0 ||
					_title.indexOf("EX")>0 || _path.indexOf("EX")>0) {
				bd.difficulty = 4;
			}
			if (_title.indexOf("BLACK")>0 || _path.indexOf("BLACK")>0 ||
					_title.indexOf("KUSO")>0 || _path.indexOf("KUSO")>0 ||
					_title.indexOf("INSANE")>0 || _path.indexOf("INSANE")>0) {
				bd.difficulty = 5;
			}
		}
		
		// total : 160+(note)*0.16
		if (bd.total == 0) {
			bd.total = (int) (bd.notecnt*0.16f + 160);
		}
		
		return true;
	}
	
	private static void ProcessBMSLine(String line, BMSData bd) {
		// preprocessor
		if (line.toUpperCase().startsWith("#RANDOM") || line.toUpperCase().startsWith("#SETRANDOM")) {
			String args[] = line.split(" ");
			int val = Integer.parseInt(args[1]);
			randomVal[randomStackCnt++] = (int)(Math.random()*val);
			return;
		} else if (line.toUpperCase().startsWith("#IF")) {
			String args[] = line.split(" ");
			int val = Integer.parseInt(args[1]);
			if (val == randomVal[randomStackCnt-1])
				condition[randomStackCnt-1] = 2;
			else
				condition[randomStackCnt-1] = 0;
			return;
		} else if (line.toUpperCase().startsWith("#ELSEIF")) {
			if (condition[randomStackCnt-1] == 2) {
				condition[randomStackCnt-1] = 3;
				return;
			}
			
			String args[] = line.split(" ");
			int val = Integer.parseInt(args[1]);
			if (val == randomVal[randomStackCnt-1])
				condition[randomStackCnt-1] = 2;
			else
				condition[randomStackCnt-1] = 0;
			return;
		} else if (line.compareToIgnoreCase("#ENDIF") == 0) {
			condition[--randomStackCnt] = 0;
			return;
		}
		if (randomStackCnt > 0) {
			if (condition[randomStackCnt-1] == 1 || condition[randomStackCnt-1] == 3)
				return;
		}
		// preprocessor end
		
		if (line.compareTo("*---------------------- HEADER FIELD") == 0) {
			BMSParseMode = BMS_PARSER_HEADER;
			return;
		}
		if (line.compareTo("*---------------------- MAIN DATA FIELD") == 0) {
			BMSParseMode = BMS_PARSER_MAINDATA;
			return;
		}
		if (line.compareTo("*---------------------- BGA FIELD") == 0) {
			BMSParseMode = BMS_PARSER_BGA;
			return;
		}
		
		if (BMSParseMode == BMS_PARSER_HEADER || BMSParseMode == BMS_PARSER_BGA) {
			String[] args = line.split(" ", 2);
			if (args.length > 1) {
				if (args[0].compareToIgnoreCase("#TITLE") == 0) {
					bd.title = args[1];
				} else
				if (args[0].compareToIgnoreCase("#SUBTITLE") == 0) {
					bd.subtitle = args[1];
				} else
				if (args[0].compareToIgnoreCase("#PLAYER") == 0) {
					bd.player = Integer.parseInt(args[1]);
				} else
				if (args[0].compareToIgnoreCase("#GENRE") == 0) {
					bd.genre = args[1];
				} else
				if (args[0].compareToIgnoreCase("#ARTIST") == 0) {
					bd.artist = args[1];
				} else
				if (args[0].compareToIgnoreCase("#BPM") == 0) {
					bd.BPM = (int) Double.parseDouble(args[1]);
				} else
				if (args[0].compareToIgnoreCase("#DIFFICULTY") == 0) {
					bd.difficulty = Integer.parseInt(args[1]);
				} else
				if (args[0].compareToIgnoreCase("#PLAYLEVEL") == 0) {
					bd.playlevel = Integer.parseInt(args[1]);
				} else
				if (args[0].compareToIgnoreCase("#RANK") == 0) {
					bd.rank = Integer.parseInt(args[1]);
				} else
				if (args[0].compareToIgnoreCase("#TOTAL") == 0) {
					bd.total = (int) Double.parseDouble(args[1]);
				} else
				if (args[0].compareToIgnoreCase("#VOLWAV") == 0) {
					bd.volwav = (int) Double.parseDouble(args[1]);
				} else
				if (args[0].compareToIgnoreCase("#STAGEFILE") == 0) {
					bd.stagefile = args[1];
				} else
				if (args[0].compareToIgnoreCase("#LNTYPE") == 0) {
					LNType = Integer.parseInt(args[1]);
				} else
				if (args[0].toUpperCase().startsWith("#STP")) {
					String[] pt = args[0].substring(4).split("[.]");
					
					BMSKeyData nData = new BMSKeyData();
					nData.value = Double.parseDouble(args[1]);
					nData.key = 9;		// STOP Channel
					nData.beat = Integer.parseInt(pt[0]) + (double)Integer.parseInt(pt[1])/1000;
					bd.bmsdata.add(nData);
				} else
				if (args[0].toUpperCase().startsWith("#LNOBJ")) {
					bd.LNObj[BMSUtil.HexToInt(args[1])] = true;
				} else
				if (args[0].toUpperCase().startsWith("#BMP")) {
					int index = BMSUtil.HexToInt(args[0].substring(4, 6));
					bd.str_bg[index] = args[1];
				} else
				if (args[0].toUpperCase().startsWith("#WAV")) {
					int index = BMSUtil.HexToInt(args[0].substring(4, 6));
					bd.str_wav[index] = args[1];
				} else
				if (args[0].toUpperCase().startsWith("#BPM")) {
					int index = BMSUtil.HexToInt(args[0].substring(4, 6));
					bd.str_bpm[index] = Double.parseDouble(args[1]);
				} else
				if (args[0].toUpperCase().startsWith("#STOP")) {
					int index = BMSUtil.HexToInt(args[0].substring(4, 6));
					bd.str_stop[index] = Double.parseDouble(args[1]);
				}
			}
		}
		
		if (BMSParseMode == BMS_PARSER_MAINDATA || BMSParseMode == BMS_PARSER_BGA) {
			String[] args = line.split(":", 2);
			if (args.length > 1) {
				if (!BMSUtil.IsInteger(args[0].substring(1, 6))) return;
				int beat = Integer.parseInt(args[0].substring(1, 4));
				int channel = Integer.parseInt(args[0].substring(4, 6));
				
				if (channel == 2) {
					bd.length_beat[beat] = Double.parseDouble(args[1]);
				} else {
					int ncb = args[1].length();
					for (int i=0; i<ncb/2; i++) {
						String val_str = args[1].substring(i*2, i*2+2);
						int val = BMSUtil.HexToInt(val_str);
						if (val == 0) {
							// ignore data 00
							LNprevVal[channel] = 0;
							continue;		
						}

						if (channel > 10 && channel < 20) bd.notecnt++;			// 1 Player's notecnt
						if (channel > 20 && channel < 30) bd.notecnt++;			// 2 Player's notecnt
						double nb = beat + (double)i/(double)ncb*2;
	
						BMSKeyData nData = new BMSKeyData();
						nData.value = val;
						nData.key = channel;
						nData.beat = nb;
						
						switch (channel) {
						case 1:		// BGM
							bd.bgmdata.add(nData);
							break;
						case 8:		// Extended BPM
							nData.value = bd.getBPM(val);
							bd.bmsdata.add(nData);
							break;
						case 9:		// STOP
							nData.value = bd.getSTOP(val);
							bd.bmsdata.add(nData);
							break;
						case 3:		// BPM
							nData.value = Integer.parseInt(val_str, 16);
							bd.bmsdata.add(nData);
							break;
						case 7:
						case 6:
						case 4:		// BGA
							bd.bgadata.add(nData);
							break;
						case 11:	// 1 Player data
						case 12:
						case 13:
						case 14:
						case 15:
						case 16:
						case 18:
						case 19:
						case 21:	// 2 Player data
						case 22:
						case 23:
						case 24:
						case 25:
						case 26:
						case 28:
						case 29:
							// check LNOBJ command
							if (bd.LNObj[val]) {
								// if LNObj is true,
								// find previous LN obj(unused) and set etime.
								// if no previous LN obj found, then insert new one.
								boolean foundObj = false;
								for (int _i=bd.bmsdata.size()-1; _i>=0 ;_i--)
								{
									BMSKeyData oldData = bd.bmsdata.get(_i);
									if (nData.key == oldData.key && oldData.ebeat == 0) {
										oldData.ebeat = nData.ebeat;
										oldData.evalue = nData.value;
										foundObj = true;
										break;
									}
								}
								
								if (!foundObj) {
									bd.bmsdata.add(nData);
								} else {
									bd.notecnt--;	// LN needs 2 key data, so 1 discount to correct note number.
								}
							} else {
								bd.bmsdata.add(nData);
							}
							break;
						case 31:
						case 32:
						case 33:
						case 34:
						case 35:
						case 36:
						case 38:
						case 39:
						case 41:
						case 42:
						case 43:
						case 44:
						case 45:
						case 46:
						case 48:
						case 49:
							// auto key sound(bg)
							bd.bmsdata.add(nData);
							break;
						case 51:
						case 52:
						case 53:
						case 54:
						case 55:
						case 56:
						case 58:
						case 59:
						case 61:
						case 62:
						case 63:
						case 64:
						case 65:
						case 66:
						case 68:
						case 69:
							// long note (LNTYPE)
							// find previous LN obj and set etime.
							// if no previous LN obj found, then insert new one.
							boolean foundObj = false;
							for (int _i=bd.bmsdata.size()-1; _i>=0 ;_i--)
							{
								if (LNType == 2 && nData.key != LNprevVal[channel])
									break;	// LNTYPE 2: create new keydata when not continuous
								
								BMSKeyData oldData = bd.bmsdata.get(_i);
								if (nData.key == oldData.key) {
									if (LNType == 1 && oldData.ebeat == 0) {
										// LNTYPE 1: only uses clean one
										oldData.ebeat = nData.ebeat;
										oldData.evalue = nData.value;
										foundObj = true;
										break;
									} else if (LNType == 2) {
										// LNTYPE 2: able to use dirty one when continuous.
										oldData.ebeat = nData.ebeat;
										oldData.evalue = nData.value;
										foundObj = true;
										break;
									}
								}
							}
							
							if (!foundObj) {
								bd.bmsdata.add(nData);
							} else {
								bd.notecnt--;	// LN needs 2 key data, so 1 discount to correct note number.
							}
							break;
						}
						
						// save prev val for LNTYPE 2
						if (channel > 50 && channel< 70) {
							LNprevVal[channel] = val;
						} else {
							LNprevVal[channel] = 0;
						}
					}
				}
			}
		}
	}
	
	// MUST USE AFTER PARSING & SORTING!
	private void setTimemark(BMSData bd) {
		double _bpm = bd.BPM;		// BPM for parsing
		double _time = 0;		// time for parsing
		double _beat = 0;		// beat for parsing
		
		for (int i=0; i<bd.bmsdata.size(); i++) {
			BMSKeyData d = bd.bmsdata.get(i);
			
			// check midi length
			while (d.beat >= (int)_beat+1) {
				_time += ((int)_beat+1-_beat) * (1.0f/_bpm*60*4) * bd.length_beat[(int)_beat];
				_beat = (int)_beat+1;
			}
			
			_time += (d.beat - _beat) * (1.0f / _bpm * 60 * 4) * bd.length_beat[(int)_beat];
			d.time = _time*1000;	// millisecond
			
			if (d.key == 3 || d.key == 8 )	// BPM
				_bpm = d.value;
			if (d.key == 9)
				_time += d.value;
			
			_beat = d.beat;
		}
		
		bd.time = _time;
		
		
		// LN note time
	}
	
	public static boolean SaveBMSFile(String path, BMSData bd) {
		String data;
		
		// add metadata
		data = "";
		
		// add keydata (need to sort first by beat -> key)
		boolean channels[] = new boolean[1322];
		int be=1, is=0, ie=0;
		for (int i=0; i<bd.bmsdata.size(); i++) {
			BMSKeyData bkey = bd.bmsdata.get(i);
			if (bkey.beat > be) {
				ie = i-1;
				data += GetBeatString(bd, is, ie, be-1);
				is = ie+1;
				be++;
			}
		}
		
		return true;
	}

	private static String GetBeatString(BMSData bd, int is, int ie, int beat) {
		String[] ret = new String[1322];
		
		// get Beat string from BMSData
		for (int i=is; i<=ie; i++) {
			BMSKeyData bkey = bd.bmsdata.get(i);
			int numerator = GetBeatFraction(bkey.beat)[0];
			int channel = bkey.key;
			if (ret[channel] == null)
				ret[channel] = "";
			
			if (numerator == 0) continue;
			
			for (int j=0; j<ret[channel].length()/2-numerator; j++) {
				ret[channel] += "00";
			}
			
			ret[channel] += BMSUtil.IntToHex((int)bkey.value);
		}
		
		// return beat string
		String res = "";
		for (int i=0; i<1322; i++) {
			if (ret[i] == null) continue;
			res += "#" + String.format("%03d", beat) + String.format("%02X", i);
			res += ret[i] + "\n";
		}
		return res;
	}
	
	private static int[] GetBeatFraction(double beat) {
		for (int i=1; i<=128; i++) {
			if (beat*i % 1 == 0) {
				return new int[] {(int)(beat*i), i};
			}
		}
		
		// 128 넘으면? 그냥 없는 걸로.
		return new int[]{0,0};
	}
	
	/*
	 * Useless now!
	 * 
	// this command MUST be called after sorting!
	// only proc LONGNOTE TYPE 2
	private void ProcessLongnote(BMSData bd) {
		for (int i=0; i<bd.bmsdata.size(); i++) {
			BMSKeyData d = bd.bmsdata.get(i);
			if (d.key > 50 && d.key<60) {
				if (d.attr == 1) {
					// LNTYPE 1
					for (int j=i+1; j<bd.bmsdata.size(); j++) {
						if (bd.bmsdata.get(j).key == d.key && bd.bmsdata.get(j).attr == 1) {
							bd.bmsdata.get(j).attr = 4;	// longnote end attr = 4
							bd.notecnt++;
							break;
						}
					}
					d.attr = 0;
				} else if (d.attr == 2) {
					// LNTYPE 2
					int prevIndex=-1;
					for (int j=i+1; j<bd.bmsdata.size(); j++) {
						if (bd.bmsdata.get(j).key == d.key && bd.bmsdata.get(j).attr == 2) {
							if (d.value != bd.bmsdata.get(j).key)
								break;
							if (prevIndex > 0) {
								bd.bmsdata.remove(prevIndex);
								j--;
							}
							bd.bmsdata.get(j).attr = 4;	// longnote end attr = 4
							prevIndex = j;
							bd.notecnt++;
							break;
						}
					}
					d.attr = 0;
				}
			}
		}
	}*/
}
