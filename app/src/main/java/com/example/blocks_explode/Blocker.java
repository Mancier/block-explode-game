package com.example.blocks_explode;

public class Blocker extends GameElement {
    private int missPenalty;

    public Blocker(CannonView view, int color, int missPenalty, int x, int y, int width, int lenght, float velocityY) {
        super(view, color, CannonView.BLOCKER_SOUND_ID, x, y, width, lenght, velocityY);
        this.missPenalty = missPenalty;
    }

    public int getMissPenalty(){
        return missPenalty;
    }
}
