package com.kuna.sabuneditor_android;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.AnimationDrawable;
import android.os.Handler;
import android.view.WindowManager.LayoutParams;
import android.widget.ImageView;

public class LoadingDialog {
	private static class DialogArgs {
		public static Context c;
		public static String title;
		public static String desc;
	}
	
	private static ProgressDialog pd;
	private static boolean showDialog = false;

	public static void showDialog(Context c, String title, String desc) {
		DialogArgs.c = c;
		DialogArgs.title = title;
		DialogArgs.desc = desc;
		Activity a = (Activity) DialogArgs.c;
		showDialog = true;
		a.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if (!showDialog)
					return;
				pd = ProgressDialog.show(DialogArgs.c, DialogArgs.title, DialogArgs.desc);
			}
		});
	}
	
	public static void hideDialog() {
		if (pd != null)
			pd.dismiss();
		showDialog = false;
	}
}
