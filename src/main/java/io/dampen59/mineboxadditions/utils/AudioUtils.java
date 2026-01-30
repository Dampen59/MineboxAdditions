package io.dampen59.mineboxadditions.utils;

import de.maxhenkel.opus4j.OpusDecoder;
import io.dampen59.mineboxadditions.MineboxAdditions;
import io.dampen59.mineboxadditions.features.voicechat.AudioMixer;
import io.dampen59.mineboxadditions.features.voicechat.AudioDeviceState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;

import javax.sound.sampled.*;
import java.util.Arrays;

public class AudioUtils {

    private static final double MAX_PROXIMITY_DISTANCE = 48.0;

    public static void playAudio(
            OpusDecoder decoder,
            byte[] audioData,
            String playerName
    ) {
        if (audioData == null || audioData.length == 0 || decoder == null) return;

        short[] decodedMono;
        try {
            decodedMono = decoder.decode(audioData);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        if (decodedMono == null || decodedMono.length != 960) return;

        float gain = AudioDeviceState.speakerVolumeMultiplier;

        for (int i = 0; i < decodedMono.length; i++) {
            float amplified = decodedMono[i] * gain;
            decodedMono[i] = (short) Math.max(Short.MIN_VALUE, Math.min(Short.MAX_VALUE, amplified));
        }

        AudioMixer mixer = MineboxAdditions.INSTANCE.state.getAudioManager().getMixer();
        if (mixer != null) {
            mixer.push(playerName, decodedMono);
        }
    }

    public static void playProximityAudio(
            OpusDecoder decoder,
            byte[] audioData,
            float proximityVolume,
            PlayerEntity sourcePlayer,
            String playerName
    ) {
        if (audioData == null || audioData.length == 0 || decoder == null) return;

        short[] decodedMono;
        try {
            decodedMono = decoder.decode(audioData);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        if (decodedMono == null || decodedMono.length != 960) return;

        float totalGain = proximityVolume * AudioDeviceState.speakerVolumeMultiplier;
        float[] stereoVolumes = AudioUtils.computeSurroundVolumes(sourcePlayer); // [left, right]

        short[] stereoPcm = new short[960 * 2]; // interleaved stereo

        for (int i = 0; i < 960; i++) {
            float sample = decodedMono[i] * totalGain;

            float left = sample * stereoVolumes[0];
            float right = sample * stereoVolumes[1];

            stereoPcm[i * 2] = (short) Math.max(Short.MIN_VALUE, Math.min(Short.MAX_VALUE, left));
            stereoPcm[i * 2 + 1] = (short) Math.max(Short.MIN_VALUE, Math.min(Short.MAX_VALUE, right));
        }

        AudioMixer mixer = MineboxAdditions.INSTANCE.state.getAudioManager().getMixer();
        if (mixer != null) {
            mixer.pushStereo(playerName, stereoPcm);
        }
    }

    public static short[] convertMonoToStereo(short[] monoData) {
        short[] stereoData = new short[monoData.length * 2];
        for (int i = 0; i < monoData.length; i++) {
            stereoData[2 * i] = monoData[i];
            stereoData[2 * i + 1] = monoData[i];
        }
        return stereoData;
    }


    public static PlayerEntity getNearbyPlayer(String playerName) {
        MinecraftClient client = MinecraftClient.getInstance();

        if (client.player == null || client.world == null) {
            return null;
        }

        Vec3d currentPosition = client.player.getPos();

        for (PlayerEntity player : client.world.getPlayers()) {

            if (player == client.player) {
                continue;
            }

            if (player.getName().getString().equalsIgnoreCase(playerName)) {
                double distance = currentPosition.distanceTo(player.getPos());
                if (distance <= MAX_PROXIMITY_DISTANCE) {
                    return player;
                }
            }

        }

        return null;
    }

    public static float computeVolumeMultiplier(PlayerEntity prmPlayer) {
        MinecraftClient client = MinecraftClient.getInstance();

        if (client.player == null || prmPlayer == null) {
            return 0.0f;
        }

        Vec3d currentPosition = client.player.getPos();
        Vec3d otherPosition = prmPlayer.getPos();

        double distance = currentPosition.distanceTo(otherPosition);

        if (distance <= 16.0) {
            return 1.0f;
        } else if (distance >= 48.0) {
            return 0.0f;
        } else {
            double fadeDistance = distance - 16.0;
            double fadeRange = 48.0 - 16.0;
            float volume = (float) (1.0 - (fadeDistance / fadeRange));
            return Math.max(0.0f, Math.min(1.0f, volume));
        }
    }

    public static float[] computeSurroundVolumes(PlayerEntity prmSourcePlayer) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || prmSourcePlayer == null) {
            return new float[]{1.0f, 1.0f};
        }

        Vec3d currentPosition = client.player.getPos();
        Vec3d otherPosition = prmSourcePlayer.getPos();

        double dx = otherPosition.x - currentPosition.x;
        double dz = otherPosition.z - currentPosition.z;

        double angleToTarget = Math.toDegrees(Math.atan2(dz, dx));
        float playerYaw = client.player.getYaw();
        double relativeAngle = angleToTarget - playerYaw;

        relativeAngle = (relativeAngle + 360) % 360;
        if (relativeAngle > 180) {
            relativeAngle -= 360;
        }

        float leftVolume = (float) (Math.cos(Math.toRadians(relativeAngle)) * 0.707);
        float rightVolume = (float) (Math.sin(Math.toRadians(relativeAngle)) * 0.707);

        leftVolume = Math.max(0.0f, Math.min(1.0f, Math.abs(leftVolume)));
        rightVolume = Math.max(0.0f, Math.min(1.0f, Math.abs(rightVolume)));

        if (Math.abs(relativeAngle) > 90) {
            float rearReduction = 0.8f;
            leftVolume *= rearReduction;
            rightVolume *= rearReduction;
        }

        return new float[]{leftVolume, rightVolume};
    }

    public static Mixer.Info[] getOutputMixers() {
        return Arrays.stream(AudioSystem.getMixerInfo())
                .filter(info -> {
                    try {
                        Mixer mixer = AudioSystem.getMixer(info);
                        mixer.open();
                        return mixer.isLineSupported(new DataLine.Info(SourceDataLine.class, new AudioFormat(48000f, 16, 2, true, false)));
                    } catch (Exception e) {
                        return false;
                    }
                })
                .toArray(Mixer.Info[]::new);
    }

    public static Mixer.Info[] getInputMixers() {
        return Arrays.stream(AudioSystem.getMixerInfo())
                .filter(info -> {
                    try {
                        Mixer mixer = AudioSystem.getMixer(info);
                        mixer.open();
                        return mixer.isLineSupported(new DataLine.Info(TargetDataLine.class, new AudioFormat(48000f, 16, 1, true, false)));
                    } catch (Exception e) {
                        return false;
                    }
                })
                .toArray(Mixer.Info[]::new);
    }

    public static Mixer.Info getMixerByName(String name, boolean input) {
        Mixer.Info[] infos = input ? getInputMixers() : getOutputMixers();
        for (Mixer.Info info : infos) {
            if (info.getName().equalsIgnoreCase(name)) return info;
        }
        return infos.length > 0 ? infos[0] : null;
    }

    public static Mixer getMixerByName(String name) {
        if (name == null) return null;

        for (Mixer.Info info : AudioSystem.getMixerInfo()) {
            if (info.getName().equalsIgnoreCase(name)) {
                return AudioSystem.getMixer(info);
            }
        }

        return null;
    }


}
