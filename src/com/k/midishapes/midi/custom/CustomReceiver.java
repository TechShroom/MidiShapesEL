package com.k.midishapes.midi.custom;

import java.util.ArrayList;
import java.util.List;

import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiDevice.Info;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Receiver;
import javax.sound.midi.Transmitter;

public class CustomReceiver implements Receiver {

    private List<MidiMessage> m;
    private MidiDevice receivedevice;
    private MidiDevice transmitdevice;
    private Receiver receiver;
    private Transmitter transmitter;

    public CustomReceiver(String tname, String rname) {
        transmitdevice = getMidiDevice(tname, false);
        receivedevice = getMidiDevice(rname, true);
        m = new ArrayList<MidiMessage>();
        try {
            receivedevice.open();
            transmitdevice.open();
            this.receiver = receivedevice.getReceiver();
            this.transmitter = transmitdevice.getTransmitter();
            this.transmitter.setReceiver(this);
        } catch (MidiUnavailableException e) {
            close();
            e.printStackTrace();
        }
    }

    private MidiDevice getMidiDevice(String s, boolean recv) {
        Info[] infos = MidiSystem.getMidiDeviceInfo();
        for (int i = 0; i < infos.length; i++) {
            if (infos[i].getName().equals(s)) {
                try {
                    MidiDevice d = MidiSystem.getMidiDevice(infos[i]);
                    if (d.getMaxReceivers() != 0 && recv) {
                        System.out.println(infos[i].getName());
                        System.out.println(d.getMaxReceivers());
                        return d;
                    } else if (d.getMaxTransmitters() != 0 && !recv) {
                        System.out.println(infos[i].getName());
                        System.out.println(d.getMaxTransmitters());
                        return d;
                    }
                } catch (MidiUnavailableException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    @Override
    public void close() {
        if (receiver != null)
            receiver.close();
        if (transmitter != null)
            transmitter.close();
        if (transmitdevice != null)
            transmitdevice.close();
        if (receivedevice != null)
            receivedevice.close();
    }

    @Override
    public void send(MidiMessage msg, long timestamp) {
        // Send the message to the receiver
        receiver.send(msg, timestamp);
    }

    public void sendSingleMessage(MidiMessage msg, long timeStamp) {
        receiver.send(msg, timeStamp);
    }

    public void sendAll() {
        for (MidiMessage mm : m)
            receiver.send(mm, -1);
    }

    public void add(MidiMessage msg) {
        m.add(msg);
    }

    public void remove(MidiMessage msg) {
        m.remove(msg);
    }
}