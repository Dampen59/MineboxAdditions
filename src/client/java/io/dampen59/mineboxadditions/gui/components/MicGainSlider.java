package io.dampen59.mineboxadditions.gui.components;

import io.dampen59.mineboxadditions.MineboxAdditions;
import io.dampen59.mineboxadditions.MineboxAdditionConfig;
import io.dampen59.mineboxadditions.state.AudioDeviceState;
import me.shedaniel.autoconfig.AutoConfig;
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
        MineboxAdditions.INSTANCE.config.micGainDb = AudioDeviceState.micGainDb;
        AutoConfig.getConfigHolder(MineboxAdditionConfig.class).save();
    }
}

