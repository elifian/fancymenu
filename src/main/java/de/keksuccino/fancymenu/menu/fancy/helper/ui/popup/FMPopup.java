package de.keksuccino.fancymenu.menu.fancy.helper.ui.popup;

import de.keksuccino.fancymenu.menu.fancy.helper.ui.UIBase;
import de.keksuccino.konkrete.gui.content.AdvancedButton;
import de.keksuccino.konkrete.gui.screens.popup.Popup;

public class FMPopup extends Popup {

	public FMPopup(int backgroundAlpha) {
		super(backgroundAlpha);
	}
	
	@Override
	protected void colorizePopupButton(AdvancedButton b) {
		UIBase.colorizeButton(b);
	}

}
