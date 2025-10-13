package io.dampen59.mineboxadditions.features.hud;

import io.dampen59.mineboxadditions.features.hud.elements.Element;
import io.dampen59.mineboxadditions.features.hud.elements.stack.StackElement;
import net.minecraft.client.gui.DrawContext;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

public abstract class Hud {
    private final Supplier<Boolean> onGetState;
    private final Consumer<Boolean> onSetState;
    private final Supplier<Integer> onGetX;
    private final Consumer<Integer> onSetX;
    private final Supplier<Integer> onGetY;
    private final Consumer<Integer> onSetY;
    public final StackElement mainStack;
    private final Map<String, Element> namedElements = new HashMap<>();

    public Hud(
            Supplier<Boolean> getState, Consumer<Boolean> setState,
            Supplier<Integer> getX, Consumer<Integer> setX,
            Supplier<Integer> getY, Consumer<Integer> setY) {
        this.onGetState = getState;
        this.onSetState = setState;
        this.onGetX = getX;
        this.onGetY = getY;
        this.onSetX = setX;
        this.onSetY = setY;
        this.mainStack = init();
    }

    public abstract StackElement init();

    public boolean getState() {
        return onGetState.get();
    }

    public void setState(boolean state) {
        onSetState.accept(state);
    }

    public int getX() {
        return onGetX.get();
    }

    public void setX(int x) {
        onSetX.accept(x);
    }

    public int getY() {
        return onGetY.get();
    }

    public void setY(int y) {
        onSetY.accept(y);
    }

    public int getWidth() {
        return mainStack.getWidth();
    }

    public int getHeight() {
        return mainStack.getHeight();
    }

    public void addNamedElement(String name, Element element) {
        if (element != null) {
            namedElements.put(name, element);
        }
    }

    public <T extends Element> T getNamedElement(String name, Class<T> clazz) {
        Element element = namedElements.get(name);
        if (clazz.isInstance(element)) return clazz.cast(element);
        else throw new IllegalStateException();
    }

    public void draw(DrawContext context) {
        mainStack.setColor(0x40000000);
        mainStack.draw(context, getX(), getY());
    }

    public void drawDisabled(DrawContext context) {
        mainStack.setColor(0x40FF0000);
        mainStack.draw(context, getX(), getY());
    }
}
