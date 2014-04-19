package com.k.midishapes.midi.custom;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.Receiver;
import javax.sound.midi.ShortMessage;

import com.k.midishapes.interfacing.DisplayableInstrument;
import com.k.midishapes.midi.MidiDisplayer;
import com.k.midishapes.midi.MidiPlayer;

public class ChainedReceiver implements Receiver {
    /**
     * Channel -> {@link DisplayableInstrument} mappings
     */
    private static final int[] mapping = new int[16];
    static {
        for (int i = 0; i < mapping.length; i++) {
            mapping[i] = MidiDisplayer.sendToInstrument(sm_alloff(i), -1);
        }
    }

    public static void init() {
        // no-op, just to do static things
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

    private Receiver chain = null;

    public ChainedReceiver(Receiver chain) {
        this.chain = chain;
    }

    @Override
    public void send(MidiMessage message, long timeStamp) {
        if (message instanceof ShortMessage) {
            ShortMessage sm = (ShortMessage) message;
            int id = mapping[sm.getChannel()];
            // System.err.println(id + " mapped from " + sm.getChannel());
            // allow for rebinding
            mapping[sm.getChannel()] = MidiDisplayer.sendToInstrument(sm, id);
        }
        chain.send(message, timeStamp);
    }

    @Override
    public void close() {
        chain.close();
    }

}
