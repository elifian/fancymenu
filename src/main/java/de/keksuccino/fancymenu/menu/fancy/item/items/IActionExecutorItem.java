package de.keksuccino.fancymenu.menu.fancy.item.items;

import de.keksuccino.fancymenu.menu.button.ButtonScriptEngine;

import java.util.List;

public interface IActionExecutorItem {

    List<ButtonScriptEngine.ActionContainer> getActionList();

}
