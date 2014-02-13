package com.k.midishapes;

import java.awt.Dimension;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.sound.midi.MidiSystem;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

import k.core.util.Helper.ProgramProps;

import org.lwjgl.opengl.Display;

import com.k.midishapes.midi.MidiDisplayer;
import com.k.midishapes.midi.MidiPlayer;
import com.k.midishapes.midi.MidiReader;

import emergencylanding.k.library.debug.FPS;
import emergencylanding.k.library.internalstate.ELEntity;
import emergencylanding.k.library.lwjgl.DisplayLayer;
import emergencylanding.k.library.lwjgl.control.Keys;
import emergencylanding.k.library.lwjgl.render.Render;
import emergencylanding.k.library.main.KMain;

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

    private boolean reboot;

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
        if (args.length > 0 && args.length < 2 && !ProgramProps.hasKey("file")) {
            ProgramProps.acceptPair("file", args[0]);
        }
        if (!ProgramProps.hasKey("file")) {
            if (!askForFile()) {
                ProgramProps.acceptPair("file", "");
            }
        }
        if (!ProgramProps.hasKey("soundbank")) {
            JFileChooser jfc = new JFileChooser();
            jfc.removeChoosableFileFilter(jfc.getAcceptAllFileFilter());
            jfc.addChoosableFileFilter(new FileNameExtensionFilter(
                    "SoundFont2 Files", "sf2"));
            jfc.showOpenDialog(null);
            if (jfc.getSelectedFile() != null) {
                ProgramProps.acceptPair("soundbank", jfc.getSelectedFile()
                        .getAbsolutePath());
            }
        }
        MidiReader.init();
        MidiDisplayer.init();
        MidiPlayer.start();
        Keys.registerListener(this, false);
    }

    private boolean askForFile() {
        JFileChooser jfc = new JFileChooser();
        jfc.removeChoosableFileFilter(jfc.getAcceptAllFileFilter());
        jfc.addChoosableFileFilter(new FileNameExtensionFilter("MIDI Files",
                "mid", "midi"));
        // WUtils.windows_safe_JFC(jfc, JFileChooser.OPEN_DIALOG);
        jfc.showOpenDialog(null);
        if (jfc.getSelectedFile() != null) {
            ProgramProps.acceptPair("file", jfc.getSelectedFile()
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
        int key = arg0.getKeyCode();
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
        if (key == KeyEvent.VK_R) {
            MidiPlayer.repeat = !MidiPlayer.repeat;
        }

    }

    @Override
    public void keyTyped(KeyEvent arg0) {
    }

    @Override
    public void registerRenders(
            HashMap<Class<? extends ELEntity>, Render<? extends ELEntity>> classToRender) {
    }
}
