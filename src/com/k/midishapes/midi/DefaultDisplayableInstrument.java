package com.k.midishapes.midi;

import java.awt.Color;

import com.k.midishapes.interfacing.DisplayableInstrument;

import emergencylanding.k.library.lwjgl.tex.ColorTexture;
import emergencylanding.k.library.lwjgl.tex.ELTexture;

public abstract class DefaultDisplayableInstrument implements
        DisplayableInstrument {
    protected static final int NOTES = 128; // 128 notes
    protected static final ELTexture noteOnStatic = new ColorTexture(Color.RED);
    protected static final ELTexture noteOffStatic = new ColorTexture(
            Color.DARK_GRAY);
    protected ELTexture noteOn = noteOnStatic;
    protected ELTexture noteOff = noteOffStatic;
    protected final int id;

    public DefaultDisplayableInstrument(int idChannel) {
        id = idChannel;
    }

    @Override
    public ELTexture noteOnColor() {
        if (noteOn == null) {
            noteOn = noteOnStatic;
        }
        return noteOn;
    }

    @Override
    public ELTexture noteOffColor() {
        if (noteOff == null) {
            noteOff = noteOffStatic;
        }
        return noteOff;
    }

    public void setNoteOn(ELTexture t) {
        noteOn = t;
    }

    public void setNoteOff(ELTexture t) {
        noteOff = t;
    }

    @Override
    public int getID() {
        return id;
    }
}
