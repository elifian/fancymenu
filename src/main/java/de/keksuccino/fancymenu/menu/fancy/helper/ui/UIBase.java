package de.keksuccino.fancymenu.menu.fancy.helper.ui;

import java.awt.Color;

import de.keksuccino.fancymenu.FancyMenu;
import de.keksuccino.konkrete.gui.content.AdvancedButton;
import de.keksuccino.konkrete.input.MouseInput;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;

public class UIBase {

	public static final Color SCROLL_GRABBER_IDLE_COLOR = new Color(89, 91, 93, 100);
	public static final Color SCROLL_GRABBER_HOVER_COLOR = new Color(102, 104, 104, 100);
	public static final Color SCREEN_BACKGROUND_COLOR = new Color(60, 63, 65);
	public static final Color ELEMENT_BORDER_COLOR_IDLE = new Color(209, 194, 209);
	public static final Color ELEMENT_BORDER_COLOR_HOVER = new Color(227, 211, 227);
	public static final Color ELEMENT_BACKGROUND_COLOR_IDLE = new Color(71, 71, 71);
	public static final Color ELEMENT_BACKGROUND_COLOR_HOVER = new Color(83, 156, 212);
	public static final Color AREA_BACKGROUND_COLOR = new Color(43, 43, 43);
	public static final Color ENTRY_COLOR_FOCUSED = new Color(50, 50, 50);
	public static final Color SIDE_BAR_COLOR = new Color(49, 51, 53);
	public static final Color TEXT_COLOR_RED_1 = new Color(237, 69, 69);
	public static final Color TEXT_COLOR_ORANGE_1 = new Color(170, 130, 63);
	public static final Color TEXT_COLOR_GRAY_1 = new Color(158, 170, 184);
	public static final Color TEXT_COLOR_GREY_2 = new Color(91, 92, 94);
	public static final Color TEXT_COLOR_GREY_3 = new Color(137, 147, 150);
	public static final Color TEXT_COLOR_GREY_4 = new Color(206, 221, 237);
	public static final Color LISTING_DOT_BLUE = new Color(62, 134, 160);
	public static final Color LISTING_DOT_RED = new Color(173, 108, 121);
	public static final Color LISTING_DOT_ORANGE = new Color(170, 130, 63);

	public static final int ELEMENT_BORDER_THICKNESS = 1;
	public static final float UI_SCALE_BASE = 1.0F;
	public static final int VERTICAL_SCROLL_BAR_WIDTH = 5;
	public static final int VERTICAL_SCROLL_BAR_HEIGHT = 40;
	public static final int HORIZONTAL_SCROLL_BAR_WIDTH = 40;
	public static final int HORIZONTAL_SCROLL_BAR_HEIGHT = 5;

	/**
	 * Applies the default skin to the input button and returns it.
	 *
	 * @return The input button.
	 */
	public static AdvancedButton applyDefaultButtonSkinTo(AdvancedButton button) {
		button.setBackgroundColor(ELEMENT_BACKGROUND_COLOR_IDLE, ELEMENT_BACKGROUND_COLOR_HOVER, ELEMENT_BORDER_COLOR_IDLE, ELEMENT_BORDER_COLOR_HOVER, ELEMENT_BORDER_THICKNESS);
		return button;
	}

	public static float getUIScale() {

		float uiScale = FancyMenu.getConfig().getOrDefault("uiscale", 1.0F);
		double mcScale = Minecraft.getInstance().getWindow().getGuiScale();

		return (float) ((((double)UI_SCALE_BASE) * (((double)UI_SCALE_BASE) / mcScale)) * uiScale);

	}

	public static void openScaledContextMenuAt(FMContextMenu menu, int x, int y) {
		Screen s = Minecraft.getInstance().screen;
		if (s != null) {
			menu.openMenuAt((int) (x / UIBase.getUIScale()), (int) (y / UIBase.getUIScale()), (int) (s.width / getUIScale()), (int) (s.height / getUIScale()));
		}
	}

	public static void openScaledContextMenuAtMouse(FMContextMenu menu) {
		openScaledContextMenuAt(menu, MouseInput.getMouseX(), MouseInput.getMouseY());
	}

	public static void renderScaledContextMenu(GuiGraphics graphics, FMContextMenu menu) {
		Screen s = Minecraft.getInstance().screen;
		if ((s != null) && (menu != null)) {

			graphics.pose().pushPose();

			graphics.pose().scale(UIBase.getUIScale(), UIBase.getUIScale(), UIBase.getUIScale());

			MouseInput.setRenderScale(UIBase.getUIScale());
			int mouseX = MouseInput.getMouseX();
			int mouseY = MouseInput.getMouseY();
			MouseInput.resetRenderScale();

			menu.render(graphics, mouseX, mouseY, (int) (s.width / getUIScale()), (int) (s.height / getUIScale()));

			graphics.pose().popPose();

		}
	}

	public static void renderListingDot(GuiGraphics graphics, int x, int y, Color color) {
		graphics.fill(x, y, x + 4, y + 4, color.getRGB());
	}

	public static void renderBorder(GuiGraphics graphics, int xMin, int yMin, int xMax, int yMax, int borderThickness, Color borderColor, boolean renderTop, boolean renderLeft, boolean renderRight, boolean renderBottom) {
		if (renderTop) {
			graphics.fill(xMin, yMin, xMax, yMin + borderThickness, borderColor.getRGB());
		}
		if (renderLeft) {
			graphics.fill(xMin, yMin + borderThickness, xMin + borderThickness, yMax - borderThickness, borderColor.getRGB());
		}
		if (renderRight) {
			graphics.fill(xMax - borderThickness, yMin + borderThickness, xMax, yMax - borderThickness, borderColor.getRGB());
		}
		if (renderBottom) {
			graphics.fill(xMin, yMax - borderThickness, xMax, yMax, borderColor.getRGB());
		}
	}

	@Deprecated
	public static void colorizeButton(AdvancedButton button) {
		applyDefaultButtonSkinTo(button);
	}

	@Deprecated
	public static Color getButtonIdleColor() {
		return ELEMENT_BACKGROUND_COLOR_IDLE;
	}

	@Deprecated
	public static Color getButtonBorderIdleColor() {
		return ELEMENT_BORDER_COLOR_IDLE;
	}

	@Deprecated
	public static Color getButtonHoverColor() {
		return ELEMENT_BACKGROUND_COLOR_HOVER;
	}

	@Deprecated
	public static Color getButtonBorderHoverColor() {
		return ELEMENT_BORDER_COLOR_HOVER;
	}

}
