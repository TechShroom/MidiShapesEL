package com.k.midishapes.midi;

import java.util.ArrayList;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Track;

import com.k.midishapes.midi.MidiUtils.EventCache;

import emergencylanding.k.imported.Sync;

public class SubHack implements Runnable {
    private int ourDIId = 0;

    public SubHack(int track) {
        this.track = track;
        ShortMessage smalloff = sm_alloff(track >= 16 ? 15 : track);
        ourDIId = MidiDisplayer.sendToInstrument(smalloff, -1);
        DisplayHackThread.recv.send(smalloff, -1);
    }

    static ShortMessage sm_alloff(int chan) {
        ShortMessage str = new ShortMessage();
        try {
            str.setMessage(MidiPlayer.CONTROL_CHANGE, chan,
                    MidiPlayer.ALL_NOTES_OFF, 0);
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
        if (DisplayHackThread.inst.tick > lastTick
                && ec.tick_to_index.get(DisplayHackThread.inst.tick) != null) {
            update(DisplayHackThread.inst.tick);
            lastTick = DisplayHackThread.inst.tick;
        }
    }

    public void update(long tick) {
        ArrayList<Integer> indexes = MidiUtils.tick2indexEC(tick, ec);
        for (Integer index : indexes) {
            MidiEvent me = t.get(index);
            MidiMessage m = me.getMessage();
            if (m instanceof ShortMessage) {
                MidiDisplayer.sendToInstrument((ShortMessage) m, ourDIId);
            }
            DisplayHackThread.recv.send(m, -1);
        }
    }

    @Override
    public void run() {
        while (!DisplayHackThread.running) {
            try {
                Thread.sleep(1);
            } catch (InterruptedException is) {
            }
        }
        while (DisplayHackThread.running && !DisplayHackThread.abort) {
            runUpdates();
            syncObj.sync(1000);
        }
    }

}