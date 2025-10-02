package de.keksuccino.fancymenu.menu.fancy.menuhandler.custom;

import java.util.List;
import java.util.Random;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.gui.GuiGraphics;
import de.keksuccino.fancymenu.events.InitOrResizeScreenEvent;
import de.keksuccino.fancymenu.menu.button.ButtonCachedEvent;
import de.keksuccino.fancymenu.menu.fancy.MenuCustomization;
import de.keksuccino.fancymenu.menu.fancy.helper.MenuReloadedEvent;
import de.keksuccino.fancymenu.menu.fancy.menuhandler.MenuHandlerBase;
import de.keksuccino.fancymenu.menu.fancy.menuhandler.deepcustomizationlayer.DeepCustomizationElement;
import de.keksuccino.fancymenu.menu.fancy.menuhandler.deepcustomizationlayer.DeepCustomizationItem;
import de.keksuccino.fancymenu.menu.fancy.menuhandler.deepcustomizationlayer.DeepCustomizationLayer;
import de.keksuccino.fancymenu.menu.fancy.menuhandler.deepcustomizationlayer.DeepCustomizationLayerRegistry;
import de.keksuccino.fancymenu.menu.fancy.menuhandler.deepcustomizationlayer.layers.titlescreen.splash.TitleScreenSplashElement;
import de.keksuccino.fancymenu.menu.fancy.menuhandler.deepcustomizationlayer.layers.titlescreen.splash.TitleScreenSplashItem;
import de.keksuccino.fancymenu.mixin.client.IMixinTitleScreen;
import de.keksuccino.konkrete.gui.screens.popup.PopupHandler;
import de.keksuccino.konkrete.input.MouseInput;
import de.keksuccino.konkrete.properties.PropertiesSection;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;

import net.minecraft.client.gui.components.LogoRenderer;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.renderer.CubeMap;
import net.minecraft.client.renderer.PanoramaRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.client.event.ScreenEvent.BackgroundRendered;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.internal.BrandingControl;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@SuppressWarnings("all")
public class MainMenuHandler extends MenuHandlerBase {

	private static final Logger LOGGER = LogManager.getLogger();
	
	private static final CubeMap PANORAMA_CUBE_MAP = new CubeMap(new ResourceLocation("textures/gui/title/background/panorama"));
	private static final ResourceLocation PANORAMA_OVERLAY = new ResourceLocation("textures/gui/title/background/panorama_overlay.png");
//	private static final ResourceLocation MINECRAFT_TITLE_TEXTURE = new ResourceLocation("textures/gui/title/minecraft.png");
//	private static final ResourceLocation EDITION_TITLE_TEXTURE = new ResourceLocation("textures/gui/title/edition.png");
	private static final LogoRenderer LOGO_RENDERER = new LogoRenderer(false);
	private static final Random RANDOM = new Random();

	private PanoramaRenderer panorama = new PanoramaRenderer(PANORAMA_CUBE_MAP);

	protected boolean showLogo = true;
	protected boolean showBranding = true;
	protected boolean showForgeNotificationCopyright = true;
	protected boolean showForgeNotificationTop = true;
	protected boolean showRealmsNotification = true;
	protected TitleScreenSplashItem splashItem = null;

	public MainMenuHandler() {
		super(TitleScreen.class.getName());
	}

	@Override
	public void onMenuReloaded(MenuReloadedEvent e) {
		super.onMenuReloaded(e);

		TitleScreenSplashItem.cachedSplashText = null;
	}

	
	@Override
	public void onInitPre(InitOrResizeScreenEvent.Pre e) {
		if (this.shouldCustomize(e.getScreen())) {
			if (MenuCustomization.isMenuCustomizable(e.getScreen())) {
				if (e.getScreen() instanceof TitleScreen) {
					setShowFadeInAnimation(false, (TitleScreen) e.getScreen());
				}
			}
		}
		super.onInitPre(e);
	}

	@Override
	public void onButtonsCached(ButtonCachedEvent e) {

		if (this.shouldCustomize(e.getScreen())) {
			if (MenuCustomization.isMenuCustomizable(e.getScreen())) {

				showLogo = true;
				showBranding = true;
				showForgeNotificationCopyright = true;
				showForgeNotificationTop = true;
				showRealmsNotification = true;
				DeepCustomizationLayer layer = DeepCustomizationLayerRegistry.getLayerByMenuIdentifier(this.getMenuIdentifier());
				if (layer != null) {
					TitleScreenSplashElement element = (TitleScreenSplashElement) layer.getElementByIdentifier("title_screen_splash");
					if (element != null) {
						splashItem = (TitleScreenSplashItem) element.constructDefaultItemInstance();
					}
				}

				super.onButtonsCached(e);

			}
		}
	}

	@Override
	protected void applyLayout(PropertiesSection sec, String renderOrder, ButtonCachedEvent e) {

		super.applyLayout(sec, renderOrder, e);

		DeepCustomizationLayer layer = DeepCustomizationLayerRegistry.getLayerByMenuIdentifier(this.getMenuIdentifier());
		if (layer != null) {

			String action = sec.getEntryValue("action");
			if (action != null) {

				if (action.startsWith("deep_customization_element:")) {
					String elementId = action.split("[:]", 2)[1];
					DeepCustomizationElement element = layer.getElementByIdentifier(elementId);
					if (element != null) {
						DeepCustomizationItem i = element.constructCustomizedItemInstance(sec);
						if (i != null) {
							if (elementId.equals("title_screen_branding")) {
								if (this.showBranding) {
									this.showBranding = !(i.hidden);
								}
							}
							if (elementId.equals("title_screen_logo")) {
								if (this.showLogo) {
									this.showLogo = !(i.hidden);
								}
							}
							if (elementId.equals("title_screen_splash")) {
								if ((this.splashItem == null) || !this.splashItem.hidden) {
									this.splashItem = (TitleScreenSplashItem) i;
								}
							}
							if (elementId.equals("title_screen_realms_notification")) {
								if (this.showRealmsNotification) {
									this.showRealmsNotification = !(i.hidden);
								}
							}
							//Forge -------------->
							if (elementId.equals("title_screen_forge_copyright")) {
								if (this.showForgeNotificationCopyright) {
									this.showForgeNotificationCopyright = !(i.hidden);
								}
							}
							if (elementId.equals("title_screen_forge_top")) {
								if (this.showForgeNotificationTop) {
									this.showForgeNotificationTop = !(i.hidden);
								}
							}
						}
					}
				}

			}

		}

	}

	@SubscribeEvent
	public void onRender(ScreenEvent.Render.Pre e) {
		if (this.shouldCustomize(e.getScreen())) {
			if (MenuCustomization.isMenuCustomizable(e.getScreen())) {
				e.setCanceled(true);
				e.getScreen().renderBackground(e.getGuiGraphics());
			}
		}
	}

	/**
	 * Mimic the original main menu to be able to customize it easier
	 */
	@Override
	public void drawToBackground(BackgroundRendered e) {
		if (this.shouldCustomize(e.getScreen())) {
			Font font = Minecraft.getInstance().font;
			int width = e.getScreen().width;
			int height = e.getScreen().height;
			int j = width / 2 - 137;
			float minecraftLogoSpelling = RANDOM.nextFloat();
			int mouseX = MouseInput.getMouseX();
			int mouseY = MouseInput.getMouseY();

			RenderSystem.enableBlend();

			//Draw the panorama skybox and a semi-transparent overlay over it
			if (!this.canRenderBackground()) {
				this.panorama.render(Minecraft.getInstance().getDeltaFrameTime(), 1.0F);
//				RenderUtils.bindTexture(PANORAMA_OVERLAY);
				RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
				RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
				e.getGuiGraphics().blit(PANORAMA_OVERLAY, 0, 0, e.getScreen().width, e.getScreen().height, 0.0F, 0.0F, 16, 128, 16, 128);
			}

			super.drawToBackground(e);

			if (this.showLogo) {
				LOGO_RENDERER.renderLogo(e.getGuiGraphics(), e.getScreen().width, 1.0F);
			}

			if (this.showBranding) {
				BrandingControl.forEachLine(true, true, (brdline, brd) -> {
					e.getGuiGraphics().drawString(font, brd, 2, e.getScreen().height - (10 + brdline * (font.lineHeight + 1)), 16777215);
				});
			}

			if (this.showForgeNotificationTop) {
				ForgeHooksClient.renderMainMenu((TitleScreen) e.getScreen(), e.getGuiGraphics(), Minecraft.getInstance().font, e.getScreen().width, e.getScreen().height, 255);
			}
			if (this.showForgeNotificationCopyright) {
				BrandingControl.forEachAboveCopyrightLine((brdline, brd) -> {
					e.getGuiGraphics().drawString(font, brd, e.getScreen().width - font.width(brd) - 1, e.getScreen().height - (11 + (brdline + 1) * (font.lineHeight + 1)), 16777215);
				});
			}

			if (!PopupHandler.isPopupActive()) {
				this.renderButtons(e, mouseX, mouseY);
			}

			if (this.showRealmsNotification) {
				this.drawRealmsNotification(e.getGuiGraphics(), e.getScreen());
			}

			this.renderSplash(e.getGuiGraphics(), e.getScreen());

		}
	}

	protected void renderSplash(GuiGraphics graphics, Screen s) {

		try {
			if (this.splashItem != null) {
				this.splashItem.render(graphics, s);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private void renderButtons(ScreenEvent.BackgroundRendered e, int mouseX, int mouseY) {
		List<Renderable> buttons = e.getScreen().renderables;
		float partial = Minecraft.getInstance().getFrameTime();
		if (buttons != null) {
			for(int i = 0; i < buttons.size(); ++i) {
				buttons.get(i).render(e.getGuiGraphics(), mouseX, mouseY, partial);
			}
		}
	}

	private void drawRealmsNotification(GuiGraphics graphics, Screen gui) {
		if (Minecraft.getInstance().options.realmsNotifications().get()) {
			Screen realms = ((IMixinTitleScreen)gui).getRealmsNotificationsScreenFancyMenu();
			if (realms != null) {
				//render
				realms.render(graphics, (int)Minecraft.getInstance().mouseHandler.xpos(), (int)Minecraft.getInstance().mouseHandler.ypos(), Minecraft.getInstance().getFrameTime());
			}
		}
	}

	protected static void setShowFadeInAnimation(boolean showFadeIn, TitleScreen s) {
		s.fading = showFadeIn;
	}

}
