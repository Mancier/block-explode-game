package com.example.blocks_explode;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.media.AudioAttributes;
import android.media.SoundPool;
import android.os.Build;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseIntArray;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentActivity;

import java.util.ArrayList;
import java.util.Random;

import static android.content.ContentValues.TAG;

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
    /**
     * API 28 and newer has deprecated getFragmentManager()
     * Although if ur APIs is lower u should use
     *
     * private Activity activity;
     *
     * :256
     * Acitivity.getFragmentManager()
     */
    private FragmentActivity activity;
    private Paint textPaint;
    private Paint backgroundPaint;

    public CannonView(Context context, AttributeSet attrs) {
        super(context, attrs);
        activity = (FragmentActivity) context;
        /**
         * SurfaceHolder.Callback()
         */
        getHolder().addCallback(this);

        /**
         * Setting up audios attributes
         */
        AudioAttributes.Builder attrBuilder = new AudioAttributes.Builder();
        attrBuilder.setUsage(AudioAttributes.USAGE_GAME);

        SoundPool.Builder builder = new SoundPool.Builder();
        builder.setMaxStreams(1);
        builder.setAudioAttributes(attrBuilder.build());
        soundPool = builder.build();
        soundMap = new SparseIntArray(3);

        /**
         * TODO: Make a correction in raw audio files
         */
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

        screenHeight = height;
        screenWidth = width;

        textPaint.setTextAlign(Paint.Align.CENTER);  //textPaint.setTextAlign é um ENUM e está sendo passado um int para ele. Isso estava certo?
        textPaint.setAntiAlias(true);
    }

    @Override
    public void surfaceCreated(@NonNull SurfaceHolder surfaceHolder) {
        if(!dialogIsDisplayed) {
            newGame();

            cannonThread = new CannonThread(surfaceHolder);
            cannonThread.setRunning(true);
            cannonThread.start();
        }
    }

    @Override
    public void surfaceChanged(@NonNull SurfaceHolder surfaceHolder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(@NonNull SurfaceHolder surfaceHolder) {
        boolean retry = true;
        cannonThread.setRunning(false);

        while (retry){
            try {
                cannonThread.join();
                retry = false;
            } catch (InterruptedException e){
                Log.e(TAG, "Thread interrupted", e);
            }
        }
    }

    @Override
    public boolean onTouchEvent (MotionEvent e){
        int action = e.getAction();

        if(action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_MOVE) {
            alignAndFireCannonball(e);
        }

        return true;
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

        for (int n = 0; n < TARGET_PIECES; n++){
            double velocity = screenHeight * (random.nextDouble() * (TARGET_MAX_SPEED_PERCENT - TARGET_MIN_SPEED_PERCENT) + TARGET_MIN_SPEED_PERCENT);
            int color = ContextCompat.getColor(
                    getContext(),
                    (n % 2 == 0) ?
                            R.color.dark :
                            R.color.light
            );
            velocity *=-1;

            targets.add(new Target(this, color, HIT_REWARD, targetX, targetY,
                    (int) (TARGET_WIDTH_PERCENT * screenWidth),
                    (int) (TARGET_LENGTH_PERCENT * screenHeight),
                    (int) velocity)
            );

            targetX += (TARGET_WIDTH_PERCENT + TARGET_SPACING_PERCENT) * screenWidth;
        }

        blocker = new Blocker(this, Color.BLACK, MISS_PENALTY,
            (int) BLOCKER_X_PERCENT * screenWidth,
            (int) ((.5 - BLOCKER_LENGTH_PERCENT/2) * screenHeight),
            (int) (BLOCKER_WIDTH_PERCENT * screenWidth),
            (int) (BLOCKER_LENGTH_PERCENT * screenHeight),
            (float) (BLOCKER_SPEED_PERCENT * screenHeight)
        );

        timeLeft = 10;
        shotsFired = 0;
        totalElapsedTime = 0.0;

        if(gameOver){
            gameOver = false;
            cannonThread = new CannonThread(getHolder());
            cannonThread.start();
        }

        hideSystemBars();
    }

    private void updatePositions(double elapsedTimeMs) {
        double interval = elapsedTimeMs / 1000.0;

        if(cannon.getCannonball() != null){
            cannon.getCannonball().update(interval);
            blocker.update(interval);

            for (GameElement target : targets) target.update(interval);

            timeLeft -= interval;

            if (timeLeft <= 0){
                timeLeft = 0.0;
                gameOver = true;
                cannonThread.setRunning(false);
                showGameOverDialog(R.string.loser);
            }

            if (targets.isEmpty()){
                cannonThread.setRunning(false);
                showGameOverDialog(R.string.winner);
                gameOver = true;
            }
        }
    }

    public void alignAndFireCannonball(MotionEvent motionEvent){
        Point touchPoint = new Point(
            (int) motionEvent.getX(),
            (int) motionEvent.getY()
        );

        double centerMinusY = screenHeight / 2 - touchPoint.y;
        double angle = 0;

        angle = Math.atan2(touchPoint.x, centerMinusY);

        cannon.align(angle);

        if (cannon.getCannonball() == null || !cannon.getCannonball().isOnScreen()){
            cannon.fireCannonball();
            ++shotsFired;
        }
    }

    private void showGameOverDialog(final int messageId){
        final DialogFragment gameResult = new DialogFragment(){
            public Dialog onCreateDialog(Bundle bundle){
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

                builder.setTitle(getResources().getString(messageId));

                builder.setMessage(getResources().getString(R.string.formated_result, shotsFired, totalElapsedTime));

                builder.setPositiveButton(R.string.reset_game, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogIsDisplayed = false;
                        newGame();
                    }
                });

                return builder.create();
            }
        };

        activity.runOnUiThread(
            new Runnable() {
                @Override
                public void run() {
                    showSystemBars();

                    dialogIsDisplayed = true;
                    gameResult.setCancelable(true);
                    gameResult.show(activity.getSupportFragmentManager(), "results");
                }
            }
        );
    }


    @SuppressLint("StringFormatInvalid")
    public void drawGameElements(Canvas canvas){
        canvas.drawRect(0, 0, canvas.getWidth(), canvas.getHeight(), backgroundPaint);
        canvas.drawText(getResources().getString(R.string.time_remaining_formatted, timeLeft), 50, 100, textPaint);

        cannon.draw(canvas);

        if(cannon.getCannonball() != null && cannon.getCannonball().isOnScreen()) cannon.getCannonball();

        blocker.draw(canvas);

        for (GameElement target : targets) target.draw(canvas);
    }

    public void testForCollision() {
        if (cannon.getCannonball() != null && cannon.getCannonball().isOnScreen()){
            for (int n = 0; n < targets.size(); n++){
                if(cannon.getCannonball().collideWith(targets.get(n))){
                    timeLeft += targets.get(n).getHitReward();
                    cannon.removeCannonball();
                    targets.remove(n);
                    --n;

                    break;
                }
            }
        } else {
            cannon.removeCannonball();
        }

        if(cannon.getCannonball() != null && cannon.getCannonball().collideWith(blocker)){
            blocker.playSound();
            cannon.getCannonball().reverseVelocityX();

            timeLeft -= blocker.getMissPenalty();
        }
    }

    public void stopGame() {
        if (cannonThread != null) cannonThread.setRunning(false);
    }

    public void releaseResources() {
        soundPool.release();
        soundPool = null;
    }

    private void hideSystemBars() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
            setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                    View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                    View.SYSTEM_UI_FLAG_FULLSCREEN |
                    View.SYSTEM_UI_FLAG_IMMERSIVE
            );
    }

    private void showSystemBars() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
            setSystemUiVisibility( View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            );
    }
}
