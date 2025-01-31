package io.dampen59.mineboxadditions.events;

import io.dampen59.mineboxadditions.state.State;

public class TooltipEvent {
    private State modState = null;

    public TooltipEvent(State prmModState) {
        this.modState = prmModState;
        initializeTooltips();
    }

    public void initializeTooltips() {

    }

}
