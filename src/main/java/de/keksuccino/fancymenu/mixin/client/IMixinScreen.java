package de.keksuccino.fancymenu.mixin.client;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.List;

@Mixin(Screen.class)
public interface IMixinScreen {

    @Accessor("font") void setFontFancyMenu(Font font);

    @Accessor("renderables") List<Renderable> getRenderablesFancyMenu();

    @Accessor("children") List<GuiEventListener> getChildrenFancyMenu();

}
