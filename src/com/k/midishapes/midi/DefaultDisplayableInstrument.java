package com.k.midishapes.midi;

import java.awt.Color;
import java.util.Random;

import com.k.midishapes.interfacing.DisplayableInstrument;

import emergencylanding.k.library.lwjgl.tex.ColorTexture;
import emergencylanding.k.library.lwjgl.tex.ELTexture;

public abstract class DefaultDisplayableInstrument<T extends DefaultDisplayableInstrument<T>>
        implements DisplayableInstrument<T> {

    static void init() {
        // thread safe dealios
    }

    private static final Random COLOR_RAND = new Random();

    /**
     * A default 'on' texture
     */
    protected static final ELTexture noteOnStatic = new ColorTexture(Color.RED);
    /**
     * A default 'off' texture
     */
    protected static final ELTexture noteOffStatic =
            new ColorTexture(Color.DARK_GRAY);
    /**
     * The 'on' note color
     */
    protected ELTexture noteOn =
            new ColorTexture(new Color(COLOR_RAND.nextInt() & 0xFF_FF_FF));
    /**
     * The 'off' note color
     */
    protected ELTexture noteOff = new ColorTexture(
            ColorUtil.complementary(((ColorTexture) noteOn).getRawColor()));
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
