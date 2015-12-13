package com.k.midishapes.midi;

import java.awt.Color;
import java.awt.Dialog;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JDialog;
import javax.swing.JPanel;

import emergencylanding.k.library.lwjgl.tex.ColorTexture;
import emergencylanding.k.library.lwjgl.tex.ELTexture;
import k.core.util.gui.SwingAWTUtils;

public class DIGUI implements Runnable {

    private DisplayableInstrumentImpl ourInst = null;
    static final JColorChooser noteOn = new JColorChooser();
    static final JColorChooser noteOff = new JColorChooser();
    static {
        noteOn.setName("Note On Color");
        noteOff.setName("Note Off Color");
    }

    public DIGUI(DisplayableInstrumentImpl di) {
        ourInst = di;
    }

    @Override
    public void run() {
        ELTexture ourTex = ourInst.noteOnColor();
        Color c = Color.WHITE;
        if (ourTex instanceof ColorTexture) {
            c = ((ColorTexture) ourTex).getRawColor();
        } else {
            return;
        }
        ELTexture ourTex2 = ourInst.noteOffColor();
        Color c2 = Color.WHITE;
        if (ourTex2 instanceof ColorTexture) {
            c2 = ((ColorTexture) ourTex2).getRawColor();
        } else {
            return;
        }
        noteOn.setColor(c);
        noteOff.setColor(c2);
        JPanel displayPanel = new JPanel(new GridLayout(5, 5));
        displayPanel.setBackground(Color.BLUE);
        final JButton primaryColor = new JButton();
        primaryColor.setForeground(Color.BLACK);
        primaryColor.setBackground(c);
        primaryColor.setFocusPainted(false);
        primaryColor.setAction(new AbstractAction("Note On Color") {

            private static final long serialVersionUID = 1L;

            @Override
            public void actionPerformed(ActionEvent arg0) {
                Color newColor = showColorChoices(noteOn);
                primaryColor.setBackground(newColor);
            }
        });
        displayPanel.add(primaryColor);
        final JButton secondaryColor = new JButton();
        secondaryColor.setForeground(Color.BLACK);
        secondaryColor.setBackground(c2);
        secondaryColor.setFocusPainted(false);
        secondaryColor.setAction(new AbstractAction("Note Off Color") {

            private static final long serialVersionUID = 1L;

            @Override
            public void actionPerformed(ActionEvent arg0) {
                Color newColor = showColorChoices(noteOff);
                secondaryColor.setBackground(newColor);
            }
        });
        displayPanel.add(secondaryColor);
        JDialog dialog = new JDialog((Dialog) null, "Options for Track", true);
        dialog.add(displayPanel);
        dialog.setSize(500, 300);
        dialog.setLocationRelativeTo(null);
        dialog.setAlwaysOnTop(false);
        dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        SwingAWTUtils.attachDisposeOnClose(dialog);
        // NB setVisible is last on list because it blocks.
        dialog.setVisible(true);
        dialog.dispose();
        // Reset textures.
        ColorTexture ctex1 = new ColorTexture(primaryColor.getBackground());
        ColorTexture ctex2 = new ColorTexture(secondaryColor.getBackground());
        ourInst.setNoteOn(ctex1);
        ourInst.setNoteOff(ctex2);
        ourInst.redraw();
    }

    Color retVal = null;

    protected Color showColorChoices(final JColorChooser jcc) {
        retVal = jcc.getColor();
        JDialog dialog = JColorChooser.createDialog(null, "Choose a color:",
                true, jcc, new AbstractAction() {

                    private static final long serialVersionUID = 1L;

                    @Override
                    public void actionPerformed(ActionEvent e) {
                        retVal = jcc.getColor();
                    }
                }, null);
        // NB if the dialog won't work, revert to deprecated show
        // currently setVisible() is just a pass -> show()
        // dialog.show();
        dialog.setVisible(true);
        return retVal;
    }

}
