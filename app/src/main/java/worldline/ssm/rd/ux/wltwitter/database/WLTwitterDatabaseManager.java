package worldline.ssm.rd.ux.wltwitter.database;

import java.util.ArrayList;
import java.util.List;

import worldline.ssm.rd.ux.wltwitter.WLTwitterApplication;
import worldline.ssm.rd.ux.wltwitter.pojo.Tweet;
import worldline.ssm.rd.ux.wltwitter.pojo.TwitterUser;
import worldline.ssm.rd.ux.wltwitter.utils.Constants;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

public class WLTwitterDatabaseManager {

	public static Tweet tweetFromCursor(Cursor c){
		if (null != c){
			final Tweet tweet = new Tweet();
			tweet.user = new TwitterUser();

			// Retrieve the date created
			if (c.getColumnIndex(WLTwitterDatabaseContract.DATE_CREATED) >= 0){
				tweet.dateCreated = c.getString(c.getColumnIndex(WLTwitterDatabaseContract.DATE_CREATED));
			}

			// Retrieve the user name
			if (c.getColumnIndex(WLTwitterDatabaseContract.USER_NAME) >= 0){
				tweet.user.name = c.getString(c.getColumnIndex(WLTwitterDatabaseContract.USER_NAME));
			}

			// Retrieve the user alias
			if (c.getColumnIndex(WLTwitterDatabaseContract.USER_ALIAS) >= 0){
				tweet.user.screenName = c.getString(c.getColumnIndex(WLTwitterDatabaseContract.USER_ALIAS));
			}

			// Retrieve the user image url
			if (c.getColumnIndex(WLTwitterDatabaseContract.USER_IMAGE_URL) >= 0){
				tweet.user.profileImageUrl = c.getString(c.getColumnIndex(WLTwitterDatabaseContract.USER_IMAGE_URL));
			}

			// Retrieve the text of the tweet
			if (c.getColumnIndex(WLTwitterDatabaseContract.TEXT) >= 0){
				tweet.text = c.getString(c.getColumnIndex(WLTwitterDatabaseContract.TEXT));
			}

			return tweet;
		}
		return null;
	}

	public static ContentValues tweetToContentValues(Tweet tweet){
		final ContentValues values = new ContentValues();

		// Set the date created
		if (!TextUtils.isEmpty(tweet.dateCreated)){
			values.put(WLTwitterDatabaseContract.DATE_CREATED, tweet.dateCreated);
		}

		// Set the date created as timestamp
		values.put(WLTwitterDatabaseContract.DATE_CREATED_TIMESTAMP, tweet.getDateCreatedTimestamp());

		// Set the user name
		if (!TextUtils.isEmpty(tweet.user.name)){
			values.put(WLTwitterDatabaseContract.USER_NAME, tweet.user.name);
		}

		// Set the user alias
		if (!TextUtils.isEmpty(tweet.user.screenName)){
			values.put(WLTwitterDatabaseContract.USER_ALIAS, tweet.user.screenName);
		}

		// Set the user image url
		if (!TextUtils.isEmpty(tweet.user.profileImageUrl)){
			values.put(WLTwitterDatabaseContract.USER_IMAGE_URL, tweet.user.profileImageUrl);
		}

		// Set the text of the tweet
		if (!TextUtils.isEmpty(tweet.text)){
			values.put(WLTwitterDatabaseContract.TEXT, tweet.text);
		}

		return values;
	}

	public static synchronized int insertTweet(Tweet tweet){
		if (null != tweet){
			if (!doesContainTweet(tweet)){
				final Uri uri = WLTwitterApplication.getContext().getContentResolver().insert(
						WLTwitterDatabaseContract.TWEETS_URI, tweetToContentValues(tweet));
				if (null != uri){
					return Integer.parseInt(uri.getLastPathSegment());
				} else {
					return -1;
				}
			}
		}
		return -1;
	}

	public static synchronized List<Tweet> getStoredTweets(){
		final List<Tweet> tweets = new ArrayList<Tweet>();
		final Cursor cursor = WLTwitterApplication.getContext().getContentResolver().query(
				WLTwitterDatabaseContract.TWEETS_URI, WLTwitterDatabaseContract.PROJECTION_FULL, null, null, null);
		if (null != cursor){
			while (cursor.moveToNext()){
				tweets.add(tweetFromCursor(cursor));
			}
		}
		if ((null != cursor) && (!cursor.isClosed())) {
			cursor.close();
		}
		return tweets;
	}

	public static synchronized void dropDatabase(){
		WLTwitterApplication.getContext().getContentResolver().delete(
				WLTwitterDatabaseContract.TWEETS_URI, null, null);
	}

	private static synchronized boolean doesContainTweet(Tweet tweet){
		boolean result = false;
		if ((null != tweet) && (!TextUtils.isEmpty(tweet.dateCreated))){
			final Cursor cursor = WLTwitterApplication.getContext().getContentResolver().query(
					WLTwitterDatabaseContract.TWEETS_URI, WLTwitterDatabaseContract.PROJECTION_FULL,
					WLTwitterDatabaseContract.SELECTION_BY_CREATION_DATE, new String[]{tweet.dateCreated}, null);
			if ((null != cursor) && (cursor.moveToFirst())) {
				result = true;
			}
			if ((null != cursor) && (!cursor.isClosed())) {
				cursor.close();
			}
		}
		return result;
	}

	public static void testDatabase(List<Tweet> tweets){
		final WLTwitterDatabaseHelper dbHelper = new WLTwitterDatabaseHelper(WLTwitterApplication.getContext());
		final SQLiteDatabase db = dbHelper.getWritableDatabase();

		// First insert all values in database
		for (Tweet tweet : tweets){
			db.insert(WLTwitterDatabaseContract.TABLE_TWEETS, "", tweetToContentValues(tweet));
			Log.w(Constants.General.LOG_TAG, "Tweet stored");
			Log.w(Constants.General.LOG_TAG, tweet.toString());
			Log.w(Constants.General.LOG_TAG, "----------------------");
		}

		// Now that all values are stored in database, read them and log
		final Cursor cursor = db.query(WLTwitterDatabaseContract.TABLE_TWEETS,
				WLTwitterDatabaseContract.PROJECTION_FULL,
				null, null, null, null, null);
		if (null != cursor){
			while (cursor.moveToNext()){
				final Tweet tweet = tweetFromCursor(cursor);
				Log.i(Constants.General.LOG_TAG, "Stored tweet");
				Log.i(Constants.General.LOG_TAG, tweet.toString());
				Log.i(Constants.General.LOG_TAG, "----------------------");
			}
		}
	}

	public static void testContentProvider(){
		// Test the query
		WLTwitterApplication.getContext().getContentResolver().query(
				WLTwitterDatabaseContract.TWEETS_URI, WLTwitterDatabaseContract.PROJECTION_FULL,
				null, null, null);

		// Test the insert
		WLTwitterApplication.getContext().getContentResolver().insert(
				WLTwitterDatabaseContract.TWEETS_URI, null);

		// Test the update
		WLTwitterApplication.getContext().getContentResolver().update(
				WLTwitterDatabaseContract.TWEETS_URI, null, null, null);

		// Test the delete
		WLTwitterApplication.getContext().getContentResolver().delete(
				WLTwitterDatabaseContract.TWEETS_URI, null, null);
	}
}
