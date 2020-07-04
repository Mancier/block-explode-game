package com.example.blocks_explode;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.shapes.Shape;
import android.sax.Element;

public class GameElement {
    protected CannonView view;
    protected Paint paint = new Paint();
    protected Rect shape;
    protected float velocityY;
    private int soundId;

    public GameElement(CannonView view, int color, int soundId, int x, int y , int width, int lenght, float velocityY){
        this.view = view;
        this.soundId = soundId;
        this.velocityY = velocityY;

        paint.setColor(color);
        shape = new Rect(x, y, x+width, y+lenght);
    }

    public void update(double interval){
        shape.offset(0, (int) (velocityY*interval));

        if (shape.top < 0 && velocityY < 0 || shape.bottom > view.getScreenHeight() && velocityY > 0){
            velocityY *= -1;
        }
    }

    public void draw(Canvas canvas) {
        canvas.drawRect(shape, paint);
    }

    public void playSound() {
        view.playSound(soundId);
    }
}
