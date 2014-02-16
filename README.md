# MidiShapes #

#### A midi player that allows for different visual styling via the mods system in EmergencyLanding ####

To create a new visual style, simply implement the DIMod interface and provide a DisplayableInstrument (or DefaultDisplayableInstrument) implementation. Then package it in a jar and add a file under the META-INF/services/ folder in the jar called 'emergencylanding.k.exst.mods.IMod', and add a line with the [full binary name](http://docs.oracle.com/javase/7/docs/api/java/lang/ClassLoader.html#name) of your DIMod implementor. Then create a folder named 'mods' in the launch directory and place the jar inside.

Note: You cannot yet change the visual style. Implementation of this is being worked on.