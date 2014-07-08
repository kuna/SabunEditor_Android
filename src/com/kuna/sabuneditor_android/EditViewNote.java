package com.kuna.sabuneditor_android;

import com.kuna.sabuneditor_android.bms.BMSData;
import com.kuna.sabuneditor_android.bms.BMSKeyData;
import com.kuna.sabuneditor_android.bms.BMSUtil;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.util.Log;

public class EditViewNote {
	private static final int noteHeight = 13;
	
	private static Bitmap notes;
	private static Context c;
	private static Bitmap[] note = new Bitmap[6];	// yellow, grey, blue, green, red, LN
	private static Paint textChannel;
	private static Paint noteTrans;
	private static Paint noteSelected;
	
	public static BMSKeyData ghostNote = null; 
	
	public static void init(Context c) {
		EditViewNote.c = c;
		BitmapFactory.Options o = new Options();
	    o.inScaled = false;
		notes = BitmapFactory.decodeResource(c.getResources(), R.drawable.notes, o);
		for (int i=0; i<6; i++) {
			note[i] = Bitmap.createBitmap(notes, 0, i*10, 27, 10);
		}
		
		textChannel = new Paint();
		textChannel.setColor(Color.parseColor("#FFFFFF"));
		textChannel.setTextSize(14);
		textChannel.setTextAlign(Align.CENTER);

		noteTrans = new Paint();
		noteTrans.setAlpha(128);
		
		noteSelected = new Paint();
		noteSelected.setColor(Color.WHITE);
		noteSelected.setStyle(Style.STROKE);
		noteSelected.setStrokeWidth(2);
	}
	
	public static int getPositionX(int col) {
		int scolumn=(int) (EditView.sizeOfColumn);
		int r = col * scolumn;
		r += 5;
		if (col >= 2) r += 5;
		if (col >= 10) r += 5;
		if (col >= 18) r += 5;
		if (col >= 21) r += 5;
		return r;
	}
	
	public static int getColFromX(int x) {
		int scolumn=(int) (EditView.sizeOfColumn);
		int col = 0;
		while (x > 0) {
			x -= scolumn;
			if (col == 2)
				x -= 5;
			else if (col == 10)
				x -= 5;
			else if (col == 18)
				x -= 5;
			else if (col == 21)
				x -= 5;
			col ++;
		}
		
		if (col > 20+32)
			col = 20+32;
		return col;
	}
	
	public static void draw(Canvas c, int x, int y) {
		BMSData bd = Program.bmsdata;
		
		for (BMSKeyData bkd: bd.bmsdata) {
			drawBMSKeyData(bkd, c, x, y);
		}
		
		for (BMSKeyData bkd: bd.bgadata) {
			drawBMSKeyData(bkd, c, x, y);
		}
		
		for (BMSKeyData bkd: bd.bgmdata) {
			drawBMSKeyData(bkd, c, x, y);
		}
		
		// draw ghost
		drawBMSKeyData(ghostNote, c, x, y);
	}
	
	public static void drawBMSKeyData(BMSKeyData bkd, Canvas c, int x, int y) {
		if (bkd == null)
			return;
		
		int scolumn=(int) (EditView.sizeOfColumn);
		
		// set bitmap
		Bitmap noteBitmap;
		int col = 0;
		Paint notePaint = null;
		
		if (bkd.isBPMChannel() || bkd.isSTOPChannel() || bkd.isBPMExtChannel())
			noteBitmap = note[0];	// yellow
		else if (bkd.is1PChannel() || bkd.is2PChannel() || bkd.is1PLNChannel() || bkd.is2PLNChannel() || bkd.is1PTransChannel() || bkd.is2PTransChannel()) {
			if (bkd.is1PLNChannel() || bkd.is2PLNChannel()) {
				noteBitmap = note[5];		// LN
			} else {
				if (bkd.getKeyNum() == 8) {	// SC
					noteBitmap = note[4]; // red
				} else if (bkd.getKeyNum() % 2 == 1) {
					noteBitmap = note[1]; // grey
				} else {
					noteBitmap = note[2]; // blue
				}
			}
		} else if (bkd.isBGAChannel() || bkd.isBGALayerChannel() || bkd.isPoorChannel()) {
			noteBitmap = note[3];	// green
		} else if (bkd.isBGMChannel()) {
			noteBitmap = note[4];	// red
		} else {
			noteBitmap = null;		// unknown - Dont draw
		}
		
		if (noteBitmap == null)
			return;
		
		// set position
		if (bkd.isBPMChannel() || bkd.isBPMExtChannel()) {
			col = 0;
		} else if (bkd.isSTOPChannel()) {
			col = 1;
		} else if (bkd.is1PChannel() || bkd.is1PLNChannel() || bkd.is1PTransChannel()) {
			// 2~9
			if (bkd.is1PTransChannel())
				notePaint = noteTrans;
			if (bkd.getKeyNum() == 8)
				col = 2;
			else
				col = 2 + bkd.getKeyNum();
		} else if (bkd.is2PChannel() || bkd.is2PLNChannel() || bkd.is2PTransChannel()) {
			// 10~17
			if (bkd.is2PTransChannel())
				notePaint = noteTrans;
			col = 9 + bkd.getKeyNum();
		} else if (bkd.isBGAChannel()) {
			col = 18;
		} else if (bkd.isBGALayerChannel()) {
			col = 19;
		} else if (bkd.isPoorChannel()) {
			col = 20;
		} else if (bkd.isBGMChannel()) {
			col = 21 + bkd.getLayerNum()-1;
		}
		
		int px = getPositionX(col) + x;
		int py = bkd.getPosY(EditView.sizeOfBeat / 100.0);//(int) Program.bmsdata.getNotePosition(EditView.sizeOfBeat, (int)bkd.getBeat(), bkd.getNumerator());
		py = EditView.viewHeight - py + y;
		
		if (px < -scolumn || px > EditView.viewWidth || py > EditView.viewHeight+noteHeight || py < 0)
			return;
		
		c.drawBitmap(noteBitmap, null, new Rect(px, py-noteHeight, px+scolumn, py), notePaint);
		if (SelectBMSKeyData.isSelected(bkd)) {
			int npx = px + EditView.moveX;
			int npy = py + EditView.moveY;
			c.drawRect(new Rect(npx, npy-noteHeight, npx+scolumn, npy), noteSelected);
		}
		
		String printStr = null;
		if (bkd.isBPMChannel() || bkd.isBPMExtChannel() || bkd.isSTOPChannel())
			printStr = Double.toString(bkd.getValue());
		else
			printStr = BMSUtil.IntToExtHex((int) bkd.getValue());
		c.drawText(printStr, px+scolumn/2, py, textChannel);
	}
}
