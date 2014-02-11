package com.k.midishapes.interfacing;

public interface DisplayableInstrument {
    public void draw();

    public int getID();

    public void stopNote(int note);

    public void startNote(int note);

    public void stopAll();
}
