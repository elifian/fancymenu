package de.keksuccino.fancymenu.menu.fancy;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import de.keksuccino.fancymenu.FancyMenu;
import de.keksuccino.konkrete.Konkrete;
import de.keksuccino.konkrete.input.StringUtils;
import de.keksuccino.konkrete.math.MathUtils;
import de.keksuccino.konkrete.properties.PropertiesSection;
import de.keksuccino.konkrete.properties.PropertiesSerializer;
import de.keksuccino.konkrete.properties.PropertiesSet;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.versions.mcp.MCPVersion;

@SuppressWarnings("all")
public class MenuCustomizationProperties {
	
	private static List<PropertiesSet> properties = new ArrayList<PropertiesSet>();
	private static List<PropertiesSet> disabled = new ArrayList<PropertiesSet>();

	public static void loadProperties() {
		File f = FancyMenu.getCustomizationPath();
		properties = parsePropertiesFromDir(f);

		File f3 = new File(FancyMenu.getCustomizationPath().getPath() + "/.disabled");
		disabled = parsePropertiesFromDir(f3);
	}

	public static List<PropertiesSet> parsePropertiesFromDir(File dir) {
		List<PropertiesSet> props = new ArrayList<PropertiesSet>();

		if (!dir.exists()) {
			dir.mkdirs();
		}
		
		for (File f2 : dir.listFiles()) {
			if (f2.getPath().toLowerCase().endsWith(".txt")) {
				PropertiesSet s = PropertiesSerializer.getProperties(f2.getAbsolutePath().replace("\\", "/"));
				if ((s != null) && s.getPropertiesType().equalsIgnoreCase("menu")) {
					List<PropertiesSection> l = s.getPropertiesOfType("customization-meta");
					if (l.isEmpty()) {
						l = s.getPropertiesOfType("type-meta");
					}
					if (!l.isEmpty()) {
						String s2 = l.get(0).getEntryValue("identifier");
						String s3 = l.get(0).getEntryValue("requiredmods");
						String s4 = l.get(0).getEntryValue("minimumfmversion");
						String s5 = l.get(0).getEntryValue("maximumfmversion");
						String s6 = l.get(0).getEntryValue("minimummcversion");
						String s7 = l.get(0).getEntryValue("maximummcversion");
						
						if (s2 == null) {
							continue;
						}
						if (!isVersionCompatible(s4, s5, FancyMenu.VERSION)) {
							continue;
						}
						if (!isVersionCompatible(s6, s7, MCPVersion.getMCVersion())) {
							continue;
						}
						if (!allRequiredModsLoaded(s3)) {
							continue;
						}

						l.get(0).removeEntry("identifier");
						l.get(0).addEntry("identifier", MenuCustomization.getValidMenuIdentifierFor(s2));
						
						l.get(0).addEntry("path", f2.getPath());
						props.add(s);
					}
				}
			}
		}
		
		return props;
	}
	
	private static String fillUpToLength(String s, String fillWith, int length) {
		String out = s;
		int add = length - s.length();
		for (int i = 1; i <= add; i++) {
			out += fillWith;
		}
		return out;
	}
	
	private static boolean isVersionCompatible(String minimum, String maximum, String version) {
		if (version == null) {
			return true;
		}
		String versionRaw = fillUpToLength(StringUtils.replaceAllExceptOf(version, "", "0", "1", "2", "3", "4", "5", "6", "7", "8", "9"), "0", 9);

		if (MathUtils.isInteger(versionRaw)) {
			int ver = Integer.parseInt(versionRaw);
			
			if (minimum != null) {
				String minShort = StringUtils.replaceAllExceptOf(minimum, "", "0", "1", "2", "3", "4", "5", "6", "7", "8", "9");
				if ((minShort.length() > 0) && MathUtils.isInteger(minShort)) {
					String minRaw = fillUpToLength(minShort, "0", 9);
					int min = Integer.parseInt(minRaw);
					if (ver < min) {
						return false;
					}
				}
			}
			
			if (maximum != null) {
				String maxShort = StringUtils.replaceAllExceptOf(maximum, "", "0", "1", "2", "3", "4", "5", "6", "7", "8", "9");
				if ((maxShort.length() > 0) && MathUtils.isInteger(maxShort)) {
					String maxRaw = fillUpToLength(maxShort, "0", 9);
					int max = Integer.parseInt(maxRaw);
					if (ver > max) {
						return false;
					}
				}
			}
		}
		return true;
	}
	
	private static boolean allRequiredModsLoaded(String requiredMods) {
		if ((requiredMods == null) || (requiredMods.replace(" ", "").length() == 0)) {
			return true;
		}
		List<String> mods = new ArrayList<String>();
		if (requiredMods.contains(",")) {
			for (String s : requiredMods.replace(" ", "").split("[,]")) {
				if (s.length() > 0) {
					mods.add(s);
				}
			}
		} else {
			mods.add(requiredMods.replace(" ", ""));
		}
		for (String s : mods) {
			if (s.equals("optifine")) {
				if (!Konkrete.isOptifineLoaded) {
					return false;
				}
			} else {
				if (!ModList.get().isLoaded(s)) {
					return false;
				}
			}
		}
		return true;
	}
	
	public static List<PropertiesSet> getProperties() {
		return properties;
	}

	public static List<PropertiesSet> getDisabledProperties() {
		return disabled;
	}
	
	public static List<PropertiesSet> getPropertiesWithIdentifier(String identifier) {
		List<PropertiesSet> l = new ArrayList<PropertiesSet>();
		for (PropertiesSet s : getProperties()) {
			List<PropertiesSection> l2 = s.getPropertiesOfType("customization-meta");
			if (l2.isEmpty()) {
				l2 = s.getPropertiesOfType("type-meta");
			}
			if (l2.isEmpty()) {
				continue;
			}
			String s2 = l2.get(0).getEntryValue("identifier");
			if (s2 != null) {
				if (s2.equalsIgnoreCase(identifier)) {
					l.add(s);
				} else if (s2.equals("%fancymenu:universal_layout%")) {
					String whitelistRaw = l2.get(0).getEntryValue("universal_layout_whitelist");
					String blacklistRaw = l2.get(0).getEntryValue("universal_layout_blacklist");
					List<String> whitelist = new ArrayList<>();
					List<String> blacklist = new ArrayList<>();
					if ((whitelistRaw != null) && whitelistRaw.contains(";")) {
						for (String s3 : whitelistRaw.split("[;]")) {
							if (s3.length() > 0) {
								whitelist.add(MenuCustomization.getValidMenuIdentifierFor(s3));
							}
						}
					}
					if ((blacklistRaw != null) && blacklistRaw.contains(";")) {
						for (String s3 : blacklistRaw.split("[;]")) {
							if (s3.length() > 0) {
								blacklist.add(MenuCustomization.getValidMenuIdentifierFor(s3));
							}
						}
					}
					if (!whitelist.isEmpty() || !blacklist.isEmpty()) {
						if (!whitelist.isEmpty() && whitelist.contains(identifier)) {
							l.add(s);
						} else if (!blacklist.isEmpty() && !blacklist.contains(identifier)) {
							l.add(s);
						}
					} else {
						l.add(s);
					}
				}
			}
		}
		return l;
	}

	public static List<PropertiesSet> getDisabledPropertiesWithIdentifier(String identifier) {
		List<PropertiesSet> l = new ArrayList<PropertiesSet>();
		for (PropertiesSet s : getDisabledProperties()) {
			List<PropertiesSection> l2 = s.getPropertiesOfType("customization-meta");
			if (l2.isEmpty()) {
				l2 = s.getPropertiesOfType("type-meta");
			}
			if (l2.isEmpty()) {
				continue;
			}
			String s2 = l2.get(0).getEntryValue("identifier");
			if (s2.equalsIgnoreCase(identifier)) {
				l.add(s);
			}
		}
		return l;
	}

	public static List<LayoutProperties> getAsLayoutProperties(List<PropertiesSet> propsList) {
		List<LayoutProperties> l = new ArrayList<>();
		propsList.forEach((props) -> {
			l.add(new LayoutProperties(props));
		});
		return l;
	}

	public static class LayoutProperties {

		public PropertiesSet properties;
		public String menuIdentifier;
		public String path;

		public LayoutProperties(PropertiesSet props) {

			List<PropertiesSection> l = props.getPropertiesOfType("customization-meta");
			if (!l.isEmpty()) {
				PropertiesSection meta = l.get(0);
				this.path = meta.getEntryValue("path");
				this.menuIdentifier = meta.getEntryValue("identifier");
			}

		}

	}

}
