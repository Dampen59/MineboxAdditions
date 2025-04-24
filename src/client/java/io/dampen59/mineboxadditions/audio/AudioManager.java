package io.dampen59.mineboxadditions.audio;

import de.maxhenkel.opus4j.OpusDecoder;
import de.maxhenkel.opus4j.OpusEncoder;
import io.dampen59.mineboxadditions.state.AudioDeviceState;
import io.dampen59.mineboxadditions.state.State;

import javax.sound.sampled.*;
import java.util.HashMap;
import java.util.Map;

public class AudioManager {

    private final State modState;
    private TargetDataLine microphone;
    private SourceDataLine speaker;
    private final byte[] buffer = new byte[960 * 2];

    private OpusEncoder encoder = null;
    private OpusDecoder decoder = null;

    private final Map<String, OpusDecoder> decoders = new HashMap<>();

    private boolean isRecording = false;

    private AudioMixer mixer;

    public AudioManager(State modState) {
        this.modState = modState;
        this.modState.setAudioManager(this);

        try {
            this.encoder = new OpusEncoder(48000, 1, OpusEncoder.Application.VOIP);
            this.decoder = new OpusDecoder(48000, 1);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void openMicrophone() throws LineUnavailableException {
        if (microphone != null && microphone.isOpen()) {
            microphone.stop();
            microphone.flush();
            microphone.close();
        }

        AudioFormat format = new AudioFormat(48000.0f, 16, 1, true, false);
        DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);

        Mixer.Info selectedMic = io.dampen59.mineboxadditions.state.AudioDeviceState.selectedInput;
        Mixer mixer = selectedMic != null ? AudioSystem.getMixer(selectedMic) : AudioSystem.getMixer(null);

        microphone = (TargetDataLine) mixer.getLine(info);
        microphone.open(format);
        microphone.start();

        isRecording = true;
        startMicrophoneCapture();
    }


    public void openSpeaker() throws LineUnavailableException {
        if (speaker != null && speaker.isOpen()) {
            speaker.stop();
            speaker.flush();
            speaker.close();
        }

        AudioFormat format = new AudioFormat(48000.0f, 16, 2, true, false);
        DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);

        Mixer.Info selectedOutput = io.dampen59.mineboxadditions.state.AudioDeviceState.selectedOutput;
        Mixer mixer = selectedOutput != null ? AudioSystem.getMixer(selectedOutput) : AudioSystem.getMixer(null);

        speaker = (SourceDataLine) mixer.getLine(info);
        speaker.open(format);
        speaker.start();
    }

    public void openMicrophone(Mixer mixer) throws LineUnavailableException {
        if (microphone != null && microphone.isOpen()) {
            microphone.stop();
            microphone.flush();
            microphone.close();
        }

        AudioFormat format = new AudioFormat(48000.0f, 16, 1, true, false);
        microphone = (TargetDataLine) mixer.getLine(new DataLine.Info(TargetDataLine.class, format));
        microphone.open(format);
        microphone.start();

        isRecording = true;
        startMicrophoneCapture();
    }

    public void openSpeaker(Mixer mixer) throws LineUnavailableException {
        if (speaker != null && speaker.isOpen()) {
            speaker.stop();
            speaker.flush();
            speaker.close();
        }

        AudioFormat format = new AudioFormat(48000.0f, 16, 2, true, false);
        speaker = (SourceDataLine) mixer.getLine(new DataLine.Info(SourceDataLine.class, format));
        speaker.open(format);
        speaker.start();

        this.mixer = new AudioMixer(this.speaker);
    }


    public OpusDecoder getDecoder() {
        return this.decoder;
    }

    public Map<String, OpusDecoder> getDecoders() {
        return this.decoders;
    }

    public OpusEncoder getEncoder() {
        return this.encoder;
    }

    public SourceDataLine getSpeaker() {
        return this.speaker;
    }

    public TargetDataLine getMicrophone() {
        return this.microphone;
    }

    public AudioMixer getMixer() {
        return this.mixer;
    }

    public void closeMicrophoneAndSpeaker() {

        if (mixer != null) {
            mixer.stop();
            mixer = null;
        }

        speaker.flush();
        speaker.stop();
        speaker.close();

        isRecording = false;

        microphone.flush();
        microphone.stop();
        microphone.close();
    }

    private void startMicrophoneCapture() {
        new Thread(() -> {
            try {
                byte[] inputBuffer = new byte[2048];
                byte[] pcmBuffer = new byte[1920];
                int pcmOffset = 0;

                float linearGain = (float) Math.pow(10, AudioDeviceState.micGainDb / 20.0);

                long frameDurationNs = 20_000_000L;
                long nextSendTime = System.nanoTime();

                while (isRecording) {
                    int bytesRead = microphone.read(inputBuffer, 0, inputBuffer.length);
                    int consumed = 0;

                    while (consumed < bytesRead) {
                        int remaining = 1920 - pcmOffset;
                        int toCopy = Math.min(remaining, bytesRead - consumed);

                        System.arraycopy(inputBuffer, consumed, pcmBuffer, pcmOffset, toCopy);
                        pcmOffset += toCopy;
                        consumed += toCopy;

                        if (pcmOffset == 1920) {
                            short[] rawAudio = new short[960];
                            for (int i = 0; i < 960; i++) {
                                short sample = (short) ((pcmBuffer[2 * i + 1] << 8) | (pcmBuffer[2 * i] & 0xFF));
                                int amplified = Math.round(sample * linearGain);
                                rawAudio[i] = (short) Math.max(Short.MIN_VALUE, Math.min(Short.MAX_VALUE, amplified));
                            }

                            byte[] encoded = encoder.encode(rawAudio);
                            modState.getSocket().emit("C2SAudioData", encoded);

                            pcmOffset = 0;

                            long now = System.nanoTime();
                            long sleepNs = nextSendTime - now;
                            if (sleepNs > 0) {
                                Thread.sleep(sleepNs / 1_000_000, (int) (sleepNs % 1_000_000));
                            }

                            nextSendTime += frameDurationNs;
                        }
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (microphone != null) {
                    microphone.flush();
                    microphone.drain();
                }
            }
        }).start();
    }


}
