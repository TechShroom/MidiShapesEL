package com.k.midishapes.midi.custom;

import javax.sound.midi.MidiDevice.Info;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.ShortMessage;

public class CustomTesting {

    public static void main(String[] args) {
        Info[] midiInfo = MidiSystem.getMidiDeviceInfo();
        Info bass = null;
        for (Info i : midiInfo) {
            System.err.printf("%s from %s version %s (%s)\n", i.getName(),
                    i.getVendor(), i.getVersion(), i.getDescription());
            if (i.getName().contains("BASSMIDI Driver (port A)")) {
                bass = i;
            }
        }
        CustomReceiver cr = new CustomReceiver(bass.getName(), bass.getName());
        cr.send(new ShortMessage(), 0);
    }

}
