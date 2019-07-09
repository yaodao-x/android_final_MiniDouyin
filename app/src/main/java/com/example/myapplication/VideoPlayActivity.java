package com.example.myapplication;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.text.SimpleDateFormat;


public class VideoPlayActivity extends AppCompatActivity implements
        MediaPlayer.OnPreparedListener,
        MediaPlayer.OnCompletionListener,
        MediaPlayer.OnErrorListener,
        MediaPlayer.OnInfoListener, View.OnClickListener,
        MediaPlayer.OnSeekCompleteListener,
        MediaPlayer.OnVideoSizeChangedListener,
        SeekBar.OnSeekBarChangeListener,
        GestureDetector.OnGestureListener {

    private ImageView playOrPauseIv;
    private SurfaceView videoSuf;
    private SurfaceHolder surfaceHolder;
    private MediaPlayer mPlayer;
    private SeekBar mSeekBar;
    private RelativeLayout rootViewRl;
    private LinearLayout controlLl;
    private TextView startTime, endTime;
    private ImageView forwardButton, backwardButton, loveImage;
    private GestureDetector gestureScanner;
    private ObjectAnimator animator;

    public static final int UPDATE_TIME = 0x0001;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case UPDATE_TIME:
                    updateTime();
                    mHandler.sendEmptyMessageDelayed(UPDATE_TIME, 500);
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_videoplay);
        initViews();
        initSurfaceView();
        initPlayer();
        initEvent();
        gestureScanner = new GestureDetector(this);
        gestureScanner.setOnDoubleTapListener(new GestureDetector.OnDoubleTapListener() {
            public boolean onDoubleTap(MotionEvent e) {
                //双击
                Log.e("test", "onDoubleTap");

                animator = ObjectAnimator.ofFloat(loveImage, "alpha", 0, 1f);
                animator.setDuration(2500);
                animator.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                        super.onAnimationStart(animation);
                        loveImage.setVisibility(View.VISIBLE);
                    }

                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        loveImage.setVisibility(View.GONE);
                        loveImage.setVisibility(View.INVISIBLE);
                    }
                });
                animator.start();


                return false;
            }

            public boolean onDoubleTapEvent(MotionEvent e) {
                return false;
            }

            public boolean onSingleTapConfirmed(MotionEvent e) {
                //单击
                play();
                return false;
            }
        });
        Log.e("XJP", "onCreate: ");
    }


    private void initEvent() {
        forwardButton.setOnClickListener(this);
        backwardButton.setOnClickListener(this);
        mSeekBar.setOnSeekBarChangeListener(this);
    }

    private void initSurfaceView() {
        videoSuf = (SurfaceView) findViewById(R.id.surfaceView);
        videoSuf.setZOrderOnTop(false);
        videoSuf.getHolder().setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    private void initPlayer() {
        mPlayer = new MediaPlayer();
        mPlayer.setOnCompletionListener(this);
        mPlayer.setOnErrorListener(this);
        mPlayer.setOnInfoListener(this);
        mPlayer.setOnPreparedListener(this);
        mPlayer.setOnSeekCompleteListener(this);
        mPlayer.setOnVideoSizeChangedListener(this);
        try {
            String video_url = getIntent().getStringExtra("video_url");
//            Toast.makeText(VideoPlayActivity.this, video_url, Toast.LENGTH_SHORT).show();
//            Log.e(TAG, "initPlayer: " + video_url);
            mPlayer.setDataSource(VideoPlayActivity.this, Uri.parse(video_url));
//            mPlayer.setDataSource(getResources().openRawResourceFd(R.raw.socer));
            surfaceHolder = videoSuf.getHolder();
//            surfaceHolder.setFixedSize(videoSuf.getWidth(),videoSuf.getHeight());
            surfaceHolder.addCallback(new SurfaceHolder.Callback() {
                @Override
                public void surfaceCreated(SurfaceHolder holder) {
                    mPlayer.setDisplay(surfaceHolder);
                }

                @Override
                public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

                }

                @Override
                public void surfaceDestroyed(SurfaceHolder holder) {

                }
            });
            mPlayer.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void initViews() {
        playOrPauseIv = (ImageView) findViewById(R.id.playOrPause);
        startTime = (TextView) findViewById(R.id.tv_start_time);
        endTime = (TextView) findViewById(R.id.tv_end_time);
        mSeekBar = (SeekBar) findViewById(R.id.tv_progess);
        rootViewRl = (RelativeLayout) findViewById(R.id.root_rl);
        controlLl = (LinearLayout) findViewById(R.id.control_ll);
        forwardButton = (ImageView) findViewById(R.id.tv_forward);
        backwardButton = (ImageView) findViewById(R.id.tv_backward);
        loveImage = (ImageView) findViewById(R.id.imageView2);
        Log.e("XJP", "initViews: " + R.id.imageView2);
        loveImage.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        SimpleDateFormat currentTime = new SimpleDateFormat("mm:ss");
        SimpleDateFormat lastTime = new SimpleDateFormat("mm:ss");

        startTime.setText(currentTime.format(mp.getCurrentPosition()));
        endTime.setText(lastTime.format(mp.getDuration()));

        mSeekBar.setMax(mp.getDuration());
        mSeekBar.setProgress(mp.getCurrentPosition());
    }

    @Override
    public void onCompletion(MediaPlayer mp) {

    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        return false;
    }

    @Override
    public boolean onInfo(MediaPlayer mp, int what, int extra) {
        return false;
    }

    private void play() {
        if (mPlayer == null) {
            return;
        }
        if (mPlayer.isPlaying()) {
            mPlayer.pause();
            mHandler.removeMessages(UPDATE_TIME);
            playOrPauseIv.setVisibility(View.VISIBLE);
            playOrPauseIv.setImageResource(android.R.drawable.ic_media_play);
        } else {
            mPlayer.start();
            mHandler.sendEmptyMessageDelayed(UPDATE_TIME, 500);
            playOrPauseIv.setVisibility(View.INVISIBLE);
            playOrPauseIv.setImageResource(android.R.drawable.ic_media_pause);
        }
    }

    @Override
    public void onSeekComplete(MediaPlayer mp) {
        //TODO
    }

    @Override
    public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_backward:
                backWard();
                break;
            case R.id.tv_forward:
                forWard();
                break;
        }
    }


    //更新播放时间
    private void updateTime() {
        SimpleDateFormat currentTime = new SimpleDateFormat("mm:ss");
        startTime.setText(currentTime.format(mPlayer.getCurrentPosition()));
        mSeekBar.setProgress(mPlayer.getCurrentPosition());
    }


    //设置快进10秒方法
    private void forWard() {
        if (mPlayer != null) {
            int position = mPlayer.getCurrentPosition();
            mPlayer.seekTo(position + 10000);
        }
    }

    //设置快退10秒的方法
    public void backWard() {
        if (mPlayer != null) {
            int position = mPlayer.getCurrentPosition();
            if (position > 10000) {
                position -= 10000;
            } else {
                position = 0;
            }
            mPlayer.seekTo(position);
        }
    }

    //根据进度条变更视频播放进度
    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (mPlayer != null && fromUser) {
            mPlayer.seekTo(progress);
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("progress", mPlayer.getCurrentPosition());
        Log.e("XJP", "onSaveInstanceState: ");
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState != null) {
            int progress = savedInstanceState.getInt("progress");
            mPlayer.seekTo(progress);
            mSeekBar.setProgress(progress);
        }
        Log.e("XJP", "onRestoreInstanceState: ");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mHandler.removeMessages(UPDATE_TIME);
        mPlayer.release();
        Log.e("XJP", "onDestroy: ");
    }

    public boolean onTouchEvent(MotionEvent me) {
        return gestureScanner.onTouchEvent(me);
    }

    public boolean onDown(MotionEvent e) {
        return true;
    }

    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        return true;
    }

    public void onLongPress(MotionEvent e) {
    }

    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        return true;
    }

    public void onShowPress(MotionEvent e) {
    }

    public boolean onSingleTapUp(MotionEvent e) {
        Log.e("test", "onSingleTapUp");
        return true;
    }

}