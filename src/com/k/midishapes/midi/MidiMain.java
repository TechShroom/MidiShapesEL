package com.k.midishapes.midi;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.sound.midi.MidiSystem;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;

import k.core.util.core.Helper.ProgramProps;

import org.lwjgl.opengl.Display;

import com.k.midishapes.interfacing.DIMod;
import com.k.midishapes.interfacing.DisplayableInstrument;

import emergencylanding.k.exst.mods.IMod;
import emergencylanding.k.library.debug.FPS;
import emergencylanding.k.library.lwjgl.DisplayLayer;
import emergencylanding.k.library.lwjgl.control.Keys;
import emergencylanding.k.library.lwjgl.tex.ELTexture;
import emergencylanding.k.library.main.KMain;
import emergencylanding.k.library.util.LUtils;

public class MidiMain extends KMain implements KeyListener {
    public static void main(String[] args) {
        String[] norm = ProgramProps.normalizeCommandArgs(args);
        for (int i = 0; i < norm.length; i += 2) {
            String key = norm[i], value = norm[i + 1];
            ProgramProps.acceptPair(key, value);
        }
        try {
            Method getsbr = MidiSystem.class
                    .getDeclaredMethod("getSoundbankReaders");
            getsbr.setAccessible(true);
            List<Object> l = new ArrayList<Object>(
                    (List<?>) getsbr.invoke(null)), del = new ArrayList<Object>(), add = new ArrayList<Object>();
            for (Object o : l) {
                add.add(o.getClass());
                del.add(o);
                Class<?> klass = o.getClass();
                URL location = klass.getResource('/'
                        + klass.getName().replace('.', '/') + ".class");
                System.err.println(location);
            }
            l.removeAll(del);
            l.addAll(add);
            System.err.println(l);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Dimension d = new Dimension(800, 600);
        FPS.enable(0);
        FPS.disable(0);
        try {
            DisplayLayer.initDisplay(false, d.width, d.height, "Midi Shapes",
                    false, args);
            while (!Display.isCloseRequested()) {
                DisplayLayer.loop(120);
            }
            Display.destroy();
        } catch (Exception e) {
            e.printStackTrace();
            MidiReader.exit();
            MidiDisplayer.exit();
            MidiPlayer.exit();
            System.exit(1);
        }
        MidiReader.exit();
        MidiDisplayer.exit();
        MidiPlayer.exit();
    }

    boolean reboot;
    private JFileChooser sbfc = new JFileChooser(), ffc = new JFileChooser();

    {
        JFileChooser jfc = ffc;
        jfc.removeChoosableFileFilter(jfc.getAcceptAllFileFilter());
        jfc.addChoosableFileFilter(new FileNameExtensionFilter("MIDI Files",
                "mid", "midi"));
        jfc = sbfc;
        jfc.removeChoosableFileFilter(jfc.getAcceptAllFileFilter());
        jfc.addChoosableFileFilter(new FileNameExtensionFilter(
                "SoundFont2 Files", "sf2"));
    }

    @Override
    public void onDisplayUpdate(int delta) {
        if (reboot) {
            reboot();
        }
        MidiDisplayer.display();
        DisplayLayer.readDevices();
    }

    private void reboot() {
        MidiReader.exit();
        MidiDisplayer.exit();
        MidiPlayer.exit();
        if (!KMain.getDisplayThread().equals(Thread.currentThread())) {
            throw new IllegalStateException("Must init in display thread");
        }
        MidiReader.init();
        MidiDisplayer.init();
        MidiPlayer.start();
        reboot = false;
    }

    @Override
    public void init(String[] args) {
        while (!Display.isCreated() || !Display.isVisible()) {
            try {
                Thread.sleep(1);
            } catch (Exception e) {
            }
        }
        try {
            LUtils.setIcon(LUtils.getInputStream(LUtils.TOP_LEVEL
                    + "/resource/img/midishapesFIN.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (args.length > 0 && args.length < 2 && !ProgramProps.hasKey("file")) {
            ProgramProps.acceptPair("file", args[0]);
        }
        if (!ProgramProps.hasKey("file")) {
            if (!askForFile()) {
                ProgramProps.acceptPair("file", "");
            }
        }
        if (!ProgramProps.hasKey("soundbank")) {
            if (!askForSB()) {
                ProgramProps.acceptPair("soundbank", "");
            }
        }
        // prevents sync issues
        DefaultDisplayableInstrument.init();
        MidiReader.init();
        MidiDisplayer.init();
        MidiPlayer.start();
        Keys.registerListener(this, false);
    }

    private boolean askForFile() {
        JFileChooser jfc = ffc;
        // apply always-on-top
        Frame f = JOptionPane.getRootFrame();
        f.setAlwaysOnTop(true);
        int yes = jfc.showOpenDialog(f);
        if (yes != JFileChooser.CANCEL_OPTION && jfc.getSelectedFile() != null) {
            ProgramProps.acceptPair("file", jfc.getSelectedFile()
                    .getAbsolutePath());
            return true;
        }
        return false;
    }

    private boolean askForSB() {
        JFileChooser jfc = sbfc;
        // apply always-on-top
        Frame f = JOptionPane.getRootFrame();
        f.setAlwaysOnTop(true);
        int yes = jfc.showOpenDialog(f);
        if (yes != JFileChooser.CANCEL_OPTION && jfc.getSelectedFile() != null) {
            ProgramProps.acceptPair("soundbank", jfc.getSelectedFile()
                    .getAbsolutePath());
            return true;
        }
        return false;
    }

    @Override
    public void keyPressed(KeyEvent arg0) {
    }

    @Override
    public void keyReleased(KeyEvent arg0) {
        final int key = arg0.getKeyCode();
        Runnable r = new Runnable() {

            @Override
            public void run() {
                if (key == KeyEvent.VK_P || key == KeyEvent.VK_SPACE) {
                    if (MidiPlayer.isPlaying()) {
                        MidiPlayer.pause();
                    } else {
                        MidiPlayer.start();
                    }
                }
                if (key == KeyEvent.VK_ESCAPE) {
                    MidiPlayer.stop();
                }
                if (key == KeyEvent.VK_F) {
                    boolean success = askForFile();
                    if (success) {
                        reboot = true;
                    }
                }
                if (key == KeyEvent.VK_S) {
                    boolean success = askForSB();
                    if (success) {
                        reboot = true;
                    }
                }
                if (key == KeyEvent.VK_R) {
                    MidiPlayer.repeat = !MidiPlayer.repeat;
                    System.err.println("Repeat is now "
                            + (MidiPlayer.repeat ? "on" : "off") + ".");
                }
            }
        };
        ELTexture.addRunnableToQueue(r);
    }

    @Override
    public void keyTyped(KeyEvent arg0) {
    }

    @SuppressWarnings("unchecked")
    @Override
    public void loadMods(ArrayList<IMod> mods) {
        ArrayList<IMod> rem = new ArrayList<IMod>(mods.size());
        for (IMod m : mods) {
            if (m instanceof DIMod) {
                MidiDisplayer.registerClass(((DIMod) m).getDIClass());
            } else if (m instanceof DisplayableInstrument<?>) {
                // messed up generics means this needs the unchecked cast
                MidiDisplayer
                        .registerClass((Class<? extends DisplayableInstrument<?>>) ((DisplayableInstrument<?>) m)
                                .getClass());
            } else {
                rem.add(m);
            }
        }
        mods.removeAll(rem);
    }
}
