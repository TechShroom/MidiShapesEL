package com.k.midishapes.midi;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Receiver;
import javax.sound.midi.Sequence;
import javax.sound.midi.Sequencer;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Synthesizer;
import javax.sound.midi.Track;

import com.k.midishapes.midi.MidiUtils.EventCache;
import com.k.midishapes.midi.MidiUtils.TempoCache;

import emergencylanding.k.imported.Sync;
import emergencylanding.k.library.debug.FPS;
import g.com.sun.media.sound.MidiDeviceReceiver;

public class MidiPlayer {
    public static final int NOTE_ON = 0x90, NOTE_OFF = 0x80,
            CONTROL_CHANGE = 0xB0, ALL_NOTES_OFF = 123, ALL_SOUND_OFF = 120,
            OMNI_ON = 125, OMNI_OFF = 124, MONO = 126, POLY = 127;

    public static class DisplayHackThread extends Thread {
        public static class SubHack implements Runnable {
            private int ourDIId = 0;

            public SubHack(int track) {
                this.track = track;
                ShortMessage smalloff = sm_alloff(track >= 16 ? 15 : track);
                ourDIId = MidiDisplayer.sendToInstrument(smalloff, -1);
                recv.send(smalloff, -1);
            }

            private static ShortMessage sm_alloff(int chan) {
                ShortMessage str = new ShortMessage();
                try {
                    str.setMessage(CONTROL_CHANGE, chan, ALL_NOTES_OFF, 0);
                } catch (InvalidMidiDataException e) {
                    e.printStackTrace();
                }
                return str;
            }

            public EventCache ec;

            public Track t;

            public int track;

            public long lastTick = -1;

            private Sync syncObj = new Sync();

            public void runUpdates() {
                if (inst.tick > lastTick
                        && ec.tick_to_index.get(inst.tick) != null) {
                    update(inst.tick);
                    lastTick = inst.tick;
                }
            }

            public void update(long tick) {
                ArrayList<Integer> indexes = MidiUtils.tick2indexEC(tick, ec);
                for (Integer index : indexes) {
                    MidiEvent me = t.get(index);
                    MidiMessage m = me.getMessage();
                    if (m instanceof ShortMessage) {
                        MidiDisplayer.sendToInstrument((ShortMessage) m,
                                ourDIId);
                    }
                    recv.send(m, -1);
                }
            }

            @Override
            public void run() {
                while (!running) {
                    try {
                        Thread.sleep(1);
                    } catch (InterruptedException is) {
                    }
                }
                while (running && !abort) {
                    runUpdates();
                    syncObj.sync(1000);
                }
            }

        }

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
            System.err
                    .println("Loading " + s.getTracks().length + " track(s).");
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
            System.err.println("[DisplayHackThread] Created " + index + "/"
                    + trks + " tracks. (reported that we completed "
                    + EventCache.complete + ")");
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
            System.err.println("DisplayHackThread is going to run "
                    + subs.size() + " tracks.");
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
                        mpt.stop0();
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

    private static MidiPlay mpt;
    public static boolean repeat;

    public static void start() {
        try {
            mpt = new MidiPlay();
        } catch (Exception e) {
            System.err
                    .println("MidiPlay not launchable, please report the following:");
            e.printStackTrace();
            mpt = MidiPlay.EXCEPTION;
            return;
        }
        mpt.run();
    }

    public static void stop() {
        // not normal: repeat shouldn't go through
        mpt.stop0(false);
    }

    public static void pause() {
        if (mpt.micro > -1) {
            mpt.run();
        } else {
            mpt.pause();
        }
    }

    public static void exit() {
        mpt.exit();
    }

    private static class MidiPlay {
        public static final MidiPlay EXCEPTION = new MidiPlay(true);
        Sequencer seq = null;
        Sequence file = null;
        private long micro = -1;
        private boolean exceptionMode;

        public MidiPlay() throws Exception {
            seq = MidiSystem.getSequencer();
            file = MidiReader.decodedSequence();
            seq.open();
            seq.setSequence(file);
        }

        private MidiPlay(boolean excep) {
            exceptionMode = excep;
        }

        public void run() {
            if (exceptionMode) {
                return;
            }
            if (micro > -1) {
                DisplayHackThread.pause = false;
                micro = -1;
            } else {
                DisplayHackThread.begin(file);
            }
        }

        public void stop0() {
            stop0(true);
        }

        public void stop0(boolean normal) {
            if (exceptionMode) {
                return;
            }
            DisplayHackThread.running = repeat && normal;
            DisplayHackThread.pause = repeat && normal
                    && DisplayHackThread.pause;
            MidiDisplayer.stop(true);
            if (repeat && normal) {
                DisplayHackThread.repeat();
            } else {
                try {
                    DisplayHackThread.abort = true;
                    DisplayHackThread.inst.join(100);
                } catch (InterruptedException e) {
                    DisplayHackThread.inst.interrupt();
                }
                if (DisplayHackThread.inst.isAlive()) {
                    DisplayHackThread.inst.interrupt();
                }
                // reset state
                DisplayHackThread.running = DisplayHackThread.abort = false;
                DisplayHackThread.inst = null;
                micro = -1;
            }
        }

        public void pause() {
            if (exceptionMode) {
                return;
            }
            micro = DisplayHackThread.inst.tick;
            DisplayHackThread.pause = true;
            MidiDisplayer.stop(true);
        }

        public void exit() {
            if (exceptionMode) {
                return;
            }
            stop0(false);
            seq.setMicrosecondPosition(0);
            seq.close();
        }
    }

    public static boolean isPlaying() {
        return DisplayHackThread.running;
    }

}
