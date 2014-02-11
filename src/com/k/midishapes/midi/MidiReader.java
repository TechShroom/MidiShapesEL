package com.k.midishapes.midi;

import java.io.File;
import java.io.IOException;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiDevice.Info;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Sequence;
import javax.sound.midi.Soundbank;
import javax.sound.midi.Synthesizer;

import com.k.midishapes.CommandLine;

public class MidiReader {
    public static File midi = null;
    public static Synthesizer synth = null;

    public static void init() {
        midi = new File(CommandLine.getProperty("file"));
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
        if (CommandLine.hasKey("soundbank")) {
            try {
                sf2bank = MidiSystem.getSoundbank(new File(CommandLine
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
            }
        }
        if (CommandLine.hasKey("repeat")) {
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
        synth = null;
    }

}
