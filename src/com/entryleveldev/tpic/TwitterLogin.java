package com.entryleveldev.tpic;

import java.util.Date;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;
import twitter4j.conf.ConfigurationBuilder;
import android.app.Activity;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;

public class TwitterLogin extends Activity {

	private final String TAG = "TwitterLogin";
	public final static String PREF_KEY_OAUTH_TOKEN = "twitter.oauth.token",
			PREF_KEY_OAUTH_SECRET = "twitter.oauth.secret", PREF_KEY_TWITTER_LOGIN = "twitter.oauth.login";

	private SharedPreferences mPreferences;

	private Twitter twitter = new TwitterFactory().getInstance();

	private Button testBtn;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.i(TAG, "Starting task to retrieve request token.");
		this.mPreferences = PreferenceManager.getDefaultSharedPreferences(this);
		super.onCreate(savedInstanceState);
		getActionBar().setTitle("TWITTER AUTHENTICATION");

		setContentView(R.layout.activity_main);
		testBtn = (Button) findViewById(R.id.test_btn);
		testBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				String token = mPreferences.getString(PREF_KEY_OAUTH_TOKEN, "");
				String secret = mPreferences.getString(PREF_KEY_OAUTH_SECRET, "");
				Log.v(TAG, "token " + token);
				Log.v(TAG, "secret "+ secret);
				init_twitter(token, secret);
			}

		});
	}

	private Twitter mTwitter;

	private void init_twitter(String tok, String sec) {

		ConfigurationBuilder cb = new ConfigurationBuilder();

		cb.setDebugEnabled(true)
				.setOAuthConsumerKey(Const.CONSUMER_KEY)
				.setOAuthConsumerSecret(Const.CONSUMER_SECRET)
				.setOAuthAccessToken(tok)
				.setOAuthAccessTokenSecret(sec);

		TwitterFactory tf = new TwitterFactory(cb.build());

		mTwitter = tf.getInstance();

		/**
		 * This always fails, even though I call this routine with the correct token & secret !!! See at the enf of
		 * message for an alternate routine like this one that makes use of verifyCredentials and also fails.
		 */

		new Thread(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				try {
					mTwitter.updateStatus("yello 2");
				} catch(TwitterException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
		}).start();

	}

	private void returnParent(boolean result) {
		setResult(result ? Activity.RESULT_OK : Activity.RESULT_CANCELED, null);
		Log.d(TAG, "TWITTER AUTH: END PROCESS , GLOBAL RESULT " + result);

		/** THE FOLLOWING THING WORKS !!!!! IT SUCCESSFULLY TWEETS */
		/*
		 * new Thread(new Runnable() {
		 * 
		 * @Override public void run() { // TODO Auto-generated method stub try { twitter.updateStatus("test"); } catch
		 * (TwitterException e) { // TODO Auto-generated catch block e.printStackTrace(); }
		 * 
		 * }}).start();
		 */

		finish();
	}

	/**
	 * Uses TWITTER4J to get the Request URL. It gets something like AUTH URL TWITTER4J IS
	 * http://api.twitter.com/oauth/authorize?oauth_token=xxxxxxxxxxxxxxxxxxxxx
	 * 
	 * @return The Request URL to open in webview and get the Verifier
	 */

	private String oauth_twitter4j_getRequestUrl() throws TwitterException {
		twitter.setOAuthConsumer(Const.CONSUMER_KEY, Const.CONSUMER_SECRET);
		RequestToken tempToken = twitter.getOAuthRequestToken(Const.CALLBACK_URL);
		return tempToken.getAuthorizationURL();
	}
/*
	@Override
	protected void onResume() {
		super.onResume();
		WebView webview = new WebView(this);
		webview.getSettings().setJavaScriptEnabled(true);
		webview.setVisibility(View.VISIBLE);
		setContentView(webview);

		Log.i(TAG, "Retrieving request token from Google servers");

		try {

			StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
			StrictMode.setThreadPolicy(policy);

			String authorizationUrl = oauth_twitter4j_getRequestUrl();
			// Log.d(TAG, "AUTH URL TWITTER4J IS "+authorizationUrl_t);

			webview.setWebViewClient(new WebViewClient() {

				@Override
				public boolean shouldOverrideUrlLoading(WebView webView, String url) {
					Log.d(TAG, "WebView: " + url);
					if(url != null && url.startsWith(Const.CALLBACK_URL)) try {
						System.out.println("TWEET TWEET TWEET");
						retrieveAccessToken(url); // added this
						webView.setVisibility(View.GONE); // added this
						return true;
					} catch(Exception e) {
						e.printStackTrace();
						returnParent(false);
						return true;
					}
					else return false;
				}

				private void saveAccessToken(AccessToken accessToken) {

					// Shared Preferences
					Editor e = mPreferences.edit();

					// After getting access token, access token secret
					// store them in application preferences

					e.putString(PREF_KEY_OAUTH_TOKEN, accessToken.getToken());
					e.putString(PREF_KEY_OAUTH_SECRET, accessToken.getTokenSecret());
					e.putBoolean(PREF_KEY_TWITTER_LOGIN, true);
					e.commit();

					Log.e("Twitter OAuth Token", "> " + accessToken.getToken() + "-" + accessToken.getScreenName());

				}

				private void retrieveAccessToken(String url) throws Exception {
					String requestToken = extractParamFromUrl(url, "oauth_token");
					String verifier = extractParamFromUrl(url, "oauth_verifier");
					Log.d(
						TAG,
						"Tenemos ACCESS TOKEN y VERIFIER :" + requestToken + "," + verifier + ","
							+ (new Date().toString()));

					retrieveAccessToken_with4j(verifier);
				}

				private void retrieveAccessToken_with4j(String verifier) throws TwitterException {
					AccessToken a = twitter.getOAuthAccessToken(verifier);
					saveAccessToken(a);
					returnParent(true);
				}

				private String extractParamFromUrl(String url, String paramName) {
					String queryString = url.substring(url.indexOf("?", 0) + 1, url.length());
					QueryStringParser queryStringParser = new QueryStringParser(queryString);
					return queryStringParser.getQueryParamValue(paramName);
				}

			});

			webview.loadUrl(authorizationUrl);
		} catch(Exception ex) {
			ex.printStackTrace();
		}
	}
*/
}