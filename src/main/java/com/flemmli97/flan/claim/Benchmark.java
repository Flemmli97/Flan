package com.flemmli97.flan.claim;

import org.apache.commons.lang3.time.StopWatch;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class Benchmark {

    private final StopWatch stopWatch = new StopWatch();
    private final List<Long> times = new LinkedList<>();

    public Benchmark() {
    }
    public void start() {
        stopWatch.start();
    }

    public void stop() {
        stopWatch.stop();
        times.add(stopWatch.getNanoTime());
        stopWatch.reset();
    }

    public void reset(){
        times.clear();
        stopWatch.reset();
    }

    public long getAverage() {
        return times.stream().reduce(0L, Long::sum)/times.size();
    }

    public long worstCase() {
        return times.stream().max(Long::compareTo).orElse(-1L);
    }
}
