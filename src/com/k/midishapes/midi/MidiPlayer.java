package com.k.midishapes.midi;

import javax.sound.midi.MidiSystem;
import javax.sound.midi.Sequence;
import javax.sound.midi.Sequencer;

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

    static class MidiPlay {
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
            DisplayHackThread.pause = repeat && normal
                    && DisplayHackThread.pause;
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
                    DisplayHackThread.inst.seqr.stop();
                    DisplayHackThread.inst.seqr.setMicrosecondPosition(0);
                    DisplayHackThread.abort = false;
                    DisplayHackThread.inst = null;
                }
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
        return DisplayHackThread.inst.isAlive();
    }

}
