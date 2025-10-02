package de.keksuccino.fancymenu.v3.rendering.ui.widget;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class RendererWidget extends AbstractWidget implements UniqueWidget {

    @NotNull
    protected RendererWidgetBody body;
    @Nullable
    protected String identifier;

    public RendererWidget(int x, int y, int width, int height, @NotNull RendererWidgetBody body) {
        super(x, y, width, height, Component.empty());
        this.body = body;
    }

    @Override
    public void renderWidget(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partial) {
        this.body.render(graphics, mouseX, mouseY, partial, this.getX(), this.getY(), this.getWidth(), this.getHeight(), this);
    }

    @Override
    protected void updateWidgetNarration(@NotNull NarrationElementOutput var1) {
    }

    public RendererWidget setBody(@NotNull RendererWidgetBody body) {
        this.body = body;
        return this;
    }

    @Override
    @Nullable
    public String getWidgetIdentifierFancyMenu() {
        return this.identifier;
    }

    @Override
    public RendererWidget setWidgetIdentifierFancyMenu(@Nullable String identifier) {
        this.identifier = identifier;
        return this;
    }

    @FunctionalInterface
    public interface RendererWidgetBody {
        void render(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partial, int x, int y, int width, int height, @NotNull RendererWidget renderer);
    }

}
