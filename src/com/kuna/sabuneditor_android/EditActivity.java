package com.kuna.sabuneditor_android;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.kuna.sabuneditor_android.bms.BMSData;
import com.kuna.sabuneditor_android.bms.BMSKeyData;
import com.kuna.sabuneditor_android.bms.BMSParser;
import com.kuna.sabuneditor_android.bms.BMSUtil;
import com.kuna.sabuneditor_android.bms.BMSWriter;

import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class EditActivity extends Activity {
	EditView ev;
	ListView lv;
	Context c;
	
	public static final int EDITMODE_MOVE = 0;
	public static final int EDITMODE_SELECT = 1;
	public static final int EDITMODE_WRITE = 2;
	public static final int EDITMODE_DELETE = 3;
	
	public static int editMode = 0;
	public static int editBeat = 4;
	public static int editNoteVal = 1;
	public static int editNoteMode = 0;	// 0: WAV, 1: BMP

	private Button btnMove, btnSelect, btnWrite, btnDelete;
	private Button btnBeat4, btnBeat8, btnBeat16, btnBeat32, btnBeat64, btnBeatFree;
	private static TextView tvNote;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.editview);
		c = this;
		
		// init bms data
		Program.bmsdata = new BMSData();
		EditHistory.initHistory();
		
		// add edit view
		LinearLayout layout = (LinearLayout)findViewById(R.id.editlayout);
		ev = new EditView(this);
		layout.addView(ev);
		
		// initalize components
		Button b;
		b = (Button)findViewById(R.id.btnNew);
		b.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Program.bmsdata = new BMSData();
				ev.postInvalidate();
			}
		});
		
		b = (Button)findViewById(R.id.btnload);
		b.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// Environment.getExternalStorageDirectory().getAbsolutePath()
				// Environment.getRootDirectory().getAbsolutePath()
				Handler h = new Handler() {
					@Override
					public void dispatchMessage(Message msg) {
						Log.i("OPEN", msg.obj.toString());
						startBMSLoad(msg.obj.toString());
						ev.postInvalidate();
						super.dispatchMessage(msg);
					}
				};
				FileOpenDialog fDialog = new FileOpenDialog(c, h);
				fDialog.OpenDialog();
			}
		});

		b = (Button)findViewById(R.id.btnsave);
		b.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Handler h = new Handler() {
					@Override
					public void dispatchMessage(final Message msg) {
						Log.i("SAVE", msg.obj.toString());
						startBMSSave(msg.obj.toString());
						super.dispatchMessage(msg);
					}
				};
				FileOpenDialog fDialog = new FileOpenDialog(c, h);
				fDialog.SaveDialog();
			}
		});

		b = (Button)findViewById(R.id.btnFileInfo);
		b.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(c, FileInfoEdit.class);
				startActivity(intent); 
			}
		});

		b = (Button)findViewById(R.id.btnUndo);
		b.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				EditHistory.doUndo(Program.bmsdata);
				ev.postInvalidate();
			}
		});

		b = (Button)findViewById(R.id.btnRedo);
		b.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				EditHistory.doRedo(Program.bmsdata);
				ev.postInvalidate();
			}
		});

		b = (Button)findViewById(R.id.btnPaste);
		b.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// get current beat
				int nowBeat = (int)Program.bmsdata.getBeatFromPosition(EditView.sizeOfBeat, ev.ScrollY).getBeat();
				
				// copy data of selected notes
				List<BMSKeyData> arr = BMSUtil.cloneKeyArray( SelectBMSKeyData.selectData );
				Log.i("AA", Integer.toString(nowBeat));
				for (BMSKeyData bkd: arr) {
					bkd.setBeat( bkd.getBeat()%1 + nowBeat , Program.bmsdata );
				}
				
				// add to bmsdata
				for (BMSKeyData bkd: arr) {
					if (!Program.bmsdata.isNoteAlreadyExists((int) bkd.getBeat(), bkd.getNumerator(), bkd.getChannel(), bkd.getLayerNum()))
						Program.bmsdata.bmsdata.add(bkd);
				}
				ev.postInvalidate();
			}
		});

		b = (Button)findViewById(R.id.btnPreview);
		b.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Handler h = new Handler() {
					@Override
					public void dispatchMessage(Message msg) {
						try {
							ComponentName cname = new ComponentName("com.kuna.rhythmus", "com.kuna.rhythmus.MainActivity");
							
							Intent intent = new Intent();
							intent.setComponent(cname);
							intent.putExtra("File", Program.bmsdata.dir + "__sample_sabuneditor.bme_");
							Log.i("BEAT", Double.toString(Program.bmsdata.getBeatFromPosition(EditView.sizeOfBeat, ev.ScrollY).getBeat()));
							intent.putExtra("Beat", Program.bmsdata.getBeatFromPosition(EditView.sizeOfBeat, ev.ScrollY).getBeat());
							intent.putExtra("RemoveAfterPlay", true);
							startActivity(intent);
						} catch (Exception e) {
							e.printStackTrace();
							Toast.makeText(c, "no rhythmus BMS emulator found!", Toast.LENGTH_LONG).show();
							new File(Program.bmsdata.dir + "__sample_sabuneditor.bme_").delete();
						}
						
						super.dispatchMessage(msg);
					}
				};
				
				// store BMS first
				startBMSSave(Program.bmsdata.dir + "__sample_sabuneditor.bme_", h);
			}
		});

		btnMove = (Button)findViewById(R.id.btnMove);
		btnSelect = (Button)findViewById(R.id.btnSelect);
		btnWrite = (Button)findViewById(R.id.btnWrite);
		btnDelete = (Button)findViewById(R.id.btnDelete);
		
		btnMove.setOnClickListener(btnModeChangeClass);
		btnSelect.setOnClickListener(btnModeChangeClass);
		btnWrite.setOnClickListener(btnModeChangeClass);
		btnDelete.setOnClickListener(btnModeChangeClass);

		btnBeat4 = (Button)findViewById(R.id.btnBeat4);
		btnBeat8 = (Button)findViewById(R.id.btnBeat8);
		btnBeat16 = (Button)findViewById(R.id.btnBeat16);
		btnBeat32 = (Button)findViewById(R.id.btnBeat32);
		btnBeat64 = (Button)findViewById(R.id.btnBeat64);
		btnBeatFree = (Button)findViewById(R.id.btnBeatFree);

		btnBeat4.setOnClickListener(btnBeatChangeClass);
		btnBeat8.setOnClickListener(btnBeatChangeClass);
		btnBeat16.setOnClickListener(btnBeatChangeClass);
		btnBeat32.setOnClickListener(btnBeatChangeClass);
		btnBeat64.setOnClickListener(btnBeatChangeClass);
		btnBeatFree.setOnClickListener(btnBeatChangeClass);
		
		lv = (ListView)findViewById(R.id.lvelement);
		List<String> lvdata = new ArrayList<String>();
		for (int i=1; i<1296; i++) {
			lvdata.add(String.format("#%s", BMSUtil.IntToExtHex(i)));
		}
		EditListViewAdapter adapter = new EditListViewAdapter(this, lvdata);
		lv.setAdapter(adapter);
		lv.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				// set val & play music
				editNoteVal = arg2+1;
				Program.PlaySound( Program.bmsdata.dir + Program.bmsdata.getWAV(editNoteVal) );
				refreshNoteLabel();
			}
		});
		lv.setOnItemLongClickListener(new OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				editNoteVal = arg2+1;
				refreshNoteLabel();
				
				AlertDialog.Builder alert = new AlertDialog.Builder(c);

				alert.setTitle("set value");
				alert.setMessage("Enter New Value");
				
				// Set an EditText view to get user input 
				final EditText input = new EditText(c);
				alert.setView(input);
				
				String str = null;
				if (editNoteMode == 0) {
					str = Program.bmsdata.getWAV( editNoteVal );
				} else {
					str = Program.bmsdata.getBGA( editNoteVal );
				}
				if (str == null) str = "";
				input.setText(str);
				
				alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
					String value = input.getText().toString();
					if (editNoteMode == 0) {
						Program.bmsdata.setWAV( editNoteVal, value );
					} else {
						Program.bmsdata.setBGA( editNoteVal, value );
					}
				  }
				});
				
				alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
				  public void onClick(DialogInterface dialog, int whichButton) {
				    // Canceled.
				  }
				});
				
				alert.show();
				// see http://androidsnippets.com/prompt-user-input-with-an-alertdialog
				return false;
			}
		});

		b = (Button)findViewById(R.id.btnWAV);
		b.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				editNoteMode = 0;
				refreshNoteLabel();
			}
		});

		b = (Button)findViewById(R.id.btnBMP);
		b.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				editNoteMode = 1;
				refreshNoteLabel();
			}
		});
		
		tvNote = (TextView)findViewById(R.id.tvNote);
		refreshNoteLabel();
	}
	
	public void startBMSLoad(final String path) {
		final Handler h = new Handler() {
			@Override
			public void dispatchMessage(Message msg) {
				ev.postInvalidate();
				super.dispatchMessage(msg);
			}
		};
		
		new Thread(new Runnable() {
			@Override
			public void run() {
				LoadingDialog.showDialog(c, "Loading BMS ...", "please wait");

				// load file
				BMSData _bmsdata = new BMSData();
				BMSParser.LoadBMSFile(path, _bmsdata);
				
				// fill posY data
				Program.bmsdata.fillNotePosition( _bmsdata.bmsdata, 100, false);
				Program.bmsdata.fillNotePosition( _bmsdata.bgadata, 100, false);
				Program.bmsdata.fillNotePosition( _bmsdata.bgmdata, 100, false);
				
				// clear history
				EditHistory.initHistory();
				
				// sleep for prevent LoadingDialog locking
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				
				LoadingDialog.hideDialog();
				h.obtainMessage().sendToTarget();	// invalidate
				
				// replace object
				Program.bmsdata = _bmsdata;	// first, init.
			}
		}).start();
	}
	
	public void startBMSSave(String path) {
		startBMSSave(path);
	}
	
	public void startBMSSave(final String path, final Handler h_) {
		final Handler h = new Handler() {
			@Override
			public void dispatchMessage(Message msg) {
				ev.postInvalidate();
				if (h_ != null)
					h_.sendEmptyMessage(0);
				super.dispatchMessage(msg);
			}
		};
		
		new Thread(new Runnable() {
			@Override
			public void run() {
				LoadingDialog.showDialog(c, "Saving BMS ...", "please wait");

				// save BMS file
				BMSWriter.SaveBMSFile(path, Program.bmsdata);
				
				// sleep for prevent LoadingDialog locking
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				
				// we need to reset path of BMSdata
				File f = new File(path);
				Program.bmsdata.dir = path.substring(0, path.length() - f.getName().length());
				
				LoadingDialog.hideDialog();
				h.obtainMessage().sendToTarget();	// invalidate
			}
		}).start();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.open, menu);
		return true;
	}

	
	private final OnClickListener btnModeChangeClass = new OnClickListener() {
		@Override
		public void onClick(View v) {
			if (btnMove == v) {
				buttonSelected(btnMove);
				editMode = EDITMODE_MOVE;
			} else {
				buttonUnSelected(btnMove);
			}

			if (btnSelect == v) {
				buttonSelected(btnSelect);
				editMode = EDITMODE_SELECT;
				SelectBMSKeyData.clearAll();
			} else {
				buttonUnSelected(btnSelect);
			}

			if (btnWrite == v) {
				buttonSelected(btnWrite);
				editMode = EDITMODE_WRITE;
			} else {
				buttonUnSelected(btnWrite);
			}

			if (btnDelete == v) {
				buttonSelected(btnDelete);
				editMode = EDITMODE_DELETE;
			} else {
				buttonUnSelected(btnDelete);
			}
		}
	};
	
	private final OnClickListener btnBeatChangeClass = new OnClickListener() {
		@Override
		public void onClick(View v) {
			if (btnBeat4 == v) {
				buttonSelected(btnBeat4);
				editBeat = 4;
			} else {
				buttonUnSelected(btnBeat4);
			}

			if (btnBeat8 == v) {
				buttonSelected(btnBeat8);
				editBeat = 8;
			} else {
				buttonUnSelected(btnBeat8);
			}
			
			if (btnBeat16 == v) {
				buttonSelected(btnBeat16);
				editBeat = 16;
			} else {
				buttonUnSelected(btnBeat16);
			}
			
			if (btnBeat32 == v) {
				buttonSelected(btnBeat32);
				editBeat = 32;
			} else {
				buttonUnSelected(btnBeat32);
			}
			
			if (btnBeat64 == v) {
				buttonSelected(btnBeat64);
				editBeat = 64;
			} else {
				buttonUnSelected(btnBeat64);
			}
			
			if (btnBeatFree == v) {
				buttonSelected(btnBeatFree);
				editBeat = 0;
			} else {
				buttonUnSelected(btnBeatFree);
			}
		}
	};

	private void buttonSelected(Button b) {
		b.setTypeface(null, Typeface.BOLD);
		b.setTextColor(Color.RED);
	}
	
	private void buttonUnSelected(Button b) {
		b.setTypeface(null, Typeface.NORMAL);
		b.setTextColor(Color.BLACK);
	}
	
	public static void refreshNoteLabel() {
		String selectedMode = "";
		if (editNoteMode == 0)
			selectedMode = "WAV";
		else
			selectedMode = "BMP";
		tvNote.setText(String.format("#%s%s", selectedMode, BMSUtil.IntToExtHex(editNoteVal)));
	}
}
