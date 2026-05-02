package io.github.flemmli97.flan.gui;

public interface TickingGui {

    default int updatePeriod() {
        return 20;
    }

    void tick();
}
