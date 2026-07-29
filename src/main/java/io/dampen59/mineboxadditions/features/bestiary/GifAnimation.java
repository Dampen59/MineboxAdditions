package io.dampen59.mineboxadditions.features.bestiary;

import net.minecraft.resources.Identifier;
import java.util.List;

public class GifAnimation {

    public static final GifAnimation EMPTY = new GifAnimation(List.of(), List.of());

    private final List<Identifier> frames;
    private final List<Integer> delaysMs;
    private final int totalDurationMs;
    private final long startMs = System.currentTimeMillis();

    public GifAnimation(List<Identifier> frames, List<Integer> delaysMs) {
        this.frames = List.copyOf(frames);
        this.delaysMs = delaysMs.isEmpty() ? List.of(100) : List.copyOf(delaysMs);
        int total = this.delaysMs.stream().mapToInt(Integer::intValue).sum();
        this.totalDurationMs = Math.max(total, 1);
    }

    public Identifier getCurrentFrame() {
        if (frames.isEmpty()) return null;
        if (frames.size() == 1) return frames.get(0);
        long elapsed = (System.currentTimeMillis() - startMs) % totalDurationMs;
        int acc = 0;
        for (int i = 0; i < frames.size(); i++) {
            acc += delaysMs.get(i);
            if (elapsed < acc) return frames.get(i);
        }
        return frames.get(frames.size() - 1);
    }

    public Identifier getFirstFrame() {
        return frames.isEmpty() ? null : frames.get(0);
    }

    public boolean isEmpty() { return frames.isEmpty(); }
}
