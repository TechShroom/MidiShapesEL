package com.k.midishapes.midi;

import java.awt.Color;

import com.k.midishapes.interfacing.DisplayableInstrument;

import emergencylanding.k.library.lwjgl.tex.ColorTexture;
import emergencylanding.k.library.lwjgl.tex.ELTexture;

public abstract class DefaultDisplayableInstrument<T extends DefaultDisplayableInstrument<T>>
        implements DisplayableInstrument<T> {
    static void init() {
        
    }
    /**
     * The constant representing the number of notes a MIDI supports
     */
    protected static final int NOTES = 128; // 128 notes
    /**
     * A default 'on' texture
     */
    protected static final ELTexture noteOnStatic = new ColorTexture(Color.RED);
    /**
     * A default 'off' texture
     */
    protected static final ELTexture noteOffStatic = new ColorTexture(
            Color.DARK_GRAY);
    /**
     * The 'on' note color
     */
    protected ELTexture noteOn = noteOnStatic;
    /**
     * The 'off' note color
     */
    protected ELTexture noteOff = noteOffStatic;
    /**
     * The id of this instrument
     */
    protected final int id;

    /**
     * Sets the id for this instrument
     * 
     * @param idChannel
     *            - the id to use. Useful for positioning.
     */
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

    /**
     * Sets the note on color.
     * 
     * @param t
     *            - the new {@link ELTexture} to use as 'on'
     */
    public void setNoteOn(ELTexture t) {
        noteOn = t;
    }

    /**
     * Sets the note off color.
     * 
     * @param t
     *            - the new {@link ELTexture} to use as 'off'
     */
    public void setNoteOff(ELTexture t) {
        noteOff = t;
    }

    @Override
    public int getID() {
        return id;
    }
}
