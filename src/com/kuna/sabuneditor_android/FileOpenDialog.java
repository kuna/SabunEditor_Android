package com.kuna.sabuneditor_android;

import java.io.File;
import java.io.FilenameFilter;
import java.util.concurrent.Semaphore;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

public class FileOpenDialog {	
	//In an Activity
	private String[] mFileList;
	private File mPath;
	private String mChosenFile;
	private static final String FTYPE = ".bms|.bme";
	private Context c;
	private Handler h;

	public FileOpenDialog(Context c, Handler h) {
		this.c = c;
		this.h = h;
		mPath = new File(Environment.getExternalStorageDirectory().getAbsolutePath());
	}

	private void loadFileList(String path, final boolean onlyDirectory) {
		File nPath = new File(path);
	    try {
	    	nPath.mkdirs();
	    }
	    catch(SecurityException e) {
	        Log.e("fileopendialog", "unable to write on the sd card " + e.toString());
	    }
	    if(nPath.exists()) {
	        FilenameFilter filter = new FilenameFilter() {
	            public boolean accept(File dir, String filename) {
	                File sel = new File(dir, filename);
	                if (sel.isDirectory())
	                	return true;
	                if (onlyDirectory)
	                	return false;
	                for (String s: FTYPE.split("\\|")) {
	                	if (filename.toUpperCase().indexOf(s.toUpperCase()) > 0)
	                		return true;
	                }
	                return false;
	            }
	        };
	        mFileList = nPath.list(filter);
	    }
	    else {
	        mFileList = new String[0];
	    }
	}

	private boolean onlyDirectory;
	private Dialog CreateDialog(final String path) {
	    Dialog dialog = null;
	    AlertDialog.Builder builder = new AlertDialog.Builder(c);
        final EditText input = new EditText(c);

        builder.setTitle("Choose your file");
        if(mFileList == null) {
            Log.e("fileopendialog", "Showing file picker before loading the file list");
            dialog = builder.create();
            return dialog;
        }
        
        builder.setItems(mFileList, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                String nPath = path + "/" + mFileList[which];
                if (new File(nPath).isDirectory()) {
            		loadFileList(nPath, onlyDirectory);
                	CreateDialog(nPath);
                } else {
                	mChosenFile = nPath;
                	h.obtainMessage(0, mChosenFile).sendToTarget();
                }
            }
        });

        if (onlyDirectory) {
	        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int whichButton) {
                	mChosenFile = path;
                	if (askFilenameDialog) {
						AlertDialog.Builder alert = new AlertDialog.Builder(c);

						alert.setTitle("set file name");
						alert.setMessage(mChosenFile);
						final EditText input = new EditText(c);
						alert.setView(input);
						input.setText(".bme");
						alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int whichButton) {
								String fname = input.getText().toString();
								mChosenFile = mChosenFile + "/" + fname;
		                    	h.obtainMessage(0, mChosenFile).sendToTarget();
								dialog.dismiss();
							}
						});
						alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int whichButton) {
								// just dismiss
								dialog.dismiss();
							}
						});
						
						alert.show();
						dialog.dismiss();
                	} else {
                    	h.obtainMessage(0, mChosenFile).sendToTarget();
    					dialog.dismiss();
                	}
				}
			});
	        
	        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int whichButton) {
					dialog.dismiss();
				}
			});
        }
        
	    dialog = builder.show();
	    return dialog;
	}

	private boolean askFilenameDialog = false;
	public void SaveDialog() {
		OpenDialog(true, true);
	}

	public void OpenDialog() {
		OpenDialog(false);
	}

	public void OpenDialog(boolean onlyDir) {
		OpenDialog(onlyDir, false);
	}
	
	public void OpenDialog(boolean onlyDir, boolean askfilename) {
		onlyDirectory = onlyDir;
		askFilenameDialog = askfilename;
		loadFileList(mPath.getPath(), onlyDirectory);
		CreateDialog(mPath.getPath());
	}
	
	public String getChosenFile() {
		return mChosenFile;
	}
}
