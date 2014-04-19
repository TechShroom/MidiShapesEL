package com.k.midishapes.midi;

import g.com.sun.media.sound.MidiDeviceReceiver;

import java.io.File;
import java.io.IOException;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiDevice.Info;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Receiver;
import javax.sound.midi.Sequence;
import javax.sound.midi.Soundbank;
import javax.sound.midi.Synthesizer;
import javax.swing.JOptionPane;

import com.k.midishapes.midi.custom.CustomReceiver;

import k.core.util.core.Helper.ProgramProps;

public class MidiReader {
    public static File midi = null;
    private static Synthesizer synth;
    private static Info userRecvI = null;

    public static void init() {
        midi = new File(ProgramProps.getProperty("file"));
        if (ProgramProps.hasKey("repeat")) {
            MidiPlayer.repeat = true;
        }
    }

    public static Sequence decodedSequence() throws InvalidMidiDataException,
            IOException {
        if (midi == null || midi.equals(""))
            return new Sequence(Sequence.PPQ, 4);
        return MidiSystem.getSequence(midi);
    }

    public static void exit() {
        midi = null;
        if (synth != null && synth.isOpen()) {
            synth.close();
            synth = null;
        }
    }

    public static Synthesizer openSynth() {
        try {
            synth = MidiSystem.getSynthesizer();
            Info[] info = MidiSystem.getMidiDeviceInfo();
            if (!synth.getDeviceInfo().getName().contains("Gervill")) {
                for (Info i : info) {
                    System.err.println("Checking info " + i + " v"
                            + i.getVersion() + ":" + i.getDescription());
                    if (i.getName().contains("Gervill")) {
                        synth = (Synthesizer) MidiSystem.getMidiDevice(i);
                    }
                }
            }
        } catch (MidiUnavailableException e1) {
            e1.printStackTrace();
        }
        Soundbank sf2bank = synth.getDefaultSoundbank();
        if (ProgramProps.hasKey("soundbank")) {
            try {
                sf2bank = MidiSystem.getSoundbank(new File(ProgramProps
                        .getProperty("soundbank")));
            } catch (InvalidMidiDataException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (sf2bank != null) {
            try {
                synth.loadAllInstruments(sf2bank);
            } catch (Exception e) {
                System.err.println("Couldn't load some instruments: ");
                e.printStackTrace();
            }
        }
        return synth;
    }

    public static void user_recv_req() {
        Info[] midiInfo = MidiSystem.getMidiDeviceInfo();
        for (Info i : midiInfo) {
            System.err.printf("%s from %s version %s (%s)\n", i.getName(),
                    i.getVendor(), i.getVersion(), i.getDescription());
        }
        Info def = userRecvI;
        if (def == null) {
            def = midiInfo[0];
        }
        userRecvI = (Info) JOptionPane.showInputDialog(null,
                "Choose a MidiDevice", "Choose a MidiDevice",
                JOptionPane.QUESTION_MESSAGE, null, midiInfo, def);
    }

    public static Receiver openReceiver() throws MidiUnavailableException {
        Receiver recv = null;
        if (userRecvI != null) {
            recv = new CustomReceiver(userRecvI.getName(), userRecvI.getName());
        }
        if (recv == null && synth != null) {
            recv = synth.getReceiver();
        }
        if (recv_not_open(recv) && recv instanceof MidiDeviceReceiver) {
            ((MidiDeviceReceiver) recv).getMidiDevice().open();
        } else if (recv_not_open(recv)) {
            throw new IllegalStateException(
                    "Receiver not opened, and not openable: " + recv.getClass());
        }
        return recv;
    }

    private static boolean recv_not_open(Receiver r) {
        try {
            r.send(MidiUtils.allNotesOff(0), -1);
            return false;
        } catch (IllegalStateException ise) {
            return true;
        }
    }

    public static void closeSynth(Synthesizer s) {
        s.close();
        if (s == synth) {
            synth = null;
        }
    }
}
