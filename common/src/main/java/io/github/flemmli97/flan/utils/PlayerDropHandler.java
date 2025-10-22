package io.github.flemmli97.flan.utils;

public interface PlayerDropHandler {

    /**
     * @param drop Setting to true ignored the drop permission in claims
     */
    void flan$setForcedDrop(boolean drop);

    boolean flan$forcedDropState();
}
