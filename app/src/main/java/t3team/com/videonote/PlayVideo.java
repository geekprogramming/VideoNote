package t3team.com.videonote;

import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Point;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.animation.TranslateAnimation;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;
import java.util.Formatter;
import java.util.Locale;
import java.util.Timer;

public class PlayVideo extends AppCompatActivity implements SurfaceHolder.Callback, MediaPlayer.OnPreparedListener{

    private SurfaceView     mSurfaceView = null;
    private View            mLayoutNavigationbar = null;
    private View            mLayoutControlMedia = null;
    private View            mLayoutLight = null;
    private View            mLayoutVoice = null;

    private TextView        mNameSong = null;
    private ImageButton     mBtnCancel = null;
    private ImageButton     mBtnCheck = null;
    private ImageButton     mBtnPen = null;
    private ImageButton     mBtnNode = null;

    private TextView        mCurrentTime = null;
    private TextView        mEndTime = null;
    private SeekBar         mSeekBar = null;
    private ProgressBar     mProgressBarControl = null;

    private ImageButton     mBtnListVideo = null;
    private ImageButton     mBtnBackWard = null;
    private ImageButton     mBtnPause = null;
    private ImageButton     mBtnFfward = null;
    private ImageButton     mBtnFullScreen = null;
    private boolean         mDragging;
    StringBuilder           mFormatBuilder;
    Formatter               mFormatter;
    private boolean         mIsBackwark;
    private boolean         mIsFfwark;

    //video
    private MediaPlayer     mMediaPlayer = null;
    private SurfaceHolder   mSurfaceHolder = null;

    private static final int    sDefaultTimeout = 2000;
    private static final int    FADE_OUT = 1;
    private static final int    SHOW_PROGRESS = 2;
    private static final int    DOWN_CONTROL = 3;

    private static final int    SET_BRIGHT = 4;
    private static final int    HIDE_BRIGHT = 5;

    private static final int    SET_VOLUME = 6;
    private static final int    HIDE_VOLUME = 7;

    private Handler             mHandler = new MessageHandler();
    private boolean             isShowing;
    private boolean             isSetupLight;
    private boolean             isSetupVolume;

    private static final long   TOUCH_DOUBLE_DELAY = 300;
    private boolean             isTouch = false;
    private long                timeTouch = 0;

    public final static int SWIPE_UP    = 1;
    public final static int SWIPE_DOWN  = 2;
    public final static int SWIPE_LEFT  = 3;
    public final static int SWIPE_RIGHT = 4;

    public final static int SCROLL_LEFT    = 0x11;
    public final static int SCROLL_RIGHT  = 0x22;
    public final static int DOUBLE_CLICK  = 0x33;
    public final static int SINGLE_CLICK  = 0x44;

    private int mWidthScreen;
    private int mHeightScreen;
    private DisplayMetrics mDisplay;

    long TIME_DOUBLE_CLICK = 250;
    long time_start = 0;
    long time_end = 0;
    float x_start = 0;
    float y_start = 0;
    float x_move = 0;
    float y_move = 0;
    boolean isLeft = false;
    private boolean             isScrolling = false;
    private boolean             isDoubleClick = false;
    private boolean             canSignleClick = false;
    Timer timer;

    int currentPossion;
    int currentVolume;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_video);
        mDisplay = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(mDisplay);
        mWidthScreen = mDisplay.widthPixels;
        mHeightScreen = mDisplay.heightPixels;

        Log.d("DHT","onCreate");
        initView();
        File video = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/video");
        if(video.exists()) Log.d("DHT", "exists: " + video.getAbsolutePath());
        else Log.d("DHT","not exist");
        //videoView.setVideoPath();
        File [] listFile = video.listFiles();

//        int a = listFile.length;
//        for(int i = 0; i < listFile.length; i++){
//            Log.d("DHT", listFile[i].getAbsolutePath());
//        }
        mSurfaceHolder = mSurfaceView.getHolder();
        mSurfaceHolder.addCallback(this);
        mMediaPlayer = new MediaPlayer();

        Uri uriPlay = Uri.fromFile(listFile[0]);
        try {
            mMediaPlayer.setDataSource(this, uriPlay);
            mMediaPlayer.setOnPreparedListener(this);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        sizeSurfaceView();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //Log.d("DHT",event.toString());
        //if(detector.onDoubleTap(event)) Log.d("DHT","double");
//        detector.onTouchEvent(event);
        DetectTouch(event);
        return super.onTouchEvent(event);
    }

    protected void DetectTouch(MotionEvent event){
        if(event .getAction() == MotionEvent.ACTION_DOWN){
            time_end = System.currentTimeMillis();
            double a = (int)Math.abs(event.getX() - x_start);

            Log.d("DHT","duration time: " + (time_end - time_start));
            if((time_end - time_start) <= TIME_DOUBLE_CLICK && a < 25 ){
                isDoubleClick = true;
                implementTouch(DOUBLE_CLICK,0);
            }
            x_start = event.getX();
            y_start = event.getY();

            if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE)
                if(x_start < mWidthScreen/2) isLeft = true;
                else isLeft = false;
            else if(x_start < mHeightScreen/2) isLeft = true;
            else isLeft = false;

        }
        if(event.getAction() == MotionEvent.ACTION_UP){
            time_start = System.currentTimeMillis();
            //waite 100 ms to check double click
            if(isDoubleClick){
                time_end = 0;
                time_start = 0;
            }
            if(!isScrolling && !isDoubleClick) implementTouch(SINGLE_CLICK,0);
            isScrolling = false;
            isDoubleClick = false;
            isSetupLight = false;
            isSetupVolume = false;
        }

        if(event.getAction() == MotionEvent.ACTION_MOVE){
            y_move = event.getY();
            if(Math.abs(y_start - y_move) > 20){
                if(!isScrolling) y_start = y_move;
                isScrolling = true;
            }

            if(isScrolling){
                int distance = (int)(y_move - y_start);
                if(isLeft) implementTouch(SCROLL_LEFT,distance);
                else implementTouch(SCROLL_RIGHT,distance);
            }
        }
    }



    private void implementTouch(int event,int distance){
        switch (event){
            case DOUBLE_CLICK :
                Log.d("DHT","double click");
                doPauseResume();
                show(DOUBLE_CLICK,sDefaultTimeout);
                break;
            case SCROLL_LEFT:
                show(SCROLL_LEFT,sDefaultTimeout);
                break;
            case SCROLL_RIGHT:
                show(SCROLL_RIGHT,sDefaultTimeout);
                break;

            case SINGLE_CLICK:
                show(DOUBLE_CLICK,sDefaultTimeout);
                break;
            default:
                Log.d("DHT","not event");
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d("DHT", "onResume");
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mWidthScreen = mDisplay.widthPixels;
        mHeightScreen = mDisplay.heightPixels;
        Log.d("DHT","onconfigation");
        initView();

        sizeSurfaceView();

    }

    private void sizeSurfaceView(){
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);

        int width = size.x;
        int height = size.y;

        if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT){
            height = 5*height/9;
        }

        if(mSurfaceHolder != null){
            mSurfaceHolder.setFixedSize(width,height);
        }
    }

    private void initView(){
        mSurfaceView = (SurfaceView) findViewById(R.id.videoSurface);
        mLayoutNavigationbar = findViewById(R.id.layout_actionbar);
        mLayoutControlMedia = findViewById(R.id.layout_control_media);
        mLayoutLight = findViewById(R.id.layout_light);
        mLayoutVoice = findViewById(R.id.layout_voice);

        mNameSong   = (TextView) findViewById(R.id.tv_name_song);
        mBtnCancel  = (ImageButton) findViewById(R.id.btn_cancel);
        mBtnCheck   = (ImageButton) findViewById(R.id.btn_check);
        mBtnPen     = (ImageButton) findViewById(R.id.btn_pen);
        mBtnNode    = (ImageButton) findViewById(R.id.btn_note);

        mCurrentTime = (TextView) findViewById(R.id.time_current);
        mEndTime = (TextView) findViewById(R.id.time_end);
        mProgressBarControl = (SeekBar) findViewById(R.id.mediacontroller_progress);


        mBtnListVideo = (ImageButton) findViewById(R.id.list_video);
        mBtnBackWard = (ImageButton) findViewById(R.id.rew);
        mBtnPause = (ImageButton) findViewById(R.id.pause);
        mBtnFfward = (ImageButton) findViewById(R.id.ffwd);
        mBtnFullScreen = (ImageButton) findViewById(R.id.fullscreen);
        hideAll();

        mFormatBuilder = new StringBuilder();
        mFormatter = new Formatter(mFormatBuilder, Locale.getDefault());

        mBtnPen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent drawNote = new Intent(getBaseContext(),MainActivity.class);
                startActivity(drawNote);
            }
        });

        mBtnBackWard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doBackward();
            }
        });

        mBtnFfward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doFfward();
            }
        });

        mBtnPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doPauseResume();
            }
        });

        if(mProgressBarControl != null){
            if(mProgressBarControl instanceof SeekBar){
                mSeekBar = (SeekBar) mProgressBarControl;
                mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                    public void onStartTrackingTouch(SeekBar bar) {
                        Log.d("DHT","onSeekListener start");
                        show(SINGLE_CLICK,sDefaultTimeout);

                        mDragging = true;

                        // By removing these pending progress messages we make sure
                        // that a) we won't update the progress while the user adjusts
                        // the seekbar and b) once the user is done dragging the thumb
                        // we will post one of these messages to the queue again and
                        // this ensures that there will be exactly one message queued up.
                        mHandler.removeMessages(SHOW_PROGRESS);
                    }

                    public void onProgressChanged(SeekBar bar, int progress, boolean fromuser) {
                        // Log.d("DHT","onSeekListener changed" + mDragging + mShowing);
                        if (mMediaPlayer == null) {
                            return;
                        }

                        if (!fromuser) {
                            // We're not interested in programmatically generated changes to
                            // the progress bar's position.
                            return;
                        }

                        long duration = mMediaPlayer.getDuration();
                        long newposition = (duration * progress) / 1000L;
                        mMediaPlayer.seekTo( (int) newposition);
                        if (mCurrentTime != null)
                            mCurrentTime.setText(stringForTime( (int) newposition));
                    }

                    public void onStopTrackingTouch(SeekBar bar) {
                        Log.d("DHT","onSeekListener stop");
                        mDragging = false;
                        setProgress();
                        updatePausePlay();
                        show(SINGLE_CLICK,sDefaultTimeout);

                        // Ensure that progress is properly updated in the future,
                        // the call to show() does not guarantee this because it is a
                        // no-op if we are already showing.
                        mHandler.sendEmptyMessage(SHOW_PROGRESS);
                    }
                });
            }
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        mMediaPlayer.setDisplay(mSurfaceHolder);
        mMediaPlayer.prepareAsync();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        mMediaPlayer.start();
    }

    public void show(int cases,int timeout) {

        // cause the progress bar to be updated even if mShowing
        // was already true.  This happens, for example, if we're
        // paused with the progress bar showing the user hits play.
        //mHandler.sendEmptyMessage(SHOW_PROGRESS);
        switch (cases) {
            case DOUBLE_CLICK:
                // break;
            case SINGLE_CLICK:
                Log.d("DHT","show");
                if (!isShowing) {
                    hideAll();
                    mLayoutControlMedia.setVisibility(View.VISIBLE);
                    mLayoutNavigationbar.setVisibility(View.VISIBLE);
                    TranslateAnimation translateAnimation = new TranslateAnimation(0, 0, -200, 0);
                    translateAnimation.setDuration(500);
                    mLayoutNavigationbar.startAnimation(translateAnimation);

                    TranslateAnimation translateAnimation1 = new TranslateAnimation(0, 0, 200, 0);
                    translateAnimation1.setDuration(500);
                    mLayoutControlMedia.startAnimation(translateAnimation1);
                    isShowing = true;
                }
                setProgress();
                updatePausePlay();

                Message msg = mHandler.obtainMessage(DOWN_CONTROL);
                if (timeout != 0) {
                    mHandler.removeMessages(FADE_OUT);
                    mHandler.removeMessages(DOWN_CONTROL);
                    mHandler.sendMessageDelayed(msg, timeout);
                }

                break;

            case SCROLL_LEFT:
                if (!isSetupLight) {
                    Log.d("DHT","start show left");
                    hideAll();
                    isSetupVolume = false;
                    isShowing = false;
                    mLayoutLight.setVisibility(View.VISIBLE);
//                    mLayoutNavigationbar.setVisibility(View.VISIBLE);
//                    TranslateAnimation translateAnimation = new TranslateAnimation(0, 0, -200, 0);
//                    translateAnimation.setDuration(500);
//                    mLayoutNavigationbar.startAnimation(translateAnimation);

//                    TranslateAnimation translateAnimation1 = new TranslateAnimation(0, 0, 200, 0);
//                    translateAnimation1.setDuration(500);
//                    mLayoutControlMedia.startAnimation(translateAnimation1);
                    isSetupLight = true;
                }

                msg = mHandler.obtainMessage(FADE_OUT);
                if (timeout != 0) {
                    mHandler.removeMessages(FADE_OUT);
                    mHandler.removeMessages(DOWN_CONTROL);
                    mHandler.sendMessageDelayed(msg, timeout);
                }
                break;

            case SCROLL_RIGHT:
                Log.d("DHT","show right");
                if (!isSetupVolume) {
                    Log.d("DHT","start show right");
                    hideAll();
                    isSetupLight = false;
                    isShowing = false;
                    mLayoutVoice.setVisibility(View.VISIBLE);
//                    mLayoutNavigationbar.setVisibility(View.VISIBLE);
//                    TranslateAnimation translateAnimation = new TranslateAnimation(0, 0, -200, 0);
//                    translateAnimation.setDuration(500);
//                    mLayoutNavigationbar.startAnimation(translateAnimation);
//
//                    TranslateAnimation translateAnimation1 = new TranslateAnimation(0, 0, 200, 0);
//                    translateAnimation1.setDuration(500);
//                    mLayoutControlMedia.startAnimation(translateAnimation1);
                    isSetupVolume = true;
                }

                msg = mHandler.obtainMessage(FADE_OUT);
                if (timeout != 0) {
                    mHandler.removeMessages(FADE_OUT);
                    mHandler.removeMessages(DOWN_CONTROL);
                    mHandler.sendMessageDelayed(msg, timeout);
                }
                break;
        }


    }

//    public void hide(){
//        mLayoutControlMedia.setVisibility(View.GONE);
//        mLayoutNavigationbar.setVisibility(View.GONE);
//    }

    private void hideAll(){
        mLayoutControlMedia.setVisibility(View.GONE);
        mLayoutNavigationbar.setVisibility(View.GONE);
        mLayoutLight.setVisibility(View.GONE);
        mLayoutVoice.setVisibility(View.GONE);
    }

    private void downControl(){
        if(isShowing) {
            mLayoutControlMedia.setVisibility(View.VISIBLE);
            mLayoutNavigationbar.setVisibility(View.VISIBLE);
            TranslateAnimation translateAnimation = new TranslateAnimation(0, 0, 0, -200);
            translateAnimation.setDuration(500);
            mLayoutNavigationbar.startAnimation(translateAnimation);

            TranslateAnimation translateAnimation1 = new TranslateAnimation(0, 0, 0, 200);
            translateAnimation1.setDuration(500);
            mLayoutControlMedia.startAnimation(translateAnimation1);
            isShowing = false;
        }
    }

    private class MessageHandler extends Handler {

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case FADE_OUT:
                    hideAll();
                    isShowing = false;
                    isSetupLight = false;
                    isSetupVolume = false;
                    break;

                case DOWN_CONTROL:
                    downControl();
                    Message msg1 = obtainMessage(FADE_OUT);
                    removeMessages(FADE_OUT);
                    removeMessages(DOWN_CONTROL);
                    sendMessageDelayed(msg1, 500);
                case SHOW_PROGRESS:

                    break;

            }
        }
    }

    public void updatePausePlay() {
        if (mBtnPause == null || mMediaPlayer == null) {
            return;
        }

        if (mMediaPlayer.isPlaying()) {
            mBtnPause.setImageResource(R.drawable.ic_media_pause);
        } else {
            mBtnPause.setImageResource(R.drawable.ic_media_play);
        }
    }

    private int setProgress() {
        if (mMediaPlayer == null || mDragging) {
            return 0;
        }

        int position = mMediaPlayer.getCurrentPosition();
        int duration = mMediaPlayer.getDuration();
        if (mProgressBarControl != null) {
            if (duration > 0) {
                // use long to avoid overflow
                long pos = 1000L * position / duration;
                mProgressBarControl.setProgress((int) pos);
            }
            int percent = 0;
            mProgressBarControl.setSecondaryProgress(percent * 10);
        }

        if (mEndTime != null)
            mEndTime.setText(stringForTime(duration));
        if (mCurrentTime != null)
            mCurrentTime.setText(stringForTime(position));

        return position;
    }

    private String stringForTime(int timeMs) {
        int totalSeconds = timeMs / 1000;

        int seconds = totalSeconds % 60;
        int minutes = (totalSeconds / 60) % 60;
        int hours   = totalSeconds / 3600;

        mFormatBuilder.setLength(0);
        if (hours > 0) {
            return mFormatter.format("%d:%02d:%02d", hours, minutes, seconds).toString();
        } else {
            return mFormatter.format("%02d:%02d", minutes, seconds).toString();
        }
    }

    private void doPauseResume() {
        mIsBackwark = false;
        mIsFfwark = false;
        if (mMediaPlayer == null) {
            return;
        }

        if (mMediaPlayer.isPlaying()) {
            mMediaPlayer.pause();
        } else {
            mMediaPlayer.start();
        }
        updatePausePlay();
    }

    private void doBackward(){
        if(mIsFfwark) mIsFfwark = false;
        else {
            mIsBackwark = !mIsBackwark;

            doFfward();
            while (mIsBackwark) {
                if (mMediaPlayer == null) {
                    return;
                }

                int pos = mMediaPlayer.getCurrentPosition();
                pos -= 5000; // milliseconds
                mMediaPlayer.seekTo(pos);
                setProgress();
            }
        }
        show(SINGLE_CLICK,sDefaultTimeout);
    }

    private void doFfward() {
        if (mIsBackwark) mIsBackwark = false;
        else {
            mIsFfwark = !mIsFfwark;
            doBackward();
            while (mIsFfwark) {
                if (mMediaPlayer == null) {
                    return;
                }

                int pos = mMediaPlayer.getCurrentPosition();
                pos += 15000; // milliseconds
                mMediaPlayer.seekTo(pos);
                setProgress();
            }

            show(SINGLE_CLICK, sDefaultTimeout);
        }
    }

}
