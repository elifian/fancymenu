package de.keksuccino.fancymenu.events;

import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.screens.Screen;
import net.minecraftforge.eventbus.api.Event;

import java.util.List;
import java.util.Objects;


public class InitOrResizeScreenEvent extends Event {

    protected final Screen screen;

    protected InitOrResizeScreenEvent(Screen screen) {
        this.screen = Objects.requireNonNull(screen);
    }

    @Override
    public boolean isCancelable() {
        return false;
    }

    public Screen getScreen() {
        return screen;
    }

    /** You should not get the widgets list in Pre, because it is fired BEFORE the list get cleared. **/
    public static class Pre extends InitOrResizeScreenEvent {

        public Pre(Screen screen) {
            super(screen);
        }

    }

    public static class Post extends InitOrResizeScreenEvent {

        public Post(Screen screen) {
            super(screen);
        }

        public List<Renderable> getRenderables() {
            return this.screen.renderables;
        }

    }

}
