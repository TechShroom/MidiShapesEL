package com.k.midishapes.midi;

import java.io.File;
import java.io.IOException;

import javax.sound.midi.Instrument;
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

import g.com.sun.media.sound.MidiDeviceReceiver;
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

    public static Sequence decodedSequence()
            throws InvalidMidiDataException, IOException {
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
            Info[] info = MidiSystem.getMidiDeviceInfo();
            for (Info i : info) {
                System.err.println("Checking info " + i + " v" + i.getVersion()
                        + ":" + i.getDescription());
                if (i.getName().contains("Gervill")) {
                    synth = (Synthesizer) MidiSystem.getMidiDevice(i);
                    synth.open();
                    break;
                }
            }
        } catch (MidiUnavailableException e1) {
            e1.printStackTrace();
        }
        Soundbank sf2bank = synth.getDefaultSoundbank();
        synth.unloadAllInstruments(sf2bank);
        if (ProgramProps.hasKey("soundbank")) {
            System.err.println("Opening soundbank...");
            try {
                sf2bank = MidiSystem.getSoundbank(
                        new File(ProgramProps.getProperty("soundbank")));
            } catch (IOException | InvalidMidiDataException e) {
                e.printStackTrace();
            }
            System.err.println("Done loading soundbank.");
        }
        if (sf2bank != null) {
            System.err.println("Injecting instruments...");
            for (Instrument i : sf2bank.getInstruments()) {
                try {
                    if (!synth.loadInstrument(i)) {
                        throw new RuntimeException("synth returned false");
                    }
                } catch (Exception e) {
                    System.err.println("Couldn't load instrument: " + i);
                    e.printStackTrace();
                }
            }
            System.err.println("Done injecting instruments.");
        }
        return synth;
    }

    public static boolean user_recv_req() {
        Info[] midiInfo = MidiSystem.getMidiDeviceInfo();
        Info def = userRecvI;
        Info before = def;
        if (def == null) {
            def = midiInfo[0];
        }
        userRecvI = (Info) JOptionPane.showInputDialog(null,
                "Choose a MidiDevice", "Choose a MidiDevice",
                JOptionPane.QUESTION_MESSAGE, null, midiInfo, def);
        if (userRecvI.getName().contains("Gervill")) {
            userRecvI = null;
        }
        return userRecvI != before;
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
                    "Receiver not opened, and not openable: "
                            + recv.getClass());
        }
        return recv;
    }

    public static boolean recv_not_open(Receiver r) {
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
