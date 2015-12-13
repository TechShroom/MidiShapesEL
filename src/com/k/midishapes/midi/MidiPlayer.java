package com.k.midishapes.midi;

import javax.sound.midi.Sequence;

public class MidiPlayer {

    public static final int NOTE_ON = 0x90, NOTE_OFF = 0x80,
            CONTROL_CHANGE = 0xB0, ALL_NOTES_OFF = 123, ALL_SOUND_OFF = 120,
            OMNI_ON = 125, OMNI_OFF = 124, MONO = 126, POLY = 127;

    static MidiPlay mpt;
    public static boolean repeat;

    public static void start() {
        try {
            mpt = new MidiPlay();
        } catch (Exception e) {
            System.err.println(
                    "MidiPlay not launchable, please report the following:");
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
        if (mpt.paused) {
            mpt.run();
        } else {
            mpt.pause();
        }
    }

    public static void exit() {
        mpt.exit();
    }

    static class MidiPlay {

        public static final MidiPlay EXCEPTION = new MidiPlay(true);
        Sequence file = null;
        boolean exceptionMode, paused;

        public MidiPlay() throws Exception {
            file = MidiReader.decodedSequence();
        }

        private MidiPlay(boolean excep) {
            exceptionMode = excep;
        }

        public void run() {
            if (exceptionMode) {
                return;
            }
            if (paused) {
                DisplayHackThread.actOnSequencer(s -> s.start());
                paused = false;
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
            paused = repeat && normal && paused;
            MidiDisplayer.stop(true);
            if (repeat && normal) {
                DisplayHackThread.repeat();
            } else {
                if (DisplayHackThread.inst != null) {
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
                    DisplayHackThread.actOnSequencer(s -> s.close());
                    DisplayHackThread.abort = false;
                    DisplayHackThread.inst = null;
                }
            }
        }

        public void pause() {
            if (exceptionMode) {
                return;
            }
            DisplayHackThread.actOnSequencer(s -> s.stop());
            paused = true;
        }

        public void exit() {
            if (exceptionMode) {
                return;
            }
            stop0(false);
            DisplayHackThread.actOnSequencer(s -> s.close());
        }
    }

    public static boolean isPlaying() {
        if (DisplayHackThread.inst == null
                || DisplayHackThread.inst.seqr == null) {
            return false;
        }
        // we are paused or running (playing) when we have an inst and seqr
        return true;
    }
}
