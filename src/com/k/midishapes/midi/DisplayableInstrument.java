package com.k.midishapes.midi;

import java.awt.Color;
import java.awt.Rectangle;
import java.util.Random;

import org.lwjgl.opengl.Display;

import emergencylanding.k.library.lwjgl.Shapes;
import emergencylanding.k.library.lwjgl.control.MouseHelp;
import emergencylanding.k.library.lwjgl.render.VBAO;
import emergencylanding.k.library.lwjgl.render.VertexData;
import emergencylanding.k.library.lwjgl.tex.ColorTexture;
import emergencylanding.k.library.lwjgl.tex.ELTexture;

public class DisplayableInstrument {
    private static final int NOTES = 128; // 128 notes
    private VBAO[] notes = new VBAO[NOTES];
    private Random rdom = new Random(
            (long) (System.currentTimeMillis() / 100 * 5.6));
    private final int rx = 0, ry, rz = rdom.nextInt(100);
    private final int dim = (int) (((float) Display.getWidth()) / (((float) NOTES) * 1.15f));
    private Rectangle bounding;
    private Runnable guiRunnable = new DIGUI(this);
    private static final ELTexture noteOnStatic = new ColorTexture(Color.RED);
    private static final ELTexture noteOffStatic = new ColorTexture(
            Color.DARK_GRAY);
    private ELTexture noteOn = noteOnStatic;
    private ELTexture noteOff = noteOffStatic;
    private final int id;

    public DisplayableInstrument(int idChannel) {
        id = idChannel;
        int ry_ = 0;
        ry_ += dim / 2;
        ry_ += id * (dim * 2);
        ry_++;
        ry = ry_;
        bounding = new Rectangle(0, ry, (int) (dim * (NOTES + 1) * 1.2f),
                dim * 2);
        setupVBAOS();
    }

    private void setupVBAOS() {
        // old code, saved for vert translating
        for (int i = 0; i < NOTES; i++) {
            int on_next = (int) ((i * dim) / Display.getWidth());
            /*
             * Shapes.glQuad( (int) (rx + (dim * ((i / (on_next + 1)) + 1) *
             * 1.2f)), ry + (dim * on_next * 2), rz, dim, dim * 2, dim,
             * Shapes.XYF, color);
             */
            VBAO next = Shapes.getQuad(
                    new VertexData().setXYZ((int) (rx + (dim
                            * ((i / (on_next + 1)) + 1) * 1.2f)), ry
                            + (dim * on_next * 2), rz),
                    new VertexData().setXYZ(dim, dim * 2, dim), Shapes.XY);
            next.setTexture(noteOffColor());
            notes[i] = next;
        }
    }

    public void startNote(int note) {
        notes[note].setTexture(noteOnColor());
    }

    public void stopNote(int note) {
        notes[note].setTexture(noteOffColor());
    }

    public void draw() {
        if (MouseHelp.clickedInRect(bounding, MouseHelp.LMB)) {
            openGUI();
        }
        for (int i = 0; i < NOTES; i++) {
            VBAO draw = notes[i];
            draw.draw();
        }
    }

    private void openGUI() {
        Thread t = new Thread(guiRunnable);
        t.start();
    }

    public void stopAll() {
        for (int i = 0; i < NOTES; i++) {
            notes[i].setTexture(noteOffColor());
        }
    }

    public ELTexture noteOnColor() {
        if (noteOn == null) {
            noteOn = noteOnStatic;
        }
        return noteOn;
    }

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

    public int getID() {
        return id;
    }
}
