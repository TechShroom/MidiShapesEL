package com.k.midishapes.midi;

import java.awt.Rectangle;

import org.lwjgl.opengl.Display;

import emergencylanding.k.library.lwjgl.Shapes;
import emergencylanding.k.library.lwjgl.control.MouseHelp;
import emergencylanding.k.library.lwjgl.render.VBAO;
import emergencylanding.k.library.lwjgl.render.VertexData;

public class DisplayableInstrumentImpl extends DefaultDisplayableInstrument {
    private VBAO[] notes = new VBAO[NOTES];
    private Runnable guiRunnable = new DIGUI(this);
    private final int rx = 0, ry, rz = 0;
    private final int dim = (int) (((float) Display.getWidth()) / (((float) NOTES) * 1.15f));
    private Rectangle bounding;

    public DisplayableInstrumentImpl(int idChannel) {
        super(idChannel);
        int ry_ = 0;
        ry_ += dim / 2;
        ry_ += id * (dim * 2);
        ry_++;
        ry = ry_;
        bounding = new Rectangle(0, ry, (int) (dim * (NOTES + 1) * 1.2f),
                dim * 2);
        setupVBAOS();
    }

    @Override
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

    @Override
    public void startNote(int note) {
        notes[note].setTexture(noteOnColor());
    }

    @Override
    public void stopNote(int note) {
        notes[note].setTexture(noteOffColor());
    }

    @Override
    public void stopAll() {
        for (int i = 0; i < NOTES; i++) {
            notes[i].setTexture(noteOffColor());
        }
    }

    private void setupVBAOS() {
        for (int i = 0; i < NOTES; i++) {
            int on_next = (int) ((i * dim) / Display.getWidth());
            VBAO next = Shapes.getQuad(
                    new VertexData().setXYZ((int) (rx + (dim
                            * ((i / (on_next + 1)) + 1) * 1.2f)), ry
                            + (dim * on_next * 2), rz),
                    new VertexData().setXYZ(dim, dim * 2, dim), Shapes.XY);
            next.setTexture(noteOffColor());
            notes[i] = next;
        }
    }
}
