package de.keksuccino.fancymenu.menu.fancy.menuhandler.deepcustomizationlayer.layers.titlescreen.forge.top;

import net.minecraft.client.gui.GuiGraphics;
import de.keksuccino.fancymenu.menu.fancy.menuhandler.deepcustomizationlayer.DeepCustomizationElement;
import de.keksuccino.fancymenu.menu.fancy.menuhandler.deepcustomizationlayer.DeepCustomizationItem;
import de.keksuccino.konkrete.localization.Locals;
import de.keksuccino.konkrete.properties.PropertiesSection;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.Component;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.fml.VersionChecker;
import net.minecraftforge.versions.forge.ForgeVersion;

import java.io.IOException;

import static net.minecraftforge.fml.VersionChecker.Status.BETA;
import static net.minecraftforge.fml.VersionChecker.Status.BETA_OUTDATED;

public class TitleScreenForgeTopItem extends DeepCustomizationItem {

    public TitleScreenForgeTopItem(DeepCustomizationElement parentElement, PropertiesSection item) {
        super(parentElement, item);
    }

    @Override
    public void render(GuiGraphics graphics, Screen menu) throws IOException {

        Font font = Minecraft.getInstance().font;

        Component line1 = Component.literal(Locals.localize("fancymenu.helper.editor.element.vanilla.deepcustomization.titlescreen.forge.top.example.line1"));
        graphics.drawCenteredString(font, line1, menu.width / 2, 4 + (0 * (font.lineHeight + 1)), -1);

        Component line2 = Component.literal(Locals.localize("fancymenu.helper.editor.element.vanilla.deepcustomization.titlescreen.forge.top.example.line2"));
        graphics.drawCenteredString(font, line2, menu.width / 2, 4 + (1 * (font.lineHeight + 1)), -1);

        this.width = font.width(line1);
        int w2 = font.width(line2);
        if (this.width < w2) {
            this.width = w2;
        }
        this.height = (font.lineHeight * 2) + 1;

        this.posX = (menu.width / 2) - (this.getWidth() / 2);
        this.posY = 4;

    }

}