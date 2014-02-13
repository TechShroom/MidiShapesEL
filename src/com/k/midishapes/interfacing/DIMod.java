package com.k.midishapes.interfacing;

import emergencylanding.k.exst.mods.IMod;

/**
 * An {@link IMod} that provides a {@link DisplayableInstrument} class.
 * 
 * @author Kenzie Togami
 */
public interface DIMod extends IMod {

    /**
     * Gets the {@link DisplayableInstrument} class that the mod adds.
     * 
     * @return a extension of the DisplayableInstrument class that the
     *         underlying mod provides.
     */
    public Class<? extends DisplayableInstrument<?>> getDIClass();

}
