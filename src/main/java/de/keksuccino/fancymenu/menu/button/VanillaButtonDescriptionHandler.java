package de.keksuccino.fancymenu.menu.button;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.gui.GuiGraphics;
import de.keksuccino.fancymenu.events.InitOrResizeScreenEvent;
import de.keksuccino.konkrete.input.StringUtils;
import de.keksuccino.konkrete.rendering.RenderUtils;
import net.minecraft.client.Minecraft;

import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.client.event.ScreenEvent.Render;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class VanillaButtonDescriptionHandler {
	
	private static Map<AbstractWidget, String> descriptions = new HashMap<AbstractWidget , String>();
	
	public static void init() {
		MinecraftForge.EVENT_BUS.register(new VanillaButtonDescriptionHandler());
	}

	
	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void onInitPre(InitOrResizeScreenEvent.Pre e) {
		descriptions.clear();
	}
	
	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void onDrawScreen(Render.Post e) {
		for (Map.Entry<AbstractWidget , String> m : descriptions.entrySet()) {
			if (m.getKey().isHoveredOrFocused()) {
				renderDescription(e.getGuiGraphics(), e.getMouseX(), e.getMouseY(), m.getValue());
				break;
			}
		}
	}
	
	public static void setDescriptionFor(AbstractWidget w, String desc) {
		descriptions.put(w, desc);
	}
	
	private static void renderDescriptionBackground(GuiGraphics graphics, int x, int y, int width, int height) {
		graphics.fill(x, y, x + width, y + height, new Color(26, 26, 26, 250).getRGB());
	}
	
	private static void renderDescription(GuiGraphics graphics, int mouseX, int mouseY, String desc) {
		if (desc != null) {
			int width = 10;
			int height = 10;

			String[] descArray = StringUtils.splitLines(desc, "%n%");
			
			//Getting the longest string from the list to render the background with the correct width
			for (String s : descArray) {
				int i = Minecraft.getInstance().font.width(s) + 10;
				if (i > width) {
					width = i;
				}
				height += 10;
			}

			mouseX += 5;
			mouseY += 5;
			
			if (Minecraft.getInstance().screen.width < mouseX + width) {
				mouseX -= width + 10;
			}
			
			if (Minecraft.getInstance().screen.height < mouseY + height) {
				mouseY -= height + 10;
			}

			RenderUtils.setZLevelPre(graphics.pose(), 600);
			
			renderDescriptionBackground(graphics, mouseX, mouseY, width, height);

			RenderSystem.enableBlend();

			int i2 = 5;
			for (String s : descArray) {
				graphics.drawString(Minecraft.getInstance().font, s, mouseX + 5, mouseY + i2, Color.WHITE.getRGB());
				i2 += 10;
			}

			RenderUtils.setZLevelPost(graphics.pose());
			
			RenderSystem.disableBlend();
		}
	}
	
}
