package com.entryleveldev.tpic;

import java.io.File;

import twitter4j.StatusUpdate;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends Activity implements OnClickListener {

	private Button buttonLogin;
	private Button postBtn;
	private static SharedPreferences mSharedPreferences;
	private static Twitter twitter;
	private static RequestToken requestToken;
	private static final String TAG = "MainActivity";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		mSharedPreferences = getSharedPreferences(Const.PREFERENCE_NAME, MODE_PRIVATE);
		buttonLogin = (Button) findViewById(R.id.twitterLogin);
		buttonLogin.setOnClickListener(this);

		
		postBtn = (Button) findViewById(R.id.post);
		postBtn.setOnClickListener(this);
		
		Uri uri = getIntent().getData();
		if(uri != null) {
			Log.v(TAG, " uri "+uri.toString());
		} else {
			Log.v(TAG, " uri is null !");
		}
		if(uri != null && uri.toString().startsWith(Const.CALLBACK_URL)) {
			String verifier = uri.getQueryParameter(Const.IEXTRA_OAUTH_VERIFIER);
			Log.v(TAG, "verifier "+verifier);
			new AccessTokenTask().execute(verifier);
		}
	}

	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		if (isConnected()) {
			String oauthAccessToken = mSharedPreferences.getString(Const.PREF_KEY_TOKEN, "");
			String oAuthAccessTokenSecret = mSharedPreferences.getString(Const.PREF_KEY_SECRET, "");

			ConfigurationBuilder confbuilder = new ConfigurationBuilder();
			Configuration conf = confbuilder
								.setOAuthConsumerKey(Const.CONSUMER_KEY)
								.setOAuthConsumerSecret(Const.CONSUMER_SECRET)
								.setOAuthAccessToken(oauthAccessToken)
								.setOAuthAccessTokenSecret(oAuthAccessTokenSecret)
								.build();
			buttonLogin.setText("connected");
		} else {
			buttonLogin.setText("not connected");
		}
	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	private boolean isConnected() {
		return mSharedPreferences.getString(Const.PREF_KEY_TOKEN, null) != null;
	}

	private void askOAuth() {
		ConfigurationBuilder configurationBuilder = new ConfigurationBuilder();
		configurationBuilder.setOAuthConsumerKey(Const.CONSUMER_KEY);
		configurationBuilder.setOAuthConsumerSecret(Const.CONSUMER_SECRET);
		Configuration configuration = configurationBuilder.build();
		twitter = new TwitterFactory(configuration).getInstance();

		try {
			requestToken = twitter.getOAuthRequestToken(Const.CALLBACK_URL);
			Log.v(TAG, "Please authorize this app!");
			// Toast.makeText(this, "Please authorize this app!", Toast.LENGTH_LONG).show();
			this.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(requestToken.getAuthenticationURL())));
		} catch(TwitterException e) {
			e.printStackTrace();
		}
	}

	private void disconnectTwitter() {
		SharedPreferences.Editor editor = mSharedPreferences.edit();
		editor.remove(Const.PREF_KEY_TOKEN);
		editor.remove(Const.PREF_KEY_SECRET);

		editor.commit();
	}

	@Override
	public void onClick(View v) {
		switch(v.getId()) {
			case R.id.twitterLogin:
				if(isConnected()) {
					disconnectTwitter();
					buttonLogin.setText("connect");
				} else {
					new AskOAuthTask().execute();
				}
				break;
			case R.id.post:
				new PostTask().execute();
		}
	}

	class AskOAuthTask extends AsyncTask<Void, Void, Void> {

		@Override
		protected Void doInBackground(Void... params) {
			askOAuth();
			return null;
		}

	}
	
	class AccessTokenTask extends AsyncTask<String, Void, Void> {

		@Override
		protected Void doInBackground(String... params) {
			//String verifier = uri.getQueryParameter(Const.IEXTRA_OAUTH_VERIFIER);
			String verifier = params[0];
			try {
				AccessToken accessToken = twitter.getOAuthAccessToken(requestToken, verifier);
				Editor e = mSharedPreferences.edit();
				e.putString(Const.PREF_KEY_TOKEN, accessToken.getToken());
				e.putString(Const.PREF_KEY_SECRET, accessToken.getTokenSecret());
				e.commit();
			} catch(Exception e) {
				Log.e(TAG, e.toString());
			}
			return null;
		}

	}
	
	public void uploadPic(Twitter twitter) throws Exception  {
	    try{
	    	String oauthAccessToken = mSharedPreferences.getString(Const.PREF_KEY_TOKEN, "");
			String oAuthAccessTokenSecret = mSharedPreferences.getString(Const.PREF_KEY_SECRET, "");

			ConfigurationBuilder confbuilder = new ConfigurationBuilder();
			Configuration conf = confbuilder
								.setOAuthConsumerKey(Const.CONSUMER_KEY)
								.setOAuthConsumerSecret(Const.CONSUMER_SECRET)
								.setOAuthAccessToken(oauthAccessToken)
								.setOAuthAccessTokenSecret(oAuthAccessTokenSecret)
								.build();
			twitter = new TwitterFactory(conf).getInstance();
	    	String path = Environment.getExternalStorageDirectory()
					.getAbsolutePath() + "/Download/test.png";
			File file = new File(path);
	        StatusUpdate status = new StatusUpdate("test");
	        status.setMedia(file);
	        twitter.updateStatus(status);}
	    catch(TwitterException e){
	        Log.d("TAG", "Pic Upload error" + e.getErrorMessage());
	        throw e;
	    }
	}

	class PostTask extends AsyncTask<Void, Void, Void> {

		@Override
		protected Void doInBackground(Void... params) {
			try {
				uploadPic(twitter);
			} catch(Exception e) {
				Log.e(TAG, e.toString());
				e.printStackTrace();
			}
			return null;
		}

	}

}
