package com.k.midishapes.midi;

import java.awt.Color;
import java.awt.Dialog;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Arrays;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JDialog;
import javax.swing.JPanel;

import k.core.util.gui.SwingAWTUtils;
import emergencylanding.k.library.lwjgl.tex.ColorTexture;
import emergencylanding.k.library.lwjgl.tex.ELTexture;

public class DIGUI implements Runnable {
    private DisplayableInstrumentImpl ourInst = null;
    final JColorChooser noteOn = new JColorChooser();
    final JColorChooser noteOff = new JColorChooser();

    public DIGUI(DisplayableInstrumentImpl di) {
        ourInst = di;
        noteOn.setName("Note On Color");
        noteOff.setName("Note Off Color");
    }

    @Override
    public void run() {
        ELTexture ourTex = ourInst.noteOnColor();
        Color c = Color.WHITE;
        if (ourTex instanceof ColorTexture) {
            c = ((ColorTexture) ourTex).getRawColor();
        }
        noteOn.setColor(c);
        ELTexture ourTex2 = ourInst.noteOffColor();
        Color c2 = Color.WHITE;
        if (ourTex2 instanceof ColorTexture) {
            c2 = ((ColorTexture) ourTex2).getRawColor();
        }
        noteOff.setColor(c2);
        JPanel disp_panel = new JPanel(new GridLayout(5, 5));
        disp_panel.setBackground(Color.BLUE);
        final JButton jbutton_color = new JButton();
        jbutton_color.setText("Note On Color");
        jbutton_color.setForeground(Color.BLACK);
        jbutton_color.setBackground(c);
        jbutton_color.setAction(new AbstractAction() {
            private static final long serialVersionUID = 1L;

            @Override
            public void actionPerformed(ActionEvent arg0) {
                Color newColor = showColorChoices(noteOn);
                jbutton_color.setBackground(newColor);
            }
        });
        disp_panel.add(jbutton_color);
        final JButton jbutton_color2 = new JButton();
        jbutton_color2.setText("Note Off Color");
        jbutton_color2.setForeground(Color.BLACK);
        jbutton_color2.setBackground(c2);
        jbutton_color2.setAction(new AbstractAction() {
            private static final long serialVersionUID = 1L;

            @Override
            public void actionPerformed(ActionEvent arg0) {
                Color newColor = showColorChoices(noteOff);
                jbutton_color2.setBackground(newColor);
            }
        });
        disp_panel.add(jbutton_color2);
        JDialog dialog = new JDialog((Dialog) null, "Options for Track", true);
        dialog.add(disp_panel);
        dialog.pack();
        dialog.setLocationRelativeTo(null);
        dialog.setAlwaysOnTop(false);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        SwingAWTUtils.attachDisposeOnClose(dialog);
        // NB setVisible is last on list because it blocks.
        dialog.setVisible(true);
        dialog.dispose();
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
