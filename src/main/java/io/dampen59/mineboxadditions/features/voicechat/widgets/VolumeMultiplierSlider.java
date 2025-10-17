package io.dampen59.mineboxadditions.features.voicechat.widgets;

import io.dampen59.mineboxadditions.config.Config;
import io.dampen59.mineboxadditions.config.ConfigManager;
import io.dampen59.mineboxadditions.features.voicechat.AudioDeviceState;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.text.Text;

public class VolumeMultiplierSlider extends SliderWidget {
    public VolumeMultiplierSlider(int x, int y, int width, int height, float initialVolumeMultiplier) {
        super(x, y, width, height, Text.empty(), initialVolumeMultiplier / 2f);
        AudioDeviceState.speakerVolumeMultiplier = initialVolumeMultiplier;
        updateMessage();
    }

    @Override
    protected void updateMessage() {
        int percent = Math.round(AudioDeviceState.speakerVolumeMultiplier * 100);
        this.setMessage(Text.literal("Volume: " + percent + "%"));
    }

    @Override
    protected void applyValue() {
        AudioDeviceState.speakerVolumeMultiplier = (float) (this.value * 2.0); // 0.0 – 2.0
        Config.speakerVolumeMultiplier = AudioDeviceState.speakerVolumeMultiplier;
        ConfigManager.save();
    }
}
