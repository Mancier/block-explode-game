package com.example.blocks_explode;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.media.AudioAttributes;
import android.media.SoundPool;
import android.util.AttributeSet;
import android.util.SparseIntArray;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.Random;

public class CannonView extends SurfaceView implements SurfaceHolder.Callback {
    /**
     * Points
     */
    public static final int MISS_PENALTY = 2;
    public static final int HIT_REWARD = 3;

    /**
     * Cannon
     */
    public static final double CANNON_BASE_RADIUS_PERCENT = 3.0 / 40;
    public static final double CANNON_BARREL_WIDTH_PERCENT = 3.0 / 40;
    public static final double CANNON_BARREL_LENGTH_PERCENT = 1.0 / 10;
    public static final double CANNONBALL_RADIUS_PERCENT = 3.0 / 80;
    public static final double CANNONBALL_SPEED_PERCENT = 3.0 / 2;

    /**
     * Targets
     */
    public static final double TARGET_WIDTH_PERCENT = 1.0 / 40;
    public static final double TARGET_LENGTH_PERCENT = 3.0 / 20;
    public static final double TARGET_FIRST_X_PERCENT = 3.0 / 5;
    public static final double TARGET_SPACING_PERCENT = 1.0 / 60;
    public static final double TARGET_PIECES = 9;
    public static final double TARGET_MIN_SPEED_PERCENT = 3.0 / 4;
    public static final double TARGET_MAX_SPEED_PERCENT = 6.0 / 4;
    public static final int TARGET_SOUND_ID = 0;
    public static final int CANNON_SOUND_ID = 1;
    public static final int BLOCKER_SOUND_ID = 2;

    /**
     * Blocker
     */
    public static final double BLOCKER_WIDTH_PERCENT = 1.0 / 40;
    public static final double BLOCKER_LENGTH_PERCENT = 1.0 / 4;
    public static final double BLOCKER_X_PERCENT = 1.0 / 2;
    public static final double BLOCKER_SPEED_PERCENT = 1.0;

    /**
     * Text
     */
    public static final double TEXT_SIZE_PERCENT = 1.0 / 18;

    /**
     * Variables
     */
    private int screenWidth;
    private int screenHeight;
    private boolean gameOver;
    private double timeLeft;
    private int shotsFired;
    private double totalElapsedTime;
    private boolean dialogIsDisplayed = false;
    private ArrayList<Target> targets;
    private Cannon cannon;
    private Blocker blocker;
    private SoundPool soundPool;
    private SparseIntArray soundMap;
    private CannonThread cannonThread;
    private Activity activity;
    private Paint textPaint;
    private Paint backgroundPaint;

    public CannonView(Context context, AttributeSet attrs) {
        super(context, attrs);
        activity = (Activity) context;
        getHolder().addCallback(this);
        AudioAttributes.Builder attrBuilder = new AudioAttributes.Builder();
        attrBuilder.setUsage(AudioAttributes.USAGE_GAME);
        SoundPool.Builder builder = new SoundPool.Builder();
        builder.setMaxStreams(1);
        builder.setAudioAttributes(attrBuilder.build());
        soundPool = builder.build();
        soundMap = new SparseIntArray(3);
        soundMap.put(TARGET_SOUND_ID, soundPool.load(context, R.raw.target_hit, 1));
        soundMap.put(CANNON_SOUND_ID,soundPool.load(context, R.raw.cannon_fire, 1));
        soundMap.put(BLOCKER_SOUND_ID,soundPool.load(context, R.raw.blocker_hit, 1));
        textPaint = new Paint();
        backgroundPaint = new Paint();
        backgroundPaint.setColor(Color.WHITE);
    }

    @Override
    protected void onSizeChanged(int width, int height, int oldWidth, int oldHeight){
        super.onSizeChanged(width, height, oldWidth, oldHeight);

        textPaint.setTextAlign(Paint.Align.CENTER);  //textPaint.setTextAlign é um ENUM e está sendo passado um int para ele. Isso estava certo?
        textPaint.setAntiAlias(true);
    }

    @Override
    public void surfaceCreated(@NonNull SurfaceHolder surfaceHolder) {

    }

    @Override
    public void surfaceChanged(@NonNull SurfaceHolder surfaceHolder, int i, int i1, int i2) {

    }

    @Override
    public void surfaceDestroyed(@NonNull SurfaceHolder surfaceHolder) {

    }

    public int getScreenWidth(){
        return screenWidth;
    }

    public int getScreenHeight(){
        return  screenHeight;
    }

    public void playSound(int soundId){
        soundPool.play(soundMap.get(soundId),1, 1, 1, 0, 1f);
    }

    public void newGame(){
        cannon = new Cannon(this,
                (int) (CANNON_BASE_RADIUS_PERCENT * screenHeight),
                (int) (CANNON_BARREL_LENGTH_PERCENT * screenWidth),
                (int) (CANNON_BARREL_WIDTH_PERCENT * screenHeight));

        Random random = new Random();
        targets = new ArrayList<>();
        int targetX = (int)(TARGET_FIRST_X_PERCENT * screenWidth);
        int targetY = (int)((0.5 - TARGET_LENGTH_PERCENT / 2) * screenHeight);
        double velocity = screenHeight * (random.nextDouble() * (TARGET_MAX_SPEED_PERCENT - TARGET_MIN_SPEED_PERCENT) + TARGET_MIN_SPEED_PERCENT);

        for (int n = 0; n < TARGET_PIECES; n++){
            int color = ContextCompat.getColor(
                    getContext(),
                    (n % 2 == 0) ?
                            R.color.dark :
                            R.color.light
            );
        }

        velocity *=-1;
    }
}
