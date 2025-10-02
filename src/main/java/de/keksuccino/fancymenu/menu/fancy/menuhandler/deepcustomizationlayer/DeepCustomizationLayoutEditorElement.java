package de.keksuccino.fancymenu.menu.fancy.menuhandler.deepcustomizationlayer;

import net.minecraft.client.gui.GuiGraphics;
import de.keksuccino.fancymenu.FancyMenu;
import de.keksuccino.fancymenu.menu.fancy.helper.layoutcreator.LayoutEditorScreen;
import de.keksuccino.fancymenu.menu.fancy.helper.layoutcreator.content.LayoutElement;
import de.keksuccino.fancymenu.menu.fancy.helper.ui.popup.FMNotificationPopup;
import de.keksuccino.konkrete.gui.screens.popup.PopupHandler;
import de.keksuccino.konkrete.input.MouseInput;
import de.keksuccino.konkrete.input.StringUtils;
import de.keksuccino.konkrete.localization.Locals;
import de.keksuccino.konkrete.properties.PropertiesSection;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public abstract class DeepCustomizationLayoutEditorElement extends LayoutElement {

    private static final Logger LOGGER = LogManager.getLogger("fancymenu/DeepCustomizationLayoutEditorElement");

    public final DeepCustomizationElement parentDeepCustomizationElement;

    protected int dragMouseX = -1000;
    protected int dragMouseY = -1000;

    public DeepCustomizationLayoutEditorElement(@Nonnull DeepCustomizationElement parentDeepCustomizationElement, @Nonnull DeepCustomizationItem customizationItemInstance, boolean destroyable, @Nonnull LayoutEditorScreen handler, boolean doInit) {
        super(customizationItemInstance, destroyable, handler, doInit);
        this.parentDeepCustomizationElement = parentDeepCustomizationElement;
    }

    public DeepCustomizationLayoutEditorElement(@Nonnull DeepCustomizationElement parentDeepCustomizationElement, @Nonnull DeepCustomizationItem customizationItemInstance, boolean destroyable, @Nonnull LayoutEditorScreen handler) {
        super(customizationItemInstance, destroyable, handler);
        this.parentDeepCustomizationElement = parentDeepCustomizationElement;
    }

    public void deepCustomizationPreInit() {
        this.stretchable = false;
        this.orderable = false;
        this.fadeable = false;
        this.copyable = false;
        this.delayable = false;
        this.resizeable = false;
        this.dragable = false;
        this.orientationCanBeChanged = false;
        this.enableVisibilityRequirements = false;
        this.enableElementIdCopyButton = false;
        this.supportsAdvancedPositioning = false;
        this.supportsAdvancedSizing = false;
    }

    @Override
    public void init() {
        this.deepCustomizationPreInit();
        super.init();
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY) {

        if (!this.getDeepCustomizationItem().hidden) {
            super.render(graphics, mouseX, mouseY);
        } else {
            if (this.handler.isFocused(this)) {
                this.handler.setObjectFocused(this, false, true);
            }
            this.hovered = false;
            if (this.rightclickMenu.isOpen()) {
                this.rightclickMenu.closeMenu();
            }
        }

        this.handleMoveWarning();

    }

    protected void handleMoveWarning() {
        if (!this.dragable) {
            if (MouseInput.isLeftMouseDown() && this.handler.isFocused(this) && this.hovered) {
                int mX = MouseInput.getMouseX();
                int mY = MouseInput.getMouseY();
                if ((this.dragMouseX == -1000) && (this.dragMouseY == -1000)) {
                    this.dragMouseX = mX;
                    this.dragMouseY = mY;
                }
                if ((mX != this.dragMouseX) || (mY != this.dragMouseY)) {
                    if (FancyMenu.getConfig().getOrDefault("showvanillamovewarning", true)) {
                        FMNotificationPopup p;
                        if (this.orientationCanBeChanged) {
                            p = new FMNotificationPopup(300, new Color(0, 0, 0, 0), 240, null, StringUtils.splitLines(Locals.localize("fancymenu.helper.editor.element.vanilla.orientation_needed"), "%n%"));
                        } else {
                            p = new FMNotificationPopup(300, new Color(0, 0, 0, 0), 240, null, StringUtils.splitLines(Locals.localize("fancymenu.helper.editor.element.moving_not_allowed"), "%n%"));
                        }
                        PopupHandler.displayPopup(p);
                    }
                }
                this.dragMouseX = mX;
                this.dragMouseY = mY;
            } else {
                this.dragMouseX = -1000;
                this.dragMouseY = -1000;
            }
        }
    }

    @Override
    public boolean isHoveredOrFocused() {
        if (this.getDeepCustomizationItem().hidden) {
            return false;
        }
        return super.isHoveredOrFocused();
    }

    @Override
    public boolean isLeftClicked() {
        if (this.getDeepCustomizationItem().hidden) {
            return false;
        }
        return super.isLeftClicked();
    }

    @Override
    public boolean isRightClicked() {
        if (this.getDeepCustomizationItem().hidden) {
            return false;
        }
        return super.isRightClicked();
    }

    @Override
    protected void renderBorder(GuiGraphics graphics, int mouseX, int mouseY) {
        String cachedOri = this.object.orientation;
        if (!this.orientationCanBeChanged) {
            this.object.orientation = "original";
        }
        super.renderBorder(graphics, mouseX, mouseY);
        this.object.orientation = cachedOri;
    }

    @Override
    public void destroyObject() {
        if (this.isDestroyable()) {
            if (FancyMenu.getConfig().getOrDefault("editordeleteconfirmation", true)) {
                FMNotificationPopup pop = new FMNotificationPopup(300, new Color(0,0,0,0), 240, null, StringUtils.splitLines(Locals.localize("fancymenu.helper.editor.element.vanilla.delete.confirm"), "%n%"));
                PopupHandler.displayPopup(pop);
            }
            if (!this.getDeepCustomizationItem().hidden) {
                this.handler.history.saveSnapshot(this.handler.history.createSnapshot());
            }
            this.getDeepCustomizationItem().hidden = true;
            this.handler.setObjectFocused(this, false, true);
            this.resetObjectStates();
        }
    }

    public DeepCustomizationItem getDeepCustomizationItem() {
        return (DeepCustomizationItem) this.object;
    }

    public abstract SimplePropertiesSection serializeItem();

    @Override
    public List<PropertiesSection> getProperties() {
        List<PropertiesSection> l = new ArrayList<>();
        PropertiesSection sec = this.serializeItem();
        if (sec == null) {
            sec = new SimplePropertiesSection();
        }
        if (sec.hasEntry("action")) {
            LOGGER.warn("WARN: Entry key 'action' for serialized customization item instances is reserved by the system. Overriding entry!");
            sec.removeEntry("action");
        }
        sec.addEntry("action", "deep_customization_element:" + this.parentDeepCustomizationElement.getIdentifier());
        sec.addEntry("actionid", this.object.getActionId());
        if (this.object.delayAppearance) {
            sec.addEntry("delayappearance", "true");
            sec.addEntry("delayappearanceeverytime", "" + this.object.delayAppearanceEverytime);
            sec.addEntry("delayappearanceseconds", "" + this.object.delayAppearanceSec);
            if (this.object.fadeIn) {
                sec.addEntry("fadein", "true");
                sec.addEntry("fadeinspeed", "" + this.object.fadeInSpeed);
            }
        }
        sec.addEntry("x", "" + this.object.posX);
        sec.addEntry("y", "" + this.object.posY);
        sec.addEntry("orientation", this.object.orientation);
        if (this.object.orientation.equals("element") && (this.object.orientationElementIdentifier != null)) {
            sec.addEntry("orientation_element", this.object.orientationElementIdentifier);
        }
        if (this.stretchX) {
            sec.addEntry("x", "0");
            sec.addEntry("width", "%guiwidth%");
        } else {
            sec.addEntry("x", "" + this.object.posX);
            sec.addEntry("width", "" + this.object.getWidth());
        }
        if (this.stretchY) {
            sec.addEntry("y", "0");
            sec.addEntry("height", "%guiheight%");
        } else {
            sec.addEntry("y", "" + this.object.posY);
            sec.addEntry("height", "" + this.object.getHeight());
        }
        sec.addEntry("hidden", "" + this.getDeepCustomizationItem().hidden);
        this.addLoadingRequirementPropertiesTo(sec);
        l.add(sec);
        return l;
    }

    public class SimplePropertiesSection extends PropertiesSection {

        public SimplePropertiesSection() {
            super("customization");
        }

    }

}
