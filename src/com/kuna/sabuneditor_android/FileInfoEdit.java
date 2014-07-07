package com.kuna.sabuneditor_android;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class FileInfoEdit extends Activity {
	private EditText tvTitle, tvArtist, tvGenre, tvSubtitle, 
		tvPlayer, tvBPM, tvPlaylevel, tvDifficulty, tvRank, tvTotal,
		tvStagefile;
	
	Context c;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.edit_bmsinfo);
		c = this;

		tvTitle = (EditText)findViewById(R.id.tvTitle);
		tvArtist = (EditText)findViewById(R.id.tvArtist);
		tvSubtitle = (EditText)findViewById(R.id.tvSubtitle);
		tvGenre = (EditText)findViewById(R.id.tvGenre);
		tvPlayer = (EditText)findViewById(R.id.tvPlayer);
		tvBPM = (EditText)findViewById(R.id.tvBPM);
		tvPlaylevel = (EditText)findViewById(R.id.tvPlaylevel);
		tvDifficulty = (EditText)findViewById(R.id.tvDifficulty);
		tvRank = (EditText)findViewById(R.id.tvRank);
		tvTotal = (EditText)findViewById(R.id.tvTotal);
		tvStagefile = (EditText)findViewById(R.id.tvStagefile);

		tvTitle.setText(Program.bmsdata.title);
		tvArtist.setText(Program.bmsdata.artist);
		tvSubtitle.setText(Program.bmsdata.subtitle);
		tvGenre.setText(Program.bmsdata.genre);
		tvPlayer.setText(Integer.toString(Program.bmsdata.player));
		tvBPM.setText(Integer.toString(Program.bmsdata.BPM));
		tvPlaylevel.setText(Integer.toString(Program.bmsdata.playlevel));
		tvDifficulty.setText(Integer.toString(Program.bmsdata.difficulty));
		tvRank.setText(Integer.toString(Program.bmsdata.rank));
		tvTotal.setText(Integer.toString(Program.bmsdata.total));
		tvStagefile.setText(Program.bmsdata.stagefile);
		
		Button b;
		b = (Button)findViewById(R.id.btnOK);
		b.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				Program.bmsdata.title = tvTitle.getText().toString();
				Program.bmsdata.artist = tvArtist.getText().toString();
				Program.bmsdata.subtitle = tvSubtitle.getText().toString();
				Program.bmsdata.genre = tvGenre.getText().toString();
				Program.bmsdata.player = Integer.parseInt(tvPlayer.getText().toString());
				Program.bmsdata.BPM = Integer.parseInt(tvBPM.getText().toString());
				Program.bmsdata.playlevel = Integer.parseInt(tvPlaylevel.getText().toString());
				Program.bmsdata.difficulty = Integer.parseInt(tvDifficulty.getText().toString());
				Program.bmsdata.rank = Integer.parseInt(tvRank.getText().toString());
				Program.bmsdata.total = Integer.parseInt(tvTotal.getText().toString());
				Program.bmsdata.stagefile = tvStagefile.getText().toString();
				finish();
			}
		});
		

		b = (Button)findViewById(R.id.btnCancel);
		b.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				finish();
			}
		});
	}
}
