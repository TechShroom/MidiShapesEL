package com.k.midishapes.interfacing;

import com.k.midishapes.midi.DefaultDisplayableInstrument;

import emergencylanding.k.library.lwjgl.tex.ColorTexture;
import emergencylanding.k.library.lwjgl.tex.ELTexture;

/**
 * The interface that represents the drawing backend of the MidiPlayer. It is
 * preferable to extend {@link DefaultDisplayableInstrument}, as it contains
 * some implementations already.
 * 
 * @author Kenzie Togami
 */
public interface DisplayableInstrument<T extends DisplayableInstrument<T>> {

    /**
     * The constant representing the number of notes a MIDI supports
     */
    public static final int NOTES = 128;

    /**
     * Draws this instrument, although it is possible to react to certain events
     * here as well.
     */
    public void draw();

    /**
     * Gets the ID used for mapping this instrument.
     * 
     * @return the ID of this instrument
     */
    public int getID();

    /**
     * This should stop the specified note. It is allowed, although not common,
     * to ignore this request. This does not prevent the MIDI note from
     * stopping.
     * 
     * @param note
     *            - the note to stop
     */
    public void stopNote(int note);

    /**
     * This should play the specified note. It is allowed, although not common,
     * to ignore this request. This does not prevent the MIDI note from playing.
     * 
     * @param note
     *            - the note to play
     */
    public void playNote(int note);

    /**
     * This should stop all the notes on this instrument. It is allowed,
     * although not common, to ignore this request. This does not prevent the
     * MIDI notes from stopping.
     */
    public void stopAll();

    /**
     * Gets the color of the "on" note.
     * 
     * @return an {@link ELTexture} representing the color of an 'on' note. It
     *         is advisable to use a {@link ColorTexture}, but this is not
     *         required.
     */
    public ELTexture noteOnColor();

    /**
     * Gets the color of the "off" note.
     * 
     * @return an {@link ELTexture} representing the color of an 'off' note. It
     *         is advisable to use a {@link ColorTexture}, but this is not
     *         required.
     */
    public ELTexture noteOffColor();
}
