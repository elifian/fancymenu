package de.keksuccino.fancymenu.menu.animation;

import java.io.File;
import java.util.*;

import de.keksuccino.fancymenu.FancyMenu;
import de.keksuccino.fancymenu.menu.animation.exceptions.AnimationNotFoundException;
import de.keksuccino.konkrete.math.MathUtils;
import de.keksuccino.konkrete.properties.PropertiesSection;
import de.keksuccino.konkrete.properties.PropertiesSerializer;
import de.keksuccino.konkrete.properties.PropertiesSet;
import de.keksuccino.konkrete.rendering.animation.IAnimationRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("unused")
public class AnimationHandler {

	private static final Logger LOGGER = LogManager.getLogger();

	private static final Map<String, AnimationData> ANIMATIONS = new HashMap<>();
	private static final List<String> EXTERNAL_ANIMATION_NAMES = new ArrayList<>();
	protected static boolean preloadCompleted = false;
	protected static boolean initialized = false;

	public static void init() {
		if (!initialized) {
			MinecraftForge.EVENT_BUS.register(new AnimationHandlerEvents());
		}
		initialized = true;
	}

	public static void register(@NotNull IAnimationRenderer animation, @NotNull String name, @NotNull AnimationData.Type type) {
		if (!ANIMATIONS.containsKey(name)) {
			ANIMATIONS.put(name, new AnimationData(animation, name, type));
			if (type == AnimationData.Type.EXTERNAL) {
				EXTERNAL_ANIMATION_NAMES.add(name);
			}
		} else {
			LOGGER.error("[FANCYMENU] Failed to register animation! Animation with same name already exists: " + name);
		}
	}

	public static void unregister(@NotNull IAnimationRenderer animation) {
		AnimationData d = null;
		for (AnimationData a : ANIMATIONS.values()) {
			if (a.animation == animation) {
				d = a;
				break;
			}
		}
		if (d != null) {
			unregister(d.name);
		}
	}

	public static void unregister(@NotNull String name) {
		if (animationExists(name)) {
			ANIMATIONS.remove(name);
			EXTERNAL_ANIMATION_NAMES.remove(name);
		}
	}

	public static void discoverAndRegisterExternalAnimations() {

		File f = FancyMenu.getAnimationPath();
		if (!f.exists() || !f.isDirectory()) {
			return;
		}

		preloadCompleted = false;
		clearExternalAnimations();

		File[] filesArray = f.listFiles();
		if (filesArray == null) return;
		for (File a : filesArray) {
			String name;
			String mainAudio = null;
			String introAudio = null;
			int fps = 0;
			boolean loop = true;
			boolean replayIntro = false;
			List<String> frameNamesMain = new ArrayList<>();
			List<String> frameNamesIntro = new ArrayList<>();
			String resourceNamespace;

			if (a.isDirectory()) {

				File p = new File(a.getAbsolutePath().replace("\\", "/") + "/animation.properties");
				if (!p.exists()) {
					continue;
				}
				
				PropertiesSet props = PropertiesSerializer.getProperties(p.getPath());
				if (props == null) {
					continue;
				}

				// ANIMATION META
				List<PropertiesSection> metas = props.getPropertiesOfType("animation-meta");
				if (metas.isEmpty()) {
					continue;
				}
				PropertiesSection m = metas.get(0);

				name = m.getEntryValue("name");
				if (name == null) {
					continue;
				}

				String fpsString = m.getEntryValue("fps");
				if ((fpsString != null) && MathUtils.isInteger(fpsString)) {
					fps = Integer.parseInt(fpsString);
				}

				String loopString = m.getEntryValue("loop");
				if ((loopString != null) && loopString.equalsIgnoreCase("false")) {
					loop = false;
				}

				String replayString = m.getEntryValue("replayintro");
				if ((replayString != null) && replayString.equalsIgnoreCase("true")) {
					replayIntro = true;
				}

				resourceNamespace = m.getEntryValue("namespace");
				if (resourceNamespace == null) {
					continue;
				}

				// MAIN FRAME NAMES
				List<PropertiesSection> mainFrameSecs = props.getPropertiesOfType("frames-main");
				if (mainFrameSecs.isEmpty()) {
					continue;
				}
				PropertiesSection mainFrames = mainFrameSecs.get(0);
				Map<String, String> mainFramesMap = mainFrames.getEntries();
				List<String> mainFrameKeys = new ArrayList<>();
				for(Map.Entry<String, String> me : mainFramesMap.entrySet()) {
					if (me.getKey().startsWith("frame_")) {
						String frameNumber = me.getKey().split("_", 2)[1];
						if (MathUtils.isInteger(frameNumber)) {
							mainFrameKeys.add(me.getKey());
						}
					}
				}
				mainFrameKeys.sort((o1, o2) -> {
					String n1 = o1.split("_", 2)[1];
					String n2 = o2.split("_", 2)[1];
					int i1 = Integer.parseInt(n1);
					int i2 = Integer.parseInt(n2);
					return Integer.compare(i1, i2);
				});
				for (String s : mainFrameKeys) {
					frameNamesMain.add("frames_main/" + mainFramesMap.get(s));
				}

				// INTRO FRAME NAMES
				List<PropertiesSection> introFrameSecs = props.getPropertiesOfType("frames-intro");
				if (!introFrameSecs.isEmpty()) {
					PropertiesSection introFrames = introFrameSecs.get(0);
					Map<String, String> introFramesMap = introFrames.getEntries();
					List<String> introFrameKeys = new ArrayList<>();
					for (Map.Entry<String, String> me : introFramesMap.entrySet()) {
						if (me.getKey().startsWith("frame_")) {
							String frameNumber = me.getKey().split("_", 2)[1];
							if (MathUtils.isInteger(frameNumber)) {
								introFrameKeys.add(me.getKey());
							}
						}
					}
					introFrameKeys.sort((o1, o2) -> {
						String n1 = o1.split("_", 2)[1];
						String n2 = o2.split("_", 2)[1];
						int i1 = Integer.parseInt(n1);
						int i2 = Integer.parseInt(n2);
						return Integer.compare(i1, i2);
					});
					for (String s : introFrameKeys) {
						frameNamesIntro.add("frames_intro/" + introFramesMap.get(s));
					}
				}

				File audio1 = new File(a.getAbsolutePath().replace("\\", "/") + "/audio/mainaudio.wav");
				if (audio1.exists()) {
					mainAudio = audio1.getPath();
				}

				File audio2 = new File(a.getAbsolutePath().replace("\\", "/") + "/audio/introaudio.wav");
				if (audio2.exists()) {
					introAudio = audio2.getPath();
				}

				IAnimationRenderer in = null;
				IAnimationRenderer an = null;

				if (!frameNamesIntro.isEmpty() && !frameNamesMain.isEmpty()) {
					in = new ResourcePackAnimationRenderer(resourceNamespace, frameNamesIntro, fps, loop, 0, 0, 100, 100);
					an = new ResourcePackAnimationRenderer(resourceNamespace, frameNamesMain, fps, loop, 0, 0, 100, 100);
				} else if (!frameNamesMain.isEmpty()) {
					an = new ResourcePackAnimationRenderer(resourceNamespace, frameNamesMain, fps, loop, 0, 0, 100, 100);
				}

				try {
					if (in != null) {
						AdvancedAnimation ani = new AdvancedAnimation(in, an, introAudio, mainAudio, replayIntro);
						ani.propertiesPath = a.getPath();
						register(ani, name, AnimationData.Type.EXTERNAL);
						ani.prepareAnimation();
						LOGGER.info("[FANCYMENU] Animation found: " + name);
					} else if (an != null) {
						AdvancedAnimation ani = new AdvancedAnimation(null, an, introAudio, mainAudio, false);
						ani.propertiesPath = a.getPath();
						register(ani, name, AnimationData.Type.EXTERNAL);
						ani.prepareAnimation();
						LOGGER.info("[FANCYMENU] Animation found:  " + name);
					} else {
						LOGGER.error("[FANCYMENU] Failed to register animation: " + name);
					}
				} catch (AnimationNotFoundException e) {
					e.printStackTrace();
				}
			}
		}
	}

	@NotNull
	public static List<String> getExternalAnimationNames() {
		return new ArrayList<>(EXTERNAL_ANIMATION_NAMES);
	}

	private static void clearExternalAnimations() {
		for (String s : EXTERNAL_ANIMATION_NAMES) {
			ANIMATIONS.remove(s);
		}
	}

	public static boolean animationExists(@NotNull String name) {
		return ANIMATIONS.containsKey(name);
	}

	@NotNull
	public static List<IAnimationRenderer> getAnimations() {
		List<IAnimationRenderer> renderers = new ArrayList<>();
		for (Map.Entry<String, AnimationData> m : ANIMATIONS.entrySet()) {
			renderers.add(m.getValue().animation);
		}
		return renderers;
	}

	@Nullable
	public static IAnimationRenderer getAnimation(String name) {
		if (animationExists(name)) {
			return ANIMATIONS.get(name).animation;
		}
		return null;
	}

	public static void resetAnimations() {
		for (AnimationData d : ANIMATIONS.values()) {
			d.animation.resetAnimation();
		}
	}

	public static void resetAnimationSounds() {
		for (AnimationData d : ANIMATIONS.values()) {
			if (d.animation instanceof AdvancedAnimation) {
				((AdvancedAnimation)d.animation).resetAudio();
			}
		}
	}

	public static void stopAnimationSounds() {
		for (AnimationData d : ANIMATIONS.values()) {
			if (d.animation instanceof AdvancedAnimation) {
				((AdvancedAnimation)d.animation).stopAudio();
			}
		}
	}

	public static void updateAnimationSizes() {
		for (IAnimationRenderer a : getAnimations()) {
			if (a instanceof ResourcePackAnimationRenderer) {
				((ResourcePackAnimationRenderer) a).setupAnimationSize();
			} else if (a instanceof AdvancedAnimation) {
				IAnimationRenderer main = ((AdvancedAnimation) a).getMainAnimationRenderer();
				if (main instanceof ResourcePackAnimationRenderer) {
					((ResourcePackAnimationRenderer) main).setupAnimationSize();
				}
				IAnimationRenderer intro = ((AdvancedAnimation) a).getIntroAnimationRenderer();
				if (intro instanceof ResourcePackAnimationRenderer) {
					((ResourcePackAnimationRenderer) intro).setupAnimationSize();
				}
			}
		}
	}

	public static void preloadAnimations(boolean ignoreAlreadyPreloaded) {

		boolean errors = false;

		if (!preloadCompleted || ignoreAlreadyPreloaded) {

			LOGGER.info("[FANCYMENU] Preloading animations! This could cause the loading screen to freeze for a while..");

			try {
				List<ResourcePackAnimationRenderer> l = new ArrayList<>();
				for (IAnimationRenderer r : AnimationHandler.getAnimations()) {
					if (r instanceof AdvancedAnimation) {
						IAnimationRenderer main = ((AdvancedAnimation) r).getMainAnimationRenderer();
						IAnimationRenderer intro = ((AdvancedAnimation) r).getIntroAnimationRenderer();
						if (main instanceof ResourcePackAnimationRenderer) {
							l.add((ResourcePackAnimationRenderer) main);
						}
						if (intro instanceof ResourcePackAnimationRenderer) {
							l.add((ResourcePackAnimationRenderer) intro);
						}
					} else if (r instanceof ResourcePackAnimationRenderer) {
						l.add((ResourcePackAnimationRenderer) r);
					}
				}
				for (ResourcePackAnimationRenderer r : l) {
					for (ResourceLocation rl : r.getAnimationFrames()) {
						TextureManager t = Minecraft.getInstance().getTextureManager();
						//This is to trigger the texture registration in TextureManager#getTexture
						t.getTexture(rl);
					}
				}
			} catch (Exception ex) {
				ex.printStackTrace();
				errors = true;
			}

			if (!errors) {
				LOGGER.info("[FANCYMENU] Finished preloading animations!");
			} else {
				LOGGER.warn("[FANCYMENU] Finished preloading animations with errors! Check your animations!");
			}

			preloadCompleted = true;

		}

	}

	public static boolean preloadingCompleted() {
		return preloadCompleted;
	}

	@Deprecated
	public static boolean isReady() {
		return true;
	}

	@Deprecated
	public static void setReady(boolean b) {}

}
