package com.k.midishapes.midi;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import javax.management.RuntimeErrorException;
import javax.sound.midi.ShortMessage;

import com.k.midishapes.interfacing.DisplayableInstrument;

public class MidiDisplayer {
    static HashMap<Integer, DisplayableInstrument<?>> itod = new HashMap<Integer, DisplayableInstrument<?>>();
    private static int nextAvaliable = 0;
    private static final Object lk = new Object();
    private static ArrayList<Class<? extends DisplayableInstrument<?>>> displayableClasses = new ArrayList<Class<? extends DisplayableInstrument<?>>>();
    private static ArrayList<Constructor<? extends DisplayableInstrument<?>>> displayableConstrs = new ArrayList<Constructor<? extends DisplayableInstrument<?>>>();

    private static Class<? extends DisplayableInstrument<?>> currentClass = null;

    public static void init() {
        displayableClasses.add(DisplayableInstrumentImpl.class);

        // register here later

        ArrayList<Class<? extends DisplayableInstrument<?>>> rem = new ArrayList<Class<? extends DisplayableInstrument<?>>>();
        for (Class<? extends DisplayableInstrument<?>> c : displayableClasses) {
            try {
                Constructor<? extends DisplayableInstrument<?>> constr = c
                        .getDeclaredConstructor(int.class);
                displayableConstrs.add(constr);
            } catch (NoSuchMethodException e) {
                System.err
                        .println("Invalid class formatting: id parameter required @ "
                                + c.getName());
                rem.add(c);
            } catch (SecurityException e) {
                throw new IllegalAccessError(
                        "Security Manager prevented reflection");
            }
        }
        displayableClasses.removeAll(rem);
        if (displayableClasses.size() == 0) {
            throw new InternalError("No DIs?");
        }
        currentClass = displayableClasses.get(0);
    }

    public static void display() {
        synchronized (lk) {
            Collection<DisplayableInstrument<?>> vals = itod.values();
            for (DisplayableInstrument<?> di : vals) {
                di.draw();
            }
        }
    }

    public static int sendToInstrument(ShortMessage sm, int id) {
        DisplayableInstrument<?> inst = itod.get(id);
        if (inst == null) {
            synchronized (lk) {
                inst = createCurrentDI();
                itod.put(inst.getID(), inst);
            }
        }
        if (sm.getCommand() == MidiPlayer.NOTE_ON) {
            int vol = sm.getData2();
            if (vol == 0) {
                // some midis like to use note on + vol=0 for note off
                inst.stopNote(sm.getData1());
            } else {
                inst.playNote(sm.getData1());
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

    private static DisplayableInstrument<?> createCurrentDI() {
        int id = nextAvaliable++;
        try {
            try {
                return displayableConstrs.get(
                        displayableClasses.indexOf(currentClass)).newInstance(
                        id);
            } catch (InvocationTargetException e) {
                throw e.getCause();
            } catch (Exception e) {
                throw e;
            }
        } catch (Throwable t) {
            throw (t instanceof Error) ? new RuntimeErrorException((Error) t)
                    : new RuntimeException(t);
        }
    }

    public static void exit() {
        synchronized (lk) {
            itod.clear();
        }
    }

    public static void stop(boolean keep) {
        synchronized (lk) {
            for (DisplayableInstrument<?> di : itod.values()) {
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
