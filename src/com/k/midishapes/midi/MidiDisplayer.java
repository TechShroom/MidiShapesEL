package com.k.midishapes.midi;

import java.util.Collection;
import java.util.HashMap;

import javax.sound.midi.ShortMessage;

public class MidiDisplayer {
    static HashMap<Integer, DisplayableInstrument> itod = new HashMap<Integer, DisplayableInstrument>();
    private static int nextAvaliable = 0;
    private static final Object lk = new Object();

    public static void init() {
    }

    public static void display() {
        synchronized (lk) {
            Collection<DisplayableInstrument> vals = itod.values();
            for (DisplayableInstrument di : vals) {
                di.draw();
            }
        }
    }

    public static int sendToInstrument(ShortMessage sm, int id) {
        DisplayableInstrument inst = itod.get(id);
        if (inst == null) {
            synchronized (lk) {
                inst = new DisplayableInstrument(nextAvaliable++);
                itod.put(inst.getID(), inst);
            }
        }
        if (sm.getCommand() == MidiPlayer.NOTE_ON) {
            int vol = sm.getData2();
            if (vol == 0) {
                // some midis like to use note on + vol=0 for note off
                inst.stopNote(sm.getData1());
            } else {
                inst.startNote(sm.getData1());
            }
        } else {
            if (sm.getCommand() == MidiPlayer.CONTROL_CHANGE
                    && (sm.getData1() == MidiPlayer.ALL_NOTES_OFF
                            || sm.getData1() == MidiPlayer.ALL_SOUND_OFF
                            || sm.getData1() == MidiPlayer.MONO
                            || sm.getData1() == MidiPlayer.OMNI_OFF
                            || sm.getData1() == MidiPlayer.OMNI_ON || sm
                            .getData1() == MidiPlayer.POLY)) {
                // All note off?
                inst.stopAll();
            } else if (sm.getCommand() == MidiPlayer.NOTE_OFF) {
                inst.stopNote(sm.getData1());
            }
        }
        return inst.getID();
    }

    public static void exit() {
        synchronized (lk) {
            itod.clear();
        }
    }

    public static void stop(boolean keep) {
        synchronized (lk) {
            for (DisplayableInstrument di : itod.values()) {
                di.stopAll();
            }
            if (keep) {
                return;
            }
            itod.clear();
            nextAvaliable = 0;
        }
    }

}
