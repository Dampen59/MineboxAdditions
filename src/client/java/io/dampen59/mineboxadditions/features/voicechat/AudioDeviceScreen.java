package io.dampen59.mineboxadditions.features.voicechat;

import io.dampen59.mineboxadditions.MineboxAdditions;
import io.dampen59.mineboxadditions.MineboxAdditionConfig;
import io.dampen59.mineboxadditions.features.voicechat.widgets.MicGainSlider;
import io.dampen59.mineboxadditions.features.voicechat.widgets.VolumeMultiplierSlider;
import io.dampen59.mineboxadditions.state.AudioDeviceState;
import io.dampen59.mineboxadditions.utils.AudioUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.CyclingButtonWidget;
import net.minecraft.text.Text;

import javax.sound.sampled.*;
import java.util.Arrays;
import java.util.List;

public class AudioDeviceScreen extends Screen {

    private static final int WIDGET_WIDTH = 256;
    private static final float SMOOTHING_SPEED = 0.2f;

    private TargetDataLine micTestLine;
    private Thread micThread;
    private boolean micRunning = false;

    private volatile float currentMicLevel = 0f;
    private volatile float currentMicDbFS = -100f;
    private float smoothedMicLevel = 0f;

    private int micBarY = 0;

    public AudioDeviceScreen() {
        super(Text.literal("Select Audio Devices"));
    }

    @Override
    public void tick() {
        super.tick();
        smoothedMicLevel += (currentMicLevel - smoothedMicLevel) * SMOOTHING_SPEED;
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;
        int currentY = 30;

        // Input
        drawGroupTitle("🎤 Input Device", centerX, currentY);
        currentY += 25;
        currentY = addDeviceSelector(centerX, currentY, AudioUtils.getInputMixers(), true);

        this.addDrawableChild(new MicGainSlider(centerX - WIDGET_WIDTH / 2, currentY, WIDGET_WIDTH, 20, AudioDeviceState.micGainDb));
        currentY += 30;

        micBarY = currentY;
        currentY += 40;

        // Output
        drawGroupTitle("🔊 Output Device", centerX, currentY);
        currentY += 25;
        currentY = addDeviceSelector(centerX, currentY, AudioUtils.getOutputMixers(), false);

        // Volume Multiplier Slider
        this.addDrawableChild(new VolumeMultiplierSlider(centerX - WIDGET_WIDTH / 2, currentY, WIDGET_WIDTH, 20, AudioDeviceState.speakerVolumeMultiplier));
        currentY += 25;

        // "Test Speaker" Button
        this.addDrawableChild(ButtonWidget.builder(Text.literal("Test Speaker"), b -> playTestTone())
                .position(centerX - WIDGET_WIDTH / 2, currentY)
                .size(WIDGET_WIDTH, 20)
                .build());
        currentY += 25;


        // Done
        this.addDrawableChild(ButtonWidget.builder(Text.literal("Done"), b -> {
            stopMicThread();
            MinecraftClient.getInstance().setScreen(null);
        }).position(centerX - WIDGET_WIDTH / 2, currentY + 20).size(WIDGET_WIDTH, 20).build());

        restartMicTestLine();
    }

    private void drawGroupTitle(String label, int centerX, int y) {
        this.addDrawableChild(ButtonWidget.builder(Text.literal(label), b -> {
                })
                .position(centerX - WIDGET_WIDTH / 2, y)
                .size(WIDGET_WIDTH, 20)
                .build()).active = false;
    }

    private int addDeviceSelector(int centerX, int y, Mixer.Info[] devices, boolean isInput) {
        List<Mixer.Info> list = Arrays.asList(devices);
        if (list.isEmpty()) {
            this.addDrawableChild(ButtonWidget.builder(Text.literal("No devices found"), b -> {
                    })
                    .position(centerX - WIDGET_WIDTH / 2, y)
                    .size(WIDGET_WIDTH, 20)
                    .build()).active = false;
            return y + 25;
        }

        Mixer.Info selected = isInput ? AudioDeviceState.selectedInput : AudioDeviceState.selectedOutput;
        if (selected == null) {
            selected = list.get(0);
            if (isInput) AudioDeviceState.selectedInput = selected;
            else AudioDeviceState.selectedOutput = selected;
        }

        this.addDrawableChild(CyclingButtonWidget.builder((Mixer.Info info) -> Text.literal(info.getName()))
                .values(list)
                .initially(selected)
                .build(centerX - WIDGET_WIDTH / 2, y, WIDGET_WIDTH, 20, Text.literal("Device"), (button, value) -> {

                    AudioManager audioManager = MineboxAdditions.INSTANCE.state.getAudioManager();

                    if (isInput) {
                        AudioDeviceState.selectedInput = value;

                        if (audioManager != null) {
                            try {
                                audioManager.openMicrophone();
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                        }

                        MineboxAdditionConfig.get().selectedMicName = value.getName();
                        MineboxAdditionConfig.save();
                        restartMicTestLine();
                    } else {
                        AudioDeviceState.selectedOutput = value;

                        if (audioManager != null) {
                            try {
                                audioManager.openSpeaker();
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                        }

                        MineboxAdditionConfig.get().selectedSpeakerName = value.getName();
                        MineboxAdditionConfig.save();
                    }
                })
        );

        return y + 25;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);

        int barX = this.width / 2 - WIDGET_WIDTH / 2;
        int barY = micBarY;
        int barHeight = 10;

        // Bar BG
        context.fill(barX, barY, barX + WIDGET_WIDTH, barY + barHeight, 0xFF333333);

        // SPL
        float estimatedSPL = currentMicDbFS + 96f;
        float normalized = Math.min(Math.max((estimatedSPL - 50f) / 46f, 0f), 1f);
        int fillWidth = (int) (normalized * WIDGET_WIDTH);

        int fillColor = 0xFF00FF00; // Green
        if (estimatedSPL >= 85f) fillColor = 0xFFFF0000; // Red
        else if (estimatedSPL >= 75f) fillColor = 0xFFFFA500; // Orange
        else if (estimatedSPL >= 65f) fillColor = 0xFFADFF2F; // Yellow-green

        context.fill(barX, barY, barX + fillWidth, barY + barHeight, fillColor);
    }

    private void restartMicTestLine() {
        stopMicThread();
        startMicLevelThread();
    }

    private void stopMicThread() {
        if (micThread != null && micThread.isAlive()) {
            micThread.interrupt();
            micThread = null;
        }

        AudioManager audioManager = MineboxAdditions.INSTANCE.state.getAudioManager();
        TargetDataLine sharedMic = audioManager != null ? audioManager.getMicrophone() : null;

        if (micTestLine != null && micTestLine.isOpen()) {
            if (micTestLine != sharedMic) {
                micTestLine.stop();
                micTestLine.close();
            }
            micTestLine = null;
        }

        micRunning = false;
        currentMicLevel = 0f;
        smoothedMicLevel = 0f;
    }

    private void startMicLevelThread() {
        if (micRunning || AudioDeviceState.selectedInput == null) return;

        micRunning = true;
        micThread = new Thread(() -> {
            try {
                AudioFormat format = new AudioFormat(48000f, 16, 1, true, false);
                DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
                Mixer mixer = AudioSystem.getMixer(AudioDeviceState.selectedInput);

                AudioManager audioManager = MineboxAdditions.INSTANCE.state.getAudioManager();
                TargetDataLine sharedMic = audioManager != null ? audioManager.getMicrophone() : null;

                if (sharedMic != null && sharedMic.isOpen()) {
                    micTestLine = sharedMic;
                } else {
                    micTestLine = (TargetDataLine) mixer.getLine(info);
                    micTestLine.open(format);
                    micTestLine.start();
                }

                byte[] buffer = new byte[480];

                while (!Thread.currentThread().isInterrupted()
                        && micTestLine.isOpen()
                        && MinecraftClient.getInstance().currentScreen == this) {

                    int available = micTestLine.available();
                    if (available < buffer.length) {
                        Thread.yield();
                        continue;
                    }

                    int bytesRead = micTestLine.read(buffer, 0, buffer.length);
                    if (bytesRead <= 0) continue;

                    float linearGain = (float) Math.pow(10, AudioDeviceState.micGainDb / 20.0);

                    double sum = 0.0;
                    int samples = 0;

                    for (int i = 0; i < bytesRead - 1; i += 2) {
                        short sample = (short) ((buffer[i + 1] << 8) | (buffer[i] & 0xFF));
                        float amplified = sample * linearGain;
                        sum += amplified * amplified;
                        samples++;
                    }

                    if (samples > 0) {
                        float rms = (float) Math.sqrt(sum / samples);
                        currentMicLevel = Math.min(1.0f, rms / 32768f);
                    }

                    currentMicDbFS = currentMicLevel <= 0.00001f
                            ? -100f
                            : 20f * (float) Math.log10(Math.max(smoothedMicLevel, 0.0001f));
                }

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                micRunning = false;
            }
        });

        micThread.start();
    }

    private void playTestTone() {
        new Thread(() -> {
            try {
                AudioFormat format = new AudioFormat(48000f, 16, 2, true, false);
                SourceDataLine line;

                Mixer mixer = AudioSystem.getMixer(AudioDeviceState.selectedOutput);
                if (mixer == null) {
                    System.err.println("No valid speaker selected");
                    return;
                }

                line = (SourceDataLine) mixer.getLine(new DataLine.Info(SourceDataLine.class, format));
                line.open(format);
                line.start();

                int durationMs = 500; // half-second tone
                double freq = 440.0; // A4
                byte[] buffer = new byte[(int) (format.getFrameSize() * format.getFrameRate() * durationMs / 1000)];
                float gain = AudioDeviceState.speakerVolumeMultiplier;

                for (int i = 0; i < buffer.length / 4; i++) {
                    double angle = 2.0 * Math.PI * i * freq / format.getSampleRate();
                    float raw = (float) (Math.sin(angle) * 32767.0 * gain);
                    raw = Math.max(Short.MIN_VALUE, Math.min(Short.MAX_VALUE, raw));
                    short sample = (short) raw;
                    buffer[4 * i] = (byte) (sample & 0xFF);         // Left LSB
                    buffer[4 * i + 1] = (byte) ((sample >> 8) & 0xFF); // Left MSB
                    buffer[4 * i + 2] = (byte) (sample & 0xFF);         // Right LSB
                    buffer[4 * i + 3] = (byte) ((sample >> 8) & 0xFF);  // Right MSB
                }

                line.write(buffer, 0, buffer.length);
                line.drain();
                line.stop();
                line.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }


    @Override
    public boolean shouldPause() {
        return false;
    }

    @Override
    public void close() {
        if (micThread != null && micThread.isAlive()) {
            micThread.interrupt();
            micThread = null;
        }

        AudioManager audioManager = MineboxAdditions.INSTANCE.state.getAudioManager();
        TargetDataLine sharedMic = audioManager != null ? audioManager.getMicrophone() : null;

        if (micTestLine != null && micTestLine.isOpen()) {
            if (micTestLine != sharedMic) {
                micTestLine.stop();
                micTestLine.close();
            }
            micTestLine = null;
        }

        micRunning = false;
        currentMicLevel = 0f;
        smoothedMicLevel = 0f;

        super.close();
    }

}
