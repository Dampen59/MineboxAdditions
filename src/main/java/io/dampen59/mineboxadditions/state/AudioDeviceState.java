package io.dampen59.mineboxadditions.state;

import javax.sound.sampled.Mixer;

public class AudioDeviceState {
    public static Mixer.Info selectedInput = null;
    public static Mixer.Info selectedOutput = null;
    public static float micGainDb = 0.0f;
    public static float speakerVolumeMultiplier = 1.0f;
}
