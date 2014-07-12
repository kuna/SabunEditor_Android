package com.kuna.sabuneditor_android;

import java.util.ArrayList;
import java.util.List;

import com.kuna.sabuneditor_android.bms.BMSKeyData;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

public class EditView extends View {
	public static int sizeOfBeat = 960;		// multiplication of size per beat
	public static int sizeOfColumn = 48;		// multiplication of size per column
	public static int viewWidth;
	public static int viewHeight;
	
	public int ScrollX; // from bottom
	public int ScrollY;
	public int AccelX;
	public int AccelY;

	private Paint textDebugPaint;
	
	private Thread tScroll;
	private boolean scrollWorking;
	
	View v;
	
	public EditView(Context c) {
		super(c);
		v = this;
		
		// init variables
		viewWidth = 800;
		viewHeight = 800;
		
		
		textDebugPaint = new Paint();
		textDebugPaint.setTextSize(12);
		textDebugPaint.setColor(Color.WHITE);
		
		EditViewBackground.init();
		EditViewNote.init(c);
		
		// scroll thread
		scrollWorking = true;
		tScroll = new Thread() {
			public void run() {
				while (scrollWorking) {
					try {
						// move Accl
						if (lastEvent == MotionEvent.ACTION_UP) {
							ScrollY += AccelY;
							ScrollX += AccelX;
							if (ScrollY < 0) ScrollY = 0;
							if (ScrollX > 0) ScrollX = 0;
							if (AccelY > 0)
								AccelY--;
							if (AccelY < 0)
								AccelY ++;
							if (AccelX > 0)
								AccelX--;
							if (AccelX < 0)
								AccelX ++;
						}
					
						Thread.sleep(30);
					} catch (Exception e) {
					}
					v.postInvalidate();
				}
			};
		};
		tScroll.start();
	}
	
	@Override
	protected void onDetachedFromWindow() {
		scrollWorking = false;
		super.onDetachedFromWindow();
	}
	
	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		
		viewWidth = w;
		viewHeight = h;
	}
	
	public void moveScroll(int x, int y) {
		ScrollX += x;
		ScrollY += y;
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		canvas.drawColor(Color.BLACK);
		
		//
		// default values
		//
		
		//*****************************
		// draw backgrounds
		//*****************************
		int x = ScrollX, y = ScrollY;
		Rect r;
		
		// draw bg
		EditViewBackground.draw(canvas, x, y);

		canvas.drawText(String.format("NOTE %d", Program.bmsdata.bmsdata.size()), 10, 40, textDebugPaint);
		
		// draw lines and colors
		EditViewNote.draw(canvas, x, y);
		
		//super.onDraw(canvas);
	}
	
	private float px, py;		// previous position
	private float sx, sy;
	private int _sx, _sy;
	private float zx, zy;		// standard of zooming
	private int zWid, zHei;		// standard of zooming
	private int sizeOfBeat_backup, sizeOfColumn_backup, ScrollY_backup;
	private int pointerCnt = 0;
	
	private boolean moveSelected;
	public static int moveX, moveY;
	private int lastEvent;
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		super.onTouchEvent(event);
		float x = event.getX(0);
		float y = event.getY(0);
		pointerCnt = event.getPointerCount();
		lastEvent = event.getAction();
		BMSKeyData rmData;
		
		// temp
		int col;
		
		if ((event.getAction() & MotionEvent.ACTION_MASK) == MotionEvent.ACTION_POINTER_DOWN) {
			switch (EditActivity.editMode) {
			case EditActivity.EDITMODE_MOVE:
				// check pointer count
				if (pointerCnt == 2) {
					zx = event.getX(1);
					zy = event.getY(1);
					zWid = (int) (x-zx);
					zHei = (int) (y-zy);
					sizeOfBeat_backup = sizeOfBeat;
					sizeOfColumn_backup = sizeOfColumn;
					ScrollY_backup = ScrollY;
				}
				break;
			case EditActivity.EDITMODE_SELECT:
				break;
			case EditActivity.EDITMODE_WRITE:
				break;
			case EditActivity.EDITMODE_DELETE:
				break;
			}
		} else if (event.getAction() == MotionEvent.ACTION_DOWN) {
			switch (EditActivity.editMode) {
			case EditActivity.EDITMODE_MOVE:
				sx = x;
				sy = y;
				_sx = ScrollX;
				_sy = ScrollY;
				break;
			case EditActivity.EDITMODE_SELECT:
				if (SelectBMSKeyData.isSelected()) {
					sx = x;
					sy = y;
					moveSelected = true;
				} else {
					moveSelected = false;
				}
				break;
			case EditActivity.EDITMODE_WRITE:
				break;
			case EditActivity.EDITMODE_DELETE:
				break;
			}
		} else if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
			switch (EditActivity.editMode) {
			case EditActivity.EDITMODE_MOVE:
				if (pointerCnt == 2) {
					ScrollY = ScrollY_backup;
					if (AccelY < 10)
						AccelY = 0;
				}
				break;
			case EditActivity.EDITMODE_SELECT:
				if (moveSelected) {
					EditHistory.AddHistory(SelectBMSKeyData.selectData, EditHistory.ACTION_DELETE);
					for (BMSKeyData bkd: SelectBMSKeyData.selectData) {
						if (bkd == null) {
							Log.i("UNEXCEPTED", "UNEXCEPTED");
							continue;
						}
						int ncol = getColFromChannel( bkd.getChannel() ) + moveX/sizeOfColumn;
						setChannelFromCol(bkd, ncol);
						BMSKeyData _bkd = Program.bmsdata.getBeatFromPosition(sizeOfBeat, 
								(int)Program.bmsdata.getNotePosition(sizeOfBeat, (int)bkd.getBeat(), bkd.getNumerator()) - moveY);
						bkd.setBeat(_bkd.getBeat(), Program.bmsdata);
						bkd.setPosY( 
								(int)Program.bmsdata.getNotePosition(100, (int)bkd.getBeat(), bkd.getBeat()%1) );
						//bkd.setBeat(bkd.getBeat() + (double)-moveY/sizeOfBeat, Program.bmsdata); OLD VERSION
					}
					EditHistory.AddHistory(SelectBMSKeyData.selectData, EditHistory.ACTION_ADD);
					
					moveX = 0;
					moveY = 0;
				} else {
					// make wav/bmp selection to that key and play that sound
					if (SelectBMSKeyData.selectData.size() > 0
						&& !SelectBMSKeyData.selectData.get(0).isBGAChannel()
						&& !SelectBMSKeyData.selectData.get(0).isBGALayerChannel() ) {
						EditActivity.editNoteVal = (int)SelectBMSKeyData.selectData.get(0).getValue();
						if (!SelectBMSKeyData.selectData.get(0).isPoorChannel() 
							&& !SelectBMSKeyData.selectData.get(0).isBPMChannel() 
							&& !SelectBMSKeyData.selectData.get(0).isSTOPChannel()) {
							Program.PlaySound(Program.bmsdata.dir + 
									Program.bmsdata.getWAV(EditActivity.editNoteVal) );
							EditActivity.editNoteMode = 0;
						} else {
							EditActivity.editNoteMode = 1;
						}
						EditActivity.refreshNoteLabel();
					}
				}
				this.postInvalidate();
				break;
			case EditActivity.EDITMODE_WRITE:
				// check note already exists
				// if not exists, then insert.
				if (EditViewNote.ghostNote == null)
					break;	// ????
				
				if (!Program.bmsdata.isNoteAlreadyExists((int) EditViewNote.ghostNote.getBeat(), EditViewNote.ghostNote.getNumerator()
						, EditViewNote.ghostNote.getChannel(), EditViewNote.ghostNote.getLayerNum())) {
					Program.bmsdata.addNote(EditViewNote.ghostNote);
					EditHistory.AddHistory(EditViewNote.ghostNote, EditHistory.ACTION_ADD);
				}

				EditViewNote.ghostNote = null;
				this.postInvalidate();
				break;
			case EditActivity.EDITMODE_DELETE:
				break;
			}
		} else if (event.getAction() == MotionEvent.ACTION_MOVE) {
			switch (EditActivity.editMode) {
			case EditActivity.EDITMODE_MOVE:
				if (pointerCnt == 2) {
					sizeOfBeat = sizeOfBeat_backup + (int)(zHei - (y - zy));
					sizeOfColumn = sizeOfColumn_backup + (int)(zWid - (x - zx)) / 3;
					ScrollY = (int) (ScrollY_backup * (double)sizeOfBeat / sizeOfBeat_backup);
					if (sizeOfBeat < 100) sizeOfBeat = 100;
					if (sizeOfColumn < 10) sizeOfColumn = 10;
					//Log.i("VAL", String.format("%d, %d, %d", zWid, sizeOfBeat, sizeOfColumn));
				} else {
					ScrollX = _sx + (int) (x - sx);
					ScrollY = _sy + (int) (y - sy);
					if (ScrollX > 0) ScrollX = 0;
					if (ScrollY < 0) ScrollY = 0;
					AccelY = (int) (y - py);
					AccelX = (int) (x - px);
				}
				this.postInvalidate();
				break;
			case EditActivity.EDITMODE_SELECT:
				if (moveSelected) {
					// draw them as moved
					moveX = (int) (x - sx);
					moveY = (int) (y - sy);
					moveX = (int)(moveX / sizeOfColumn) * sizeOfColumn;
					moveY = (int)(moveY / (int)(sizeOfBeat / EditActivity.editBeat)) * (int)(sizeOfBeat / EditActivity.editBeat);
				} else {
					// add in selection
					//rmData = Program.bmsdata.getBeatFromPosition(sizeOfBeat, (int) (ScrollY+(viewHeight - y)));
					rmData = new BMSKeyData();
					col = EditViewNote.getColFromX((int) (x - ScrollX));
					setChannelFromCol(rmData, col);
					
					for (BMSKeyData bkd: Program.bmsdata.bmsdata) {
						if (Math.abs(bkd.getPosY(sizeOfBeat / 100.0) - (ScrollY+(viewHeight - y))) < 10)
							SelectBMSKeyData.addBMSData(bkd);
					}
					/*BMSKeyData bkd = Program.bmsdata.getNote((int) rmData.getBeat(), rmData.getNumerator()
							, rmData.getChannel(), rmData.getLayerNum());*/
				}
				this.postInvalidate();
				break;
			case EditActivity.EDITMODE_WRITE:
				EditViewNote.ghostNote = Program.bmsdata.getBeatFromPosition(sizeOfBeat, (int) (ScrollY+(viewHeight - y)));
				EditViewNote.ghostNote.setValue(EditActivity.editNoteVal);
				col = EditViewNote.getColFromX((int) (x - ScrollX));
				setChannelFromCol(EditViewNote.ghostNote, col);
				
				// set numerator
				Program.bmsdata.setNumeratorFit(EditViewNote.ghostNote, EditActivity.editBeat);
				EditViewNote.ghostNote.setPosY( 
						(int)Program.bmsdata.getNotePosition(100, (int)EditViewNote.ghostNote.getBeat(), EditViewNote.ghostNote.getBeat()%1) );
				
				//Log.i("DATA", String.format("%d, %d, %f", (int) (ScrollY+(viewHeight - y)), EditViewNote.ghostNote.getNumerator(), EditViewNote.ghostNote.getBeat()));
				this.postInvalidate();
				break;
			case EditActivity.EDITMODE_DELETE:
				rmData = Program.bmsdata.getBeatFromPosition(sizeOfBeat, (int) (ScrollY+(viewHeight - y)));
				col = EditViewNote.getColFromX((int) (x - ScrollX));
				setChannelFromCol(rmData, col);
				
				// check if data exists (if exists, then remove)
				if (Program.bmsdata.removeNote((int) rmData.getBeat(), rmData.getNumerator()
						, rmData.getChannel(), rmData.getLayerNum())) {
					EditHistory.AddHistory(rmData, EditHistory.ACTION_DELETE);
				}
				
				this.postInvalidate();
				break;
			}
		}
		
		px = x;
		py = y;
		return true;	// dont return event
	}
	
	private void setChannelFromCol(BMSKeyData bkd, int col) {
		if (col == 0) {
			bkd.setBPMChannel();
		} else if (col == 1) {
			bkd.setSTOPChannel();
		} else if (col >= 2 && col <= 9) {
			if (col == 2)
				bkd.set1PKey(8);
			else
				bkd.set1PKey(col-2);
		} else if (col >= 10 && col <= 17) {
			bkd.set2PKey(col - 9);
		} else if (col == 18) {
			bkd.setBGAChannel();
		} else if (col == 19) {
			bkd.setBGALayerChannel();
		} else if (col == 20) {
			bkd.setPoorChannel();
		} else {
			bkd.setBGMChannel(col-20);
		}
	}

	public static int getColFromChannel(int channel) {
		BMSKeyData bkd = new BMSKeyData();
		bkd.setChannel(channel);
		if (bkd.isBPMChannel() || bkd.isBPMExtChannel()) {
			return 0;
		} else if (bkd.isSTOPChannel()) {
			return 1;
		} else if (bkd.is1PChannel() || bkd.is1PLNChannel() || bkd.is1PTransChannel()) {
			// 2~9
			if (bkd.getKeyNum() == 8)
				return 2;
			else
				return 2 + bkd.getKeyNum();
		} else if (bkd.is2PChannel() || bkd.is2PLNChannel() || bkd.is2PTransChannel()) {
			// 10~17
			return 9 + bkd.getKeyNum();
		} else if (bkd.isBGAChannel()) {
			return 18;
		} else if (bkd.isBGALayerChannel()) {
			return 19;
		} else if (bkd.isPoorChannel()) {
			return 20;
		} else if (bkd.isBGMChannel()) {
			return 21 + bkd.getLayerNum()-1;
		}
		return -1;
	}
}
