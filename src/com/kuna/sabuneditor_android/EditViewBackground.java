package com.kuna.sabuneditor_android;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Paint.Align;
import android.util.Log;

public class EditViewBackground {
	private static Paint area1;	// BPM/STOP
	private static Paint area2;	// SC
	private static Paint area3[];	// keys
	private static Paint area4;	// BGAs
	private static Paint area5;	// others
	private static Paint clrSubline;	// 
	private static Paint clrMainline;	// 
	private static Paint textBeat;
	private static Paint textColumn;
	
	private static int x;
	
	public static void init() {

		// init paints
		area1 = new Paint();
		area1.setColor(Color.parseColor("#303000"));
		area2 = new Paint();
		area2.setColor(Color.parseColor("#300000"));
		area3 = new Paint[2];
		area3[0] = new Paint();
		area3[0].setColor(Color.parseColor("#202020"));
		area3[1] = new Paint();
		area3[1].setColor(Color.parseColor("#000028"));
		area4 = new Paint();
		area4.setColor(Color.parseColor("#001800"));
		area5 = new Paint();
		area5.setColor(Color.parseColor("#300000"));
		

		clrSubline = new Paint();
		clrSubline.setColor(Color.parseColor("#999999"));
		clrMainline = new Paint();
		clrMainline.setColor(Color.parseColor("#FFFFFF"));
		
		textBeat = new Paint();
		textBeat.setTextSize(120);
		textBeat.setTextAlign(Align.CENTER);
		textBeat.setColor(Color.parseColor("#33FFFFFF"));
		
		textColumn = new Paint();
		textColumn.setTextSize(15);
		textColumn.setTextAlign(Align.CENTER);
		textColumn.setColor(Color.parseColor("#00FF99"));
	}
	
	public static void draw(Canvas canvas, int x, int y) {
		int i;
		int scolumn=(int) (EditView.sizeOfColumn);
		int sbeat=(int) (EditView.sizeOfBeat);
		
		EditViewBackground.x = x;
		
		/*
		 * draw column
		 */
		drawMainColumnLine(canvas);
		drawColumn(canvas, area1, scolumn);		// BPM
		drawColumn(canvas, area1, scolumn);		// STOP
		drawMainColumnLine(canvas);
		drawColumn(canvas, area2, scolumn);		// 1SCR
		drawColumn(canvas, area3[0], scolumn);	// 1
		drawColumn(canvas, area3[1], scolumn);	// 1
		drawColumn(canvas, area3[0], scolumn);	// 1
		drawColumn(canvas, area3[1], scolumn);	// 1
		drawColumn(canvas, area3[0], scolumn);	// 1
		drawColumn(canvas, area3[1], scolumn);	// 1
		drawColumn(canvas, area3[0], scolumn);	// 1
		drawMainColumnLine(canvas);
		drawColumn(canvas, area3[0], scolumn);	// 2
		drawColumn(canvas, area3[1], scolumn);	// 2
		drawColumn(canvas, area3[0], scolumn);	// 2
		drawColumn(canvas, area3[1], scolumn);	// 2
		drawColumn(canvas, area3[0], scolumn);	// 2
		drawColumn(canvas, area3[1], scolumn);	// 2
		drawColumn(canvas, area3[0], scolumn);	// 2
		drawColumn(canvas, area2, scolumn);		// 2SCR
		drawMainColumnLine(canvas);
		drawColumn(canvas, area4, scolumn);		// BGA
		drawColumn(canvas, area4, scolumn);		// LAYER
		drawColumn(canvas, area4, scolumn);		// POOR
		drawMainColumnLine(canvas);
		for (i=0; i<32; i++)
			drawColumn(canvas, area5, scolumn);		// auto key
		
		/*
		 * draw row & beat
		 * for precision, cast into double
		 */
		i = 0;
		double convy = EditView.viewHeight+y;
		int beatNum = 0;
		double sbeatNum = 0;	// length per numerator
		while (convy > -sbeatNum) {
			if (i%4 == 0) {
				// calculate new sbeatNum
				sbeatNum = sbeat / (double)Program.bmsdata.getBeatDenominator(beatNum);
				
				// draw text n line
				canvas.drawLine(0, (int)convy, EditView.viewWidth, (int)convy, clrMainline);
				canvas.drawText(String.format("#%02d", beatNum), EditView.viewWidth/2, (int)convy, textBeat);
				beatNum++;
			} else {
				// draw line
				canvas.drawLine(0, (int)convy, EditView.viewWidth, (int)convy, clrSubline);
			}
			convy -= sbeatNum;
			
			i++;
			if (Program.bmsdata.getBeatNumerator(beatNum-1) == 0) {
				Log.i("error", String.format("%d, %d", beatNum, Program.bmsdata.beat_denominator[beatNum-1]));
				return;
			}
			i %= Program.bmsdata.getBeatNumerator(beatNum-1);
		}
		
		/*
		 * draw column text
		 */
		EditViewBackground.x = x;

		drawColumnText(canvas, "BPM", scolumn);
		drawColumnText(canvas, "STOP", scolumn);
		drawColumnText(canvas, null, 5);
		drawColumnText(canvas, "SC", scolumn);
		drawColumnText(canvas, "1", scolumn);
		drawColumnText(canvas, "2", scolumn);
		drawColumnText(canvas, "3", scolumn);
		drawColumnText(canvas, "4", scolumn);
		drawColumnText(canvas, "5", scolumn);
		drawColumnText(canvas, "6", scolumn);
		drawColumnText(canvas, "7", scolumn);
		drawColumnText(canvas, null, 5);
		drawColumnText(canvas, "1", scolumn);
		drawColumnText(canvas, "2", scolumn);
		drawColumnText(canvas, "3", scolumn);
		drawColumnText(canvas, "4", scolumn);
		drawColumnText(canvas, "5", scolumn);
		drawColumnText(canvas, "6", scolumn);
		drawColumnText(canvas, "7", scolumn);
		drawColumnText(canvas, "SC", scolumn);
		drawColumnText(canvas, null, 5);
		drawColumnText(canvas, "BGA", scolumn);
		drawColumnText(canvas, "LAYER", scolumn);
		drawColumnText(canvas, "POOR", scolumn);
		drawColumnText(canvas, null, 5);
		for (i=0; i<32; i++) {
			drawColumnText(canvas, String.format("B%02d", i), scolumn);
		}
	}
	
	private static void drawColumnText(Canvas canvas, String txt, int cwidth) {
		x += cwidth;
		if (txt == null) {
			return;
		}
		canvas.drawText(txt, x-cwidth/2, 20, textColumn);
	}
	
	private static void drawColumn(Canvas canvas, Paint p, int cwidth) {
		Rect r = new Rect(x, 0, x+cwidth, EditView.viewHeight);
		canvas.drawRect(r, p);
		x += cwidth;
		canvas.drawLine(x, 0, x, EditView.viewHeight, clrSubline);
	}
	
	private static void drawMainColumnLine(Canvas canvas) {
		canvas.drawLine(x, 0, x, EditView.viewHeight, clrMainline);
		x += 5;
		canvas.drawLine(x, 0, x, EditView.viewHeight, clrMainline);
	}
}
