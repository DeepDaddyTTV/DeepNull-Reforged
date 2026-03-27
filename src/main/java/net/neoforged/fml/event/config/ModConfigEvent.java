package net.neoforged.fml.event.config;

public abstract class ModConfigEvent {
    private ModConfigEvent() {
    }

    public static final class Loading extends ModConfigEvent {
    }

    public static final class Reloading extends ModConfigEvent {
    }
}
