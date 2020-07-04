package com.example.blocks_explode;

import android.graphics.Canvas;
import android.view.SurfaceHolder;

public class CannonThread extends Thread{
    private SurfaceHolder surfaceHolder;
    private long totalElapsedTime = 0;
    private boolean isThreadsRunning = false;

    public CannonThread(SurfaceHolder holder) {
        surfaceHolder = holder;
        setName("CannonThread");
    }

    public void setRunning(boolean running) {
        isThreadsRunning = running;
    }

    @Override
    public void run() {
        Canvas canvas = null;
        long previousFrameTime = System.currentTimeMillis();
        
        while (isThreadsRunning){
            try {
                canvas = surfaceHolder.lockCanvas(null);

                /**,
                 * Something is wrong, I can fell.
                 * Bimo, is a trap
                 * TODO: See how do u import and use this functions without a constructor and abstraction
                 */

                synchronized (surfaceHolder){
                    long currentTime = System.currentTimeMillis();
                    double elapsedTimeMS = currentTime - previousFrameTime;
                    totalElapsedTime += elapsedTimeMS / 1000.0;
                    updatePositions(elapsedTimeMS);
                    testForCollitions();
                    drawGameElements(canvas);
                    previousFrameTime = currentTime;
                }
            } finally {
                if (canvas != null) surfaceHolder.unlockCanvasAndPost(canvas);
            }
        }
    }



}
