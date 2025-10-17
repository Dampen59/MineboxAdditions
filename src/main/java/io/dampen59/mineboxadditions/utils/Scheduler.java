package io.dampen59.mineboxadditions.utils;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ForkJoinPool;

public class Scheduler {
    public static final Scheduler INSTANCE = new Scheduler();

    private int tick = 0;
    private final ConcurrentHashMap<Integer, CopyOnWriteArrayList<Task>> tasks = new ConcurrentHashMap<>();
    private final ExecutorService executor = ForkJoinPool.commonPool();

    private Scheduler() {}

    public void schedule(Runnable task, int delay) {
        schedule(task, delay, false);
    }

    public void schedule(Runnable runnable, int delay, boolean multithreaded) {
        if (delay < 0) return;
        addTask(new Task(runnable, 0, false, multithreaded), delay);
    }

    public void cycle(Runnable task, int period) {
        cycle(task, period, false);
    }

    public void cycle(Runnable runnable, int period, boolean multithreaded) {
        if (period < 1) return;
        addTask(new Task(runnable, period, true, multithreaded), 0);
    }

    public void tick() {
        List<Task> current = tasks.remove(tick);
        if (current != null) {
            for (Task t : current) runTask(t);
        }
        tick++;
    }

    private void addTask(Task task, int delay) {
        if (!RenderSystem.isOnRenderThread() && MinecraftClient.getInstance() != null) {
            MinecraftClient.getInstance().send(() -> addTask(task, delay));
            return;
        }
        tasks.computeIfAbsent(tick + delay, k -> new CopyOnWriteArrayList<>()).add(task);
    }

    private void runTask(Task task) {
        if (task.multithreaded) executor.execute(task::run);
        else task.run();
    }

    private static class Task {
        private final Runnable runnable;
        private final int interval;
        private final boolean cyclic;
        private final boolean multithreaded;

        public Task(Runnable runnable, int interval, boolean cyclic, boolean multithreaded) {
            this.runnable = runnable;
            this.interval = interval;
            this.cyclic = cyclic;
            this.multithreaded = multithreaded;
        }

        public void run() {
            runnable.run();
            if (cyclic) {
                Scheduler.INSTANCE.addTask(new Task(runnable, interval, true, multithreaded), interval);
            }
        }
    }
}
