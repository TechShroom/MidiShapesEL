package com.k.midishapes.midi;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import javax.sound.midi.MidiSystem;
import javax.sound.midi.Sequencer;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.lwjgl.opengl.Display;

import com.k.midishapes.interfacing.DIMod;
import com.k.midishapes.interfacing.DisplayableInstrument;
import com.k.midishapes.midi.custom.ChainedReceiver;

import emergencylanding.k.exst.mods.IMod;
import emergencylanding.k.library.debug.FPS;
import emergencylanding.k.library.lwjgl.DisplayLayer;
import emergencylanding.k.library.lwjgl.control.Keys;
import emergencylanding.k.library.lwjgl.tex.ELTexture;
import emergencylanding.k.library.main.KMain;
import emergencylanding.k.library.util.LUtils;
import k.core.util.core.Helper.ProgramProps;

public class MidiMain extends KMain implements KeyListener {

    
    private static final class SaveJFCThread extends Thread {
        
        private final JFileChooser toSave;
        private final String config;

        public SaveJFCThread(JFileChooser jfc, String config) {
            this.toSave = jfc;
            this.config = config;
        }
        
        @Override
        public void run() {
            try (XMLEncoder enc = new XMLEncoder(configWriter(config))) {
                enc.writeObject(toSave);
            } catch (IOException e) {
                System.err.println("Error saving JFC in " + config);
                e.printStackTrace();
            }
        }

    }

    private static final Path CONFIG = Paths.get("./config");

    static {
        try {
            Files.createDirectories(CONFIG);
        } catch (IOException cannotCreate) {
            System.err.println("Couldn't create config dir:");
            cannotCreate.printStackTrace();
        }
    }

    private static Path from(String path) {
        return CONFIG.resolve(path);
    }

    public static OutputStream configWriter(String path) throws IOException {
        return Files.newOutputStream(from(path));
    }

    public static InputStream configReader(String path) throws IOException {
        return Files.newInputStream(from(path));
    }

    public static boolean isConfigPresent(String path) {
        return Files.exists(from(path));
    }

    public static void main(String[] args) {
        String[] norm = ProgramProps.normalizeCommandArgs(args);
        for (int i = 0; i < norm.length; i += 2) {
            String key = norm[i], value = norm[i + 1];
            ProgramProps.acceptPair(key, value);
        }
        try {
            Method getsbr =
                    MidiSystem.class.getDeclaredMethod("getSoundbankReaders");
            getsbr.setAccessible(true);
            List<Object> l =
                    new ArrayList<Object>((List<?>) getsbr.invoke(null)),
                    del = new ArrayList<Object>(),
                    add = new ArrayList<Object>();
            for (Object o : l) {
                add.add(o.getClass());
                del.add(o);
                Class<?> klass = o.getClass();
                URL location = klass.getResource(
                        '/' + klass.getName().replace('.', '/') + ".class");
                System.err.println(location);
            }
            l.removeAll(del);
            l.addAll(add);
            System.err.println(l);
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            UIManager.setLookAndFeel(
                    UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException
                | IllegalAccessException | UnsupportedLookAndFeelException e1) {
            e1.printStackTrace();
            // eh.
        }
        Dimension d = new Dimension(800, 600);
        FPS.enable(0);
        // FPS.disable(0);
        try {
            DisplayLayer.initDisplay(false, d.width, d.height, "Midi Shapes",
                    false, args);
            while (!Display.isCloseRequested() || DisplayHackThread.isInstanceBooting()) {
                DisplayLayer.loop(120);
            }
            // Ensure all bindings processed
            ELTexture.doBindings();
            DisplayLayer.destroy();
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
        Thread[] threads = new Thread[Thread.activeCount() + 10];
        Thread.enumerate(threads);
        for (Thread thread : threads) {
            if (thread == null || thread.isDaemon()
                    || thread.equals(Thread.currentThread())) {
                continue;
            }
            try {
                thread.join(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (thread.isAlive()) {
                thread.interrupt();
                try {
                    thread.join(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (thread.isAlive()) {
                    // Give up. Force a clean exit here.
                    deprecatedStop(thread);
                }
            }
        }
    }

    @SuppressWarnings("deprecation")
    private static void deprecatedStop(Thread thread) {
        thread.stop();
    }

    boolean reboot;
    private JFileChooser sbfc = new JFileChooser(), ffc = new JFileChooser();

    {
        final String FFC_CONFIG = "ffc.xml";
        final String SBFC_CONFIG = "sbfc.xml";
        boolean doSetFFC = true;
        boolean doSetSBFC = true;
        if (isConfigPresent(SBFC_CONFIG)) {
            try (
                    XMLDecoder dec =
                            new XMLDecoder(configReader(SBFC_CONFIG))) {
                sbfc = (JFileChooser) dec.readObject();
                doSetSBFC = false;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (isConfigPresent(FFC_CONFIG)) {
            try (
                    XMLDecoder dec = new XMLDecoder(configReader(FFC_CONFIG))) {
                ffc = (JFileChooser) dec.readObject();
                doSetFFC = false;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        JFileChooser jfc = ffc;
        if (doSetFFC) {
            jfc.removeChoosableFileFilter(jfc.getAcceptAllFileFilter());
            jfc.addChoosableFileFilter(
                    new FileNameExtensionFilter("MIDI Files", "mid", "midi"));
            jfc.setFileHidingEnabled(false);
            Runtime.getRuntime().addShutdownHook(new SaveJFCThread(jfc, FFC_CONFIG));
        }
        jfc = sbfc;
        if (doSetSBFC) {
            jfc.removeChoosableFileFilter(jfc.getAcceptAllFileFilter());
            jfc.addChoosableFileFilter(
                    new FileNameExtensionFilter("SoundFont2 Files", "sf2"));
            jfc.setFileHidingEnabled(false);
            Runtime.getRuntime().addShutdownHook(new SaveJFCThread(jfc, SBFC_CONFIG));
        }
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
        // hum....
        MidiDisplayer.stop(true);
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
            LUtils.setIcon(LUtils.getInputStream(
                    LUtils.TOP_LEVEL + "/resource/img/midishapesFIN.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (args.length > 0 && args.length < 2
                && !ProgramProps.hasKey("file")) {
            ProgramProps.acceptPair("file", args[0]);
        }
        if (!ProgramProps.hasKey("file")) {
            if (!askForFile()) {
                ProgramProps.acceptPair("file", "");
            }
        } else {
            Path theFile = Paths.get(ProgramProps.getProperty("file"));
            ffc.setCurrentDirectory(theFile.getParent().toFile());
        }
        if (!ProgramProps.hasKey("soundbank")) {
            if (!askForSB()) {
                ProgramProps.acceptPair("soundbank", "");
            }
        } else {
            Path theFile = Paths.get(ProgramProps.getProperty("soundbank"));
            sbfc.setCurrentDirectory(theFile.getParent().toFile());
        }
        // prevents sync issues
        DefaultDisplayableInstrument.init();
        MidiReader.init();
        MidiDisplayer.init();
        ChainedReceiver.init();
        MidiPlayer.start();
        Keys.registerListener(this, false);
    }

    private boolean askForFile() {
        JFileChooser jfc = ffc;
        // apply always-on-top
        Frame f = new JFrame();
        f.setAlwaysOnTop(true);
        if (shifty) {
            // use a inputboxthing
            String s = JOptionPane.showInputDialog(f, "File:");
            if (s == null || s.trim().isEmpty()) {
                return false;
            }
            Path p = Paths.get(s.trim());
            if (!Files.exists(p)) {
                System.err.println("NoSuchFileException: " + p);
                return false;
            }
            ProgramProps.acceptPair("file", p.toAbsolutePath().toString());
            return true;
        }
        int yes = jfc.showOpenDialog(f);
        f.dispose();
        if (yes != JFileChooser.CANCEL_OPTION
                && jfc.getSelectedFile() != null) {
            ProgramProps.acceptPair("file",
                    jfc.getSelectedFile().getAbsolutePath());
            return true;
        }
        return false;
    }

    private boolean askForSB() {
        JFileChooser jfc = sbfc;
        // apply always-on-top
        Frame f = new JFrame();
        f.setAlwaysOnTop(true);
        if (shifty) {
            // use a inputboxthing
            String s = JOptionPane.showInputDialog(f, "File:");
            if (s == null || s.trim().isEmpty()) {
                return false;
            }
            Path p = Paths.get(s.trim());
            if (!Files.exists(p)) {
                System.err.println("NoSuchFileException: " + p);
                return false;
            }
            ProgramProps.acceptPair("soundbank", p.toAbsolutePath().toString());
            return true;
        }
        int yes = jfc.showOpenDialog(f);
        f.dispose();
        if (yes != JFileChooser.CANCEL_OPTION
                && jfc.getSelectedFile() != null) {
            ProgramProps.acceptPair("soundbank",
                    jfc.getSelectedFile().getAbsolutePath());
            return true;
        }
        return false;
    }

    private boolean shifty = false;

    @Override
    public void keyPressed(KeyEvent arg0) {
        if (arg0.getKeyCode() == KeyEvent.VK_SHIFT) {
            shifty = true;
        }
    }

    @Override
    public void keyReleased(KeyEvent arg0) {
        if (arg0.getKeyCode() == KeyEvent.VK_SHIFT) {
            shifty = false;
        }
        final int key = arg0.getKeyCode();
        Runnable r = new Runnable() {

            @Override
            public void run() {
                if (DisplayHackThread.isInstanceBooting()) {
                    // DO NOT PROCESS EVENTS WHILE BOOTING.
                    return;
                }
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
                    if (MidiPlayer.repeat) {
                        DisplayHackThread.actOnSequencer(s -> {
                            s.setLoopCount(Sequencer.LOOP_CONTINUOUSLY);
                            if (!s.isRunning()) {
                                s.start();
                            }
                        });
                    } else {
                        DisplayHackThread
                                .actOnSequencer(s -> s.setLoopCount(0));
                    }
                    System.err.println("Repeat is now "
                            + (MidiPlayer.repeat ? "on" : "off") + ".");
                }
                if (key == KeyEvent.VK_U) {
                    reboot = MidiReader.user_recv_req();
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
                MidiDisplayer.registerClass(
                        (Class<? extends DisplayableInstrument<?>>) ((DisplayableInstrument<?>) m)
                                .getClass());
            } else {
                rem.add(m);
            }
        }
        mods.removeAll(rem);
    }
}
