package de.keksuccino.fancymenu.menu.fancy.helper.ui;

import de.keksuccino.fancymenu.FancyMenu;
import de.keksuccino.konkrete.config.ConfigEntry;
import de.keksuccino.konkrete.gui.screens.ConfigScreen;
import de.keksuccino.konkrete.localization.Locals;
import net.minecraft.client.gui.screens.Screen;

public class FMConfigScreen extends ConfigScreen {

	public FMConfigScreen(Screen parent) {
		super(FancyMenu.getConfig(), Locals.localize("fancymenu.config"), parent);
	}
	
	@Override
	protected void init() {
		super.init();
		
		for (String s : this.config.getCategorys()) {
			this.setCategoryDisplayName(s, Locals.localize("fancymenu.config.categories." + s));
		}
		
		for (ConfigEntry e : this.config.getAllAsEntry()) {
			this.setValueDisplayName(e.getName(), Locals.localize("fancymenu.config." + e.getName()));
			this.setValueDescription(e.getName(), Locals.localize("fancymenu.config." + e.getName() + ".desc"));
		}
		
	}
	
}
