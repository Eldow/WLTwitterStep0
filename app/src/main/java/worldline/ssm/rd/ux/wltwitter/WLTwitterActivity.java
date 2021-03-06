package worldline.ssm.rd.ux.wltwitter;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.RingtoneManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;


import java.util.Calendar;

import worldline.ssm.rd.ux.wltwitter.frags.WLTweetFragment;
import worldline.ssm.rd.ux.wltwitter.frags.WLTweetsFragment;
import worldline.ssm.rd.ux.wltwitter.pojo.Tweet;
import worldline.ssm.rd.ux.wltwitter.receiver.WLTweetsReceiver;
import worldline.ssm.rd.ux.wltwitter.service.WLTweetService;
import worldline.ssm.rd.ux.wltwitter.utils.Constants;


public class WLTwitterActivity extends Activity implements WLTweetsFragment.OnArticleSelectedListener{
    private Fragment tweetsFragment;
    private Fragment tweetFragment;
    private PendingIntent mServicePendingIntent;
    private WLTweetsReceiver mReceiver;

    @Override
    protected void onResume() {
        super.onResume();

        Intent intent = new Intent(WLTwitterApplication.getContext(), WLTweetService.class);
        WLTwitterApplication.getContext().startService(intent);

        final Calendar cal = Calendar.getInstance();
        final Intent serviceIntent = new Intent(this, WLTweetService.class);
        mServicePendingIntent = PendingIntent.getService(this, 0, serviceIntent, 0);
        final AlarmManager alarmManager = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), Constants.Twitter.POLLING_DELAY, mServicePendingIntent);

        mReceiver = new WLTweetsReceiver();
        registerReceiver(mReceiver,new IntentFilter(Constants.General.ACTION_NEW_TWEETS));
    }

    @Override
    protected void onPause() {
        super.onPause();
        final AlarmManager alarmManager = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(mServicePendingIntent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        String username;
        if(getIntent().getExtras() != null) { //Sign in
            username = getIntent().getExtras().getString("username");
        } else { //Quick start
            username = WLTwitterApplication.getContext().getSharedPreferences(Constants.Preferences.SHARED_PREFERENCES_FILE_NAME, Context.MODE_PRIVATE).getString("username",null);
        }
        getActionBar().setSubtitle(username);
        tweetsFragment = new WLTweetsFragment();
        FragmentManager fragmentM = getFragmentManager();
        FragmentTransaction transaction = fragmentM.beginTransaction();
        transaction.add(R.id.rootLayout, tweetsFragment);
        transaction.commit();

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_wltwitter_logout, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        // On Logout
        if (id == R.id.actionLogout) {
            //clear username & password
            WLTwitterApplication.getContext().getSharedPreferences(Constants.Preferences.SHARED_PREFERENCES_FILE_NAME, Context.MODE_PRIVATE).edit().remove("username").commit();
            WLTwitterApplication.getContext().getSharedPreferences(Constants.Preferences.SHARED_PREFERENCES_FILE_NAME, Context.MODE_PRIVATE).edit().remove("password").commit();
            //finish this activity
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public static void displayNewTweetsNotification(int nbTweets, boolean vibrate, boolean playSound){
        final Context context = WLTwitterApplication.getContext();
        final Notification.Builder mBuilder = new Notification.Builder(context)
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentTitle(context.getString(R.string.app_name))
                .setContentText(String.format(context.getString(R.string.notification_content), nbTweets))
                .setAutoCancel(true);

        final Intent intent = new Intent(context, WLTwitterLoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        final TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addParentStack(WLTwitterLoginActivity.class);
        stackBuilder.addNextIntent(intent);

        final PendingIntent pIntent = stackBuilder.getPendingIntent(0,PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(pIntent);

        final Notification notification = mBuilder.build();

        if(vibrate){
            notification.defaults =  Notification.DEFAULT_VIBRATE;
        }
        if(playSound){
            notification.sound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        }

        final NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(42,notification);

    }

    @Override
    public void onTweetClicked(Tweet tweet) {
        tweetFragment = WLTweetFragment.newInstance(tweet.user.name, tweet.user.screenName, tweet.text, tweet.user.profileImageUrl);
        FragmentManager fragmentM = getFragmentManager();
        FragmentTransaction transaction = fragmentM.beginTransaction();
        transaction.hide(tweetsFragment);
        transaction.add(R.id.rootLayout,tweetFragment);
        transaction.commit();
    }

    public void onTweetFragmentClicked(){
        FragmentManager fragmentM = getFragmentManager();
        FragmentTransaction transaction = fragmentM.beginTransaction();
        transaction.remove(tweetFragment);
        transaction.show(tweetsFragment);
        transaction.commit();
    }
}
