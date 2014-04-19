package com.k.midishapes.midi;

import java.util.List;

import javax.sound.midi.MidiSystem;
import javax.sound.midi.Receiver;
import javax.sound.midi.Sequence;
import javax.sound.midi.Sequencer;
import javax.sound.midi.Transmitter;

import com.k.midishapes.midi.MidiUtils.TempoCache;
import com.k.midishapes.midi.custom.ChainedReceiver;
import com.sun.media.sound.AutoConnectSequencer;

public class DisplayHackThread extends Thread {
    public TempoCache tc;
    public long tick;
    public static boolean running, abort;
    protected static Receiver recv = null;
    private Sequence s = null;
    private Sequencer seqr = null;
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
        try {
            if (recv != null) {
                recv.close();
            }
            MidiReader.openSynth();
            recv = MidiReader.openReceiver();
            // wrap the receiver so we can pull data
            recv = new ChainedReceiver(recv);
            seqr = MidiSystem.getSequencer();
            List<Transmitter> transers = seqr.getTransmitters();
            for (Transmitter t : transers) {
                t.setReceiver(recv);
            }
            // abide by java things
            if (seqr instanceof AutoConnectSequencer) {
                ((AutoConnectSequencer) seqr).setAutoConnect(recv);
            }
            if (!seqr.isOpen()) {
                seqr.open();
            }
            seqr.setSequence(s);
        } catch (Exception e1) {
            e1.printStackTrace();
            abort = true;
        }
        if (abort) {
            return;
        }
    }

    @Override
    public void run() {
        seqr.start();
    }

    public static void repeat() {
        inst.seqr.setMicrosecondPosition(0);
    }
}