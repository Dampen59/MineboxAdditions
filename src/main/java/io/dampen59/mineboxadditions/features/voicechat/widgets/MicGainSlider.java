package io.dampen59.mineboxadditions.features.voicechat.widgets;

import io.dampen59.mineboxadditions.config.Config;
import io.dampen59.mineboxadditions.config.ConfigManager;
import io.dampen59.mineboxadditions.features.voicechat.AudioDeviceState;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.text.Text;

public class MicGainSlider extends SliderWidget {
    public MicGainSlider(int x, int y, int width, int height, float initialDb) {
        super(x, y, width, height, Text.empty(), (initialDb + 20f) / 50f);
        AudioDeviceState.micGainDb = initialDb;
        updateMessage();
    }

    @Override
    protected void updateMessage() {
        float db = (float) (value * 50f - 20f);
        this.setMessage(Text.literal(String.format("Mic Gain: %.1f dB", db)));
    }

    @Override
    protected void applyValue() {
        AudioDeviceState.micGainDb = (float) (value * 50f - 20f);
        Config.micGainDb = AudioDeviceState.micGainDb;
        ConfigManager.save();
    }
}

