package io.dampen59.mineboxadditions.audio;

import javax.sound.sampled.SourceDataLine;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class AudioMixer {
    private final SourceDataLine speaker;
    Map<String, Queue<short[]>> playerQueues = new ConcurrentHashMap<>();
    Map<String, Queue<short[]>> playerStereoQueues = new ConcurrentHashMap<>();

    private final int frameSize = 960;
    private volatile boolean running = true;

    public AudioMixer(SourceDataLine speaker) {
        this.speaker = speaker;
        Thread mixingThread = new Thread(this::mixLoop);
        mixingThread.start();
    }

    public void stop() {
        running = false;
    }

    public void push(String playerName, short[] decodedPcm) {
        if (decodedPcm == null || decodedPcm.length != frameSize) return;
        playerQueues.computeIfAbsent(playerName, k -> new ConcurrentLinkedQueue<>()).add(decodedPcm);
    }

    public void pushStereo(String playerName, short[] stereoPcm) {
        if (stereoPcm == null || stereoPcm.length != 1920) return;

        short[] copy = new short[1920];
        System.arraycopy(stereoPcm, 0, copy, 0, 1920);

        playerStereoQueues.computeIfAbsent(playerName, k -> new ConcurrentLinkedQueue<>()).add(copy);
    }


    private void mixLoop() {
        byte[] outputBuffer = new byte[frameSize * 2 * 2];
        long frameDurationNs = 20_000_000L;
        long nextFrameTime = System.nanoTime();

        while (running) {
            long now = System.nanoTime();
            if (now < nextFrameTime) {
                try {
                    Thread.sleep((nextFrameTime - now) / 1_000_000, (int) ((nextFrameTime - now) % 1_000_000));
                } catch (InterruptedException ignored) {
                }
                continue;
            }

            float[] mixedLeft = new float[frameSize];
            float[] mixedRight = new float[frameSize];

            for (Map.Entry<String, Queue<short[]>> entry : playerQueues.entrySet()) {
                Queue<short[]> queue = entry.getValue();
                short[] monoFrame = queue.poll();
                if (monoFrame == null || monoFrame.length != frameSize) continue;

                for (int i = 0; i < frameSize; i++) {
                    float sample = monoFrame[i] / 32768f;
                    mixedLeft[i] += sample;
                    mixedRight[i] += sample;
                }
            }

            for (Map.Entry<String, Queue<short[]>> entry : playerStereoQueues.entrySet()) {
                Queue<short[]> queue = entry.getValue();
                short[] stereoFrame = queue.poll();
                if (stereoFrame == null || stereoFrame.length != frameSize * 2) continue;

                for (int i = 0; i < frameSize; i++) {
                    float leftSample = stereoFrame[i * 2] / 32768f;
                    float rightSample = stereoFrame[i * 2 + 1] / 32768f;

                    mixedLeft[i] += leftSample;
                    mixedRight[i] += rightSample;
                }
            }


            int streamCount = playerQueues.size() + playerStereoQueues.size();
            float normalization = streamCount > 1 ? 1.0f / streamCount : 1.0f;

            for (int i = 0; i < frameSize; i++) {
                float left = Math.max(-1f, Math.min(1f, mixedLeft[i] * normalization));
                float right = Math.max(-1f, Math.min(1f, mixedRight[i] * normalization));

                short sLeft = (short) (left * 32767);
                short sRight = (short) (right * 32767);

                int index = i * 4;
                outputBuffer[index] = (byte) (sLeft & 0xFF);
                outputBuffer[index + 1] = (byte) ((sLeft >> 8) & 0xFF);
                outputBuffer[index + 2] = (byte) (sRight & 0xFF);
                outputBuffer[index + 3] = (byte) ((sRight >> 8) & 0xFF);
            }


            synchronized (speaker) {
                speaker.write(outputBuffer, 0, outputBuffer.length);
            }

            nextFrameTime += frameDurationNs;
        }
    }
}
