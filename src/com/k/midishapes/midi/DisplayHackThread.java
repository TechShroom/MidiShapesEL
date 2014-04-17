package com.k.midishapes.midi;

import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Receiver;
import javax.sound.midi.Sequence;
import javax.sound.midi.Synthesizer;
import javax.sound.midi.Track;

import com.k.midishapes.midi.MidiUtils.EventCache;
import com.k.midishapes.midi.MidiUtils.TempoCache;

import emergencylanding.k.library.debug.FPS;
import g.com.sun.media.sound.MidiDeviceReceiver;

public class DisplayHackThread extends Thread {
    public TempoCache tc;
    public long tick;
    public static boolean running, abort;
    protected static Receiver recv = null;
    private static Synthesizer syn = null;

    Sequence s = null;
    HashMap<Track, SubHack> subs = new HashMap<Track, SubHack>();
    private long micro_tick;
    public static boolean pause;
    static DisplayHackThread inst = null;

    public static void begin(Sequence file) {
        if (inst != null) {
            try {
                // psh, forgot to abort
                abort = true;
                inst.join(10);
                System.err.println("joined");
            } catch (InterruptedException e) {
                inst.interrupt();
            }
            if (inst.isAlive()) {
                inst.interrupt();
            }
            inst = null;
        }
        // remove instruments
        MidiDisplayer.stop(false);
        inst = new DisplayHackThread(file);
        inst.setDaemon(true);
        inst.start();
    }

    public DisplayHackThread(Sequence file) {
        super("DisplayHackThread");
        s = file;
        subs.clear();
        EventCache.complete.set(0);
        try {
            if (recv != null) {
                recv.close();
            }
            syn = MidiReader.openSynth();
            recv = syn.getReceiver();
            if (recv_not_open(recv) && recv instanceof MidiDeviceReceiver) {
                ((MidiDeviceReceiver) recv).getMidiDevice().open();
            } else if (recv_not_open(recv)) {
                throw new IllegalStateException(
                        "Receiver not opened, and not openable: "
                                + recv.getClass());
            }
        } catch (MidiUnavailableException e1) {
            e1.printStackTrace();
            abort = true;
        }
        if (abort) {
            return;
        }
        final AtomicInteger index = new AtomicInteger(0);
        int trks = 0;
        System.err.println("Loading " + s.getTracks().length + " track(s).");
        for (final Track t : s.getTracks()) {
            if (t.size() <= 1) {
                continue;
            }
            final int currtrks = trks;
            Runnable r = new Runnable() {
                @Override
                public void run() {
                    subs.put(t, new SubHack(MidiUtils.channel(t)));
                    subs.get(t).ec = new EventCache(t);
                    subs.get(t).t = t;
                    index.incrementAndGet();
                    System.err.println("Track " + currtrks + " completed.");
                }
            };
            new Thread(r, "TrackLoader" + trks).start();
            trks++;
        }
        while (EventCache.complete.get() < trks) {
            try {
                Thread.sleep(1);
            } catch (InterruptedException ie) {
            }
        }
        System.err.println("[DisplayHackThread] Created " + index + "/" + trks
                + " tracks. (reported that we completed " + EventCache.complete
                + ")");
        tc = new TempoCache(file);
        // don't twice it
        // tc.refresh(file);
    }

    private boolean recv_not_open(Receiver r) {
        try {
            r.send(SubHack.sm_alloff(1), -1);
            return false;
        } catch (IllegalStateException ise) {
            return true;
        }
    }

    public DisplayHackThread(Sequence file, HashMap<Track, SubHack> tsh,
            TempoCache tcache) {
        s = file;
        subs = tsh;
        tc = tcache;
        tc.refresh(file);
    }

    @Override
    public void run() {
        micro_tick = MidiUtils.tick2microsecond(s, 0, tc);
        long micro = 0;
        try {
            Thread.sleep(10);
        } catch (InterruptedException is) {
        }
        FPS.init(1, FPS.micro);
        if (abort) {
            recv.close();
            recv = null;
            MidiReader.closeSynth(syn);
            s = null;
            System.err.println("Aborted.");
            return;
        }
        System.err.println("DisplayHackThread is going to run " + subs.size()
                + " tracks.");
        running = true;
        FPS.getDelta(1, FPS.micro);
        tick = 0;
        for (SubHack sh : subs.values()) {
            sh.update(tick);
        }
        do {
            micro += FPS.getDelta(1, FPS.micro);
            while (micro >= micro_tick) {
                tick++;
                if (s == null) {
                    // a weird state, but okay
                    abort = true;
                    continue;
                }
                if (tick > s.getTickLength()) {
                    MidiPlayer.mpt.stop0();
                    break;
                }

                for (SubHack sh : subs.values()) {
                    sh.update(tick);
                }
                micro -= micro_tick;
                micro_tick = MidiUtils.tick2microsecond(s, tick, tc) / tick;
            }
            while (pause) {
                try {
                    Thread.sleep(1);
                    FPS.getDelta(1, FPS.micro); // Prevent note skip
                } catch (Exception e) {
                }
            }
        } while (running && !abort);
        abort = false;
        running = false;
        recv.close();
        recv = null;
        MidiReader.closeSynth(syn);
        s = null;
    }

    public static void repeat() {
        inst.tick = 0;
        inst.micro_tick = MidiUtils.tick2microsecond(inst.s, 1, inst.tc);
        try {
            Thread.sleep(10);
        } catch (InterruptedException is) {
        }
        FPS.getDelta(1, FPS.micro);
    }
}