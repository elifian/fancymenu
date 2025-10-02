package de.keksuccino.fancymenu.menu.fancy.helper.ui.texteditor;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import de.keksuccino.fancymenu.menu.fancy.helper.ui.FMContextMenu;
import de.keksuccino.fancymenu.menu.fancy.helper.ui.UIBase;
import de.keksuccino.fancymenu.menu.fancy.helper.ui.scroll.scrollbar.ScrollBar;
import de.keksuccino.fancymenu.menu.fancy.helper.ui.texteditor.formattingrules.TextEditorFormattingRules;
import de.keksuccino.fancymenu.menu.placeholder.v2.Placeholder;
import de.keksuccino.fancymenu.menu.placeholder.v2.PlaceholderRegistry;
import de.keksuccino.fancymenu.mixin.client.IMixinEditBox;
import de.keksuccino.konkrete.gui.content.AdvancedButton;
import de.keksuccino.konkrete.input.CharacterFilter;
import de.keksuccino.konkrete.input.MouseInput;
import de.keksuccino.konkrete.input.StringUtils;
import de.keksuccino.konkrete.localization.Locals;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.Mth;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

@SuppressWarnings("all")
public class TextEditorScreen extends Screen {

    private static final Logger LOGGER = LogManager.getLogger();

    public Screen parentScreen;
    public CharacterFilter characterFilter;
    public Consumer<String> callback;
    public List<TextEditorLine> textFieldLines = new ArrayList<>();
    public ScrollBar verticalScrollBar = new ScrollBar(ScrollBar.ScrollBarDirection.VERTICAL, UIBase.VERTICAL_SCROLL_BAR_WIDTH, UIBase.VERTICAL_SCROLL_BAR_HEIGHT, 0, 0, 0, 0, (Color) null, null);
    public ScrollBar horizontalScrollBar = new ScrollBar(ScrollBar.ScrollBarDirection.HORIZONTAL, UIBase.HORIZONTAL_SCROLL_BAR_WIDTH, UIBase.HORIZONTAL_SCROLL_BAR_HEIGHT, 0, 0, 0, 0, (Color) null, null);
    public ScrollBar verticalScrollBarPlaceholderMenu = new ScrollBar(ScrollBar.ScrollBarDirection.VERTICAL, UIBase.VERTICAL_SCROLL_BAR_WIDTH, UIBase.VERTICAL_SCROLL_BAR_HEIGHT, 0, 0, 0, 0, (Color) null, null);
    public ScrollBar horizontalScrollBarPlaceholderMenu = new ScrollBar(ScrollBar.ScrollBarDirection.HORIZONTAL, UIBase.HORIZONTAL_SCROLL_BAR_WIDTH, UIBase.HORIZONTAL_SCROLL_BAR_HEIGHT, 0, 0, 0, 0, (Color) null, null);
    public FMContextMenu rightClickContextMenu;
    public AdvancedButton cancelButton;
    public AdvancedButton doneButton;
    public AdvancedButton placeholderButton;
    public int lastCursorPosSetByUser = 0;
    public boolean justSwitchedLineByWordDeletion = false;
    public boolean triggeredFocusedLineWasTooHighInCursorPosMethod = false;
    public int headerHeight = 50;
    public int footerHeight = 50;
    public int borderLeft = 40;
    public int borderRight = 20;
    public int lineHeight = 14;
    public Color screenBackgroundColor = UIBase.SCREEN_BACKGROUND_COLOR;
    public Color editorAreaBorderColor = UIBase.ELEMENT_BORDER_COLOR_IDLE;
    public Color editorAreaBackgroundColor = UIBase.AREA_BACKGROUND_COLOR;
    public Color textColor = UIBase.TEXT_COLOR_GRAY_1;
    public Color focusedLineColor = UIBase.ENTRY_COLOR_FOCUSED;
    public Color scrollGrabberIdleColor = UIBase.SCROLL_GRABBER_IDLE_COLOR;
    public Color scrollGrabberHoverColor = UIBase.SCROLL_GRABBER_HOVER_COLOR;
    public Color sideBarColor = UIBase.SIDE_BAR_COLOR;
    public Color lineNumberTextColorNormal = UIBase.TEXT_COLOR_GREY_2;
    public Color lineNumberTextColorFocused = UIBase.TEXT_COLOR_GREY_3;
    public Color multilineNotSupportedNotificationColor = UIBase.TEXT_COLOR_RED_1;
    public Color placeholderEntryBackgroundColorIdle = UIBase.AREA_BACKGROUND_COLOR;
    public Color placeholderEntryBackgroundColorHover = UIBase.ENTRY_COLOR_FOCUSED;
    public Color placeholderEntryDotColorPlaceholder = UIBase.LISTING_DOT_BLUE;
    public Color placeholderEntryDotColorCategory = UIBase.LISTING_DOT_RED;
    public Color placeholderEntryLabelColor = UIBase.TEXT_COLOR_GRAY_1;
    public Color placeholderEntryBackToCategoriesLabelColor = UIBase.TEXT_COLOR_ORANGE_1;
    public int currentLineWidth;
    public int lastTickFocusedLineIndex = -1;
    public TextEditorLine startHighlightLine = null;
    public int startHighlightLineIndex = -1;
    public int endHighlightLineIndex = -1;
    public int overriddenTotalScrollHeight = -1;
    public List<Runnable> lineNumberRenderQueue = new ArrayList<>();
    public List<TextEditorFormattingRule> formattingRules = new ArrayList<>();
    public int currentRenderCharacterIndexTotal = 0;
    public static boolean showPlaceholderMenu = false;
    public int placeholderMenuWidth = 120;
    public int placeholderMenuEntryHeight = 16;
    public List<PlaceholderMenuEntry> placeholderMenuEntries = new ArrayList<>();
    public boolean multilineMode = true;
    public long multilineNotSupportedNotificationDisplayStart = -1L;
    public boolean boldTitle = true;

    public TextEditorScreen(@Nullable Screen parent, @Nullable CharacterFilter characterFilter, Consumer<String> callback) {
        this(null, parent, characterFilter, callback);
    }

    public TextEditorScreen(@Nullable Component title, @Nullable Screen parent, @Nullable CharacterFilter characterFilter, Consumer<String> callback) {
        super((title != null) ? title : Component.literal(""));
        this.minecraft = Minecraft.getInstance();
        this.font = Minecraft.getInstance().font;
        this.parentScreen = parent;
        this.characterFilter = characterFilter;
        this.callback = callback;
        this.addLine();
        this.getLine(0).setFocused(true);
        this.verticalScrollBar.setScrollWheelAllowed(true);
        this.verticalScrollBarPlaceholderMenu.setScrollWheelAllowed(true);
        this.updateRightClickContextMenu();
        this.formattingRules.addAll(TextEditorFormattingRules.getRules());
        this.updatePlaceholderEntries(null, true, true);
        this.updateCurrentLineWidth();
    }

    @Override
    public void init() {

        //Reset the GUI scale in case the layout editor changed it
        Minecraft.getInstance().getWindow().setGuiScale(Minecraft.getInstance().getWindow().calculateScale(Minecraft.getInstance().options.guiScale().get(), Minecraft.getInstance().isEnforceUnicode()));
        this.height = Minecraft.getInstance().getWindow().getGuiScaledHeight();
        this.width = Minecraft.getInstance().getWindow().getGuiScaledWidth();

        super.init();

        this.verticalScrollBar.scrollAreaStartX = this.getEditorAreaX() + 1;
        this.verticalScrollBar.scrollAreaStartY = this.getEditorAreaY() + 1;
        this.verticalScrollBar.scrollAreaEndX = this.getEditorAreaX() + this.getEditorAreaWidth() - 2;
        this.verticalScrollBar.scrollAreaEndY = this.getEditorAreaY() + this.getEditorAreaHeight() - this.horizontalScrollBar.grabberHeight - 2;

        this.horizontalScrollBar.scrollAreaStartX = this.getEditorAreaX() + 1;
        this.horizontalScrollBar.scrollAreaStartY = this.getEditorAreaY() + 1;
        this.horizontalScrollBar.scrollAreaEndX = this.getEditorAreaX() + this.getEditorAreaWidth() - this.verticalScrollBar.grabberWidth - 2;
        this.horizontalScrollBar.scrollAreaEndY = this.getEditorAreaY() + this.getEditorAreaHeight() - 1;

        int placeholderAreaX = this.width - this.borderRight - this.placeholderMenuWidth;
        int placeholderAreaY = this.getEditorAreaY();

        this.verticalScrollBarPlaceholderMenu.scrollAreaStartX = placeholderAreaX + 1;
        this.verticalScrollBarPlaceholderMenu.scrollAreaStartY = placeholderAreaY + 1;
        this.verticalScrollBarPlaceholderMenu.scrollAreaEndX = placeholderAreaX + this.placeholderMenuWidth - 2;
        this.verticalScrollBarPlaceholderMenu.scrollAreaEndY = placeholderAreaY + this.getEditorAreaHeight() - this.horizontalScrollBarPlaceholderMenu.grabberHeight - 2;

        this.horizontalScrollBarPlaceholderMenu.scrollAreaStartX = placeholderAreaX + 1;
        this.horizontalScrollBarPlaceholderMenu.scrollAreaStartY = placeholderAreaY + 1;
        this.horizontalScrollBarPlaceholderMenu.scrollAreaEndX = placeholderAreaX + this.placeholderMenuWidth - this.verticalScrollBarPlaceholderMenu.grabberWidth - 2;
        this.horizontalScrollBarPlaceholderMenu.scrollAreaEndY = placeholderAreaY + this.getEditorAreaHeight() - 1;

        //Set scroll grabber colors
        this.verticalScrollBar.idleBarColor = this.scrollGrabberIdleColor;
        this.verticalScrollBar.hoverBarColor = this.scrollGrabberHoverColor;
        this.horizontalScrollBar.idleBarColor = this.scrollGrabberIdleColor;
        this.horizontalScrollBar.hoverBarColor = this.scrollGrabberHoverColor;

        //Set placeholder menu scroll bar colors
        this.verticalScrollBarPlaceholderMenu.idleBarColor = this.scrollGrabberIdleColor;
        this.verticalScrollBarPlaceholderMenu.hoverBarColor = this.scrollGrabberHoverColor;
        this.horizontalScrollBarPlaceholderMenu.idleBarColor = this.scrollGrabberIdleColor;
        this.horizontalScrollBarPlaceholderMenu.hoverBarColor = this.scrollGrabberHoverColor;

        this.cancelButton = new AdvancedButton(this.width - this.borderRight - 100 - 5 - 100, this.height - 35, 100, 20, Locals.localize("fancymenu.guicomponents.cancel"), true, (button) -> {
            this.onClose();
        });
        UIBase.applyDefaultButtonSkinTo(this.cancelButton);

        this.doneButton = new AdvancedButton(this.width - this.borderRight - 100, this.height - 35, 100, 20, Locals.localize("fancymenu.guicomponents.done"), true, (button) -> {
            Minecraft.getInstance().setScreen(this.parentScreen);
            if (this.callback != null) {
                this.callback.accept(this.getText());
            }
        });
        UIBase.applyDefaultButtonSkinTo(this.doneButton);

        this.placeholderButton = new AdvancedButton(this.width - this.borderRight - 100, (this.headerHeight / 2) - 10, 100, 20, Locals.localize("fancymenu.ui.text_editor.placeholders"), true, (button) -> {
            if (showPlaceholderMenu) {
                showPlaceholderMenu = false;
            } else {
                showPlaceholderMenu = true;
            }
            this.rebuildWidgets();
        });
        this.placeholderButton.setDescription(StringUtils.splitLines(Locals.localize("helper.ui.dynamicvariabletextfield.variables.desc"), "%n%"));
        UIBase.applyDefaultButtonSkinTo(this.placeholderButton);
        if (showPlaceholderMenu) {
            this.placeholderButton.setBackgroundColor(UIBase.getButtonIdleColor(), UIBase.getButtonHoverColor(), this.editorAreaBorderColor, this.editorAreaBorderColor, 1);
            this.placeholderButton.setHeight(this.getEditorAreaY() - ((this.headerHeight / 2) - 10));
        }

    }

    public void updateRightClickContextMenu() {

        TextEditorLine hoveredLine = this.getHoveredLine();

        if (this.rightClickContextMenu != null) {
            this.rightClickContextMenu.closeMenu();
        }
        this.rightClickContextMenu = new FMContextMenu();

        AdvancedButton cutButton = new AdvancedButton(0, 0, 0, 0, Locals.localize("fancymenu.ui.text_editor.cut"), true, (press) -> {
            Minecraft.getInstance().keyboardHandler.setClipboard(this.cutHighlightedText());
            this.rightClickContextMenu.closeMenu();
        });
        this.rightClickContextMenu.addContent(cutButton);
        if ((hoveredLine == null) || !hoveredLine.isHighlightedHovered()) {
            cutButton.active = false;
        }

        AdvancedButton copyButton = new AdvancedButton(0, 0, 0, 0, Locals.localize("fancymenu.ui.text_editor.copy"), true, (press) -> {
            Minecraft.getInstance().keyboardHandler.setClipboard(this.getHighlightedText());
            this.rightClickContextMenu.closeMenu();
        });
        this.rightClickContextMenu.addContent(copyButton);
        if ((hoveredLine == null) || !hoveredLine.isHighlightedHovered()) {
            copyButton.active = false;
        }

        AdvancedButton pasteButton = new AdvancedButton(0, 0, 0, 0, Locals.localize("fancymenu.ui.text_editor.paste"), true, (press) -> {
            this.pasteText(Minecraft.getInstance().keyboardHandler.getClipboard());
            this.rightClickContextMenu.closeMenu();
        });
        this.rightClickContextMenu.addContent(pasteButton);

        this.rightClickContextMenu.addSeparator();

        AdvancedButton selectAllButton = new AdvancedButton(0, 0, 0, 0, Locals.localize("fancymenu.ui.text_editor.select_all"), true, (press) -> {
            for (TextEditorLine t : this.textFieldLines) {
                t.setHighlightPos(0);
                t.setCursorPosition(t.getValue().length());
            }
            this.setFocusedLine(this.getLineCount()-1);
            this.startHighlightLineIndex = 0;
            this.endHighlightLineIndex = this.getLineCount()-1;
            this.rightClickContextMenu.closeMenu();
        });
        this.rightClickContextMenu.addContent(selectAllButton);

    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partial) {

        //Reset scrolls if content fits editor area
        if (this.currentLineWidth <= this.getEditorAreaWidth()) {
            this.horizontalScrollBar.setScroll(0.0F);
        }
        if (this.getTotalLineHeight() <= this.getEditorAreaHeight()) {
            this.verticalScrollBar.setScroll(0.0F);
        }

        this.justSwitchedLineByWordDeletion = false;

        this.updateCurrentLineWidth();

        //Adjust the scroll wheel speed depending on the amount of lines
        this.verticalScrollBar.setWheelScrollSpeed(1.0F / ((float)this.getTotalScrollHeight() / 500.0F));

        this.renderScreenBackground(graphics);

        this.renderEditorAreaBackground(graphics);

        Window win = Minecraft.getInstance().getWindow();
        double scale = win.getGuiScale();
        int sciBottom = this.height - this.footerHeight;
        //Don't render parts of lines outside of editor area
        RenderSystem.enableScissor((int)(this.borderLeft * scale), (int)(win.getHeight() - (sciBottom * scale)), (int)(this.getEditorAreaWidth() * scale), (int)(this.getEditorAreaHeight() * scale));

        this.formattingRules.forEach((rule) -> rule.resetRule(this));
        this.currentRenderCharacterIndexTotal = 0;
        this.lineNumberRenderQueue.clear();
        //Update positions and size of lines and render them
        this.updateLines((line) -> {
            if (line.isInEditorArea()) {
                this.lineNumberRenderQueue.add(() -> this.renderLineNumber(graphics, line));
            }
            line.render(graphics, mouseX, mouseY, partial);
        });

        RenderSystem.disableScissor();

        this.renderLineNumberBackground(graphics, this.borderLeft);

        RenderSystem.enableScissor(0, (int)(win.getHeight() - (sciBottom * scale)), (int)(this.borderLeft * scale), (int)(this.getEditorAreaHeight() * scale));
        for (Runnable r : this.lineNumberRenderQueue) {
            r.run();
        }
        RenderSystem.disableScissor();

        this.lastTickFocusedLineIndex = this.getFocusedLineIndex();
        this.triggeredFocusedLineWasTooHighInCursorPosMethod = false;

        UIBase.renderBorder(graphics, this.borderLeft-1, this.headerHeight-1, this.getEditorAreaX() + this.getEditorAreaWidth(), this.height - this.footerHeight + 1, 1, this.editorAreaBorderColor, true, true, true, true);

        this.verticalScrollBar.render(graphics);
        this.horizontalScrollBar.render(graphics);

        this.renderPlaceholderMenu(graphics, mouseX, mouseY, partial);

        this.cancelButton.render(graphics, mouseX, mouseY, partial);
        this.doneButton.render(graphics, mouseX, mouseY, partial);

        this.renderMultilineNotSupportedNotification(graphics, mouseX, mouseY, partial);

        UIBase.renderScaledContextMenu(graphics, this.rightClickContextMenu);

        this.tickMouseHighlighting();

        MutableComponent t = this.title.copy();
        t.setStyle(t.getStyle().withBold(this.boldTitle));
        graphics.drawString(this.font, t, this.borderLeft, (this.headerHeight / 2) - (this.font.lineHeight / 2), -1, false);

    }

    protected void renderMultilineNotSupportedNotification(GuiGraphics graphics, int mouseX, int mouseY, float partial) {
        long now = System.currentTimeMillis();
        if (!this.multilineMode && (this.multilineNotSupportedNotificationDisplayStart + 3000L >= now)) {
            int a = 255;
            int diff = (int) (this.multilineNotSupportedNotificationDisplayStart + 3000L - now);
            if (diff <= 1000) {
                float f = (float)diff / 1000F;
                a = Math.max(10, (int)(255F * f));
            }
            Color c = new Color(this.multilineNotSupportedNotificationColor.getRed(), this.multilineNotSupportedNotificationColor.getGreen(), this.multilineNotSupportedNotificationColor.getBlue(), a);
            graphics.drawString(this.font, Locals.localize("fancymenu.ui.text_editor.error.multiline_support"), this.borderLeft, this.headerHeight - this.font.lineHeight - 5, c.getRGB(), false);
        }
    }

    protected void renderPlaceholderMenu(GuiGraphics graphics, int mouseX, int mouseY, float partial) {

        if (showPlaceholderMenu) {

            if (this.getTotalPlaceholderEntriesWidth() <= this.placeholderMenuWidth) {
                this.horizontalScrollBarPlaceholderMenu.setScroll(0.0F);
            }
            if (this.getTotalPlaceholderEntriesHeight() <= this.getEditorAreaHeight()) {
                this.verticalScrollBarPlaceholderMenu.setScroll(0.0F);
            }

            //Render placeholder menu background
            graphics.fill(this.width - this.borderRight - this.placeholderMenuWidth, this.getEditorAreaY(), this.width - this.borderRight, this.getEditorAreaY() + this.getEditorAreaHeight(), this.editorAreaBackgroundColor.getRGB());

            Window win = Minecraft.getInstance().getWindow();
            double scale = win.getGuiScale();
            int sciBottom = this.height - this.footerHeight;
            //Don't render parts of placeholder entries outside of placeholder menu area
            RenderSystem.enableScissor((int)((this.width - this.borderRight - this.placeholderMenuWidth) * scale), (int)(win.getHeight() - (sciBottom * scale)), (int)(this.placeholderMenuWidth * scale), (int)(this.getEditorAreaHeight() * scale));

            //Render placeholder entries
            List<PlaceholderMenuEntry> entries = new ArrayList<>();
            entries.addAll(this.placeholderMenuEntries);
            int index = 0;
            for (PlaceholderMenuEntry e : entries) {
                e.x = (this.width - this.borderRight - this.placeholderMenuWidth) + this.getPlaceholderEntriesRenderOffsetX();
                e.y = this.getEditorAreaY() + (this.placeholderMenuEntryHeight * index) + this.getPlaceholderEntriesRenderOffsetY();
                e.render(graphics, mouseX, mouseY, partial);
                index++;
            }

            RenderSystem.disableScissor();

            //Render placeholder menu border
            UIBase.renderBorder(graphics, this.width - this.borderRight - this.placeholderMenuWidth - 1, this.headerHeight-1, this.width - this.borderRight, this.height - this.footerHeight + 1, 1, this.editorAreaBorderColor, true, true, true, true);

            //Render placeholder menu scroll bars
            this.verticalScrollBarPlaceholderMenu.render(graphics);
            this.horizontalScrollBarPlaceholderMenu.render(graphics);

        }

        this.placeholderButton.render(graphics, mouseX, mouseY, partial);

    }

    public int getTotalPlaceholderEntriesHeight() {
        return this.placeholderMenuEntryHeight * this.placeholderMenuEntries.size();
    }

    public int getTotalPlaceholderEntriesWidth() {
        int i = this.placeholderMenuWidth;
        for (PlaceholderMenuEntry e : this.placeholderMenuEntries) {
            if (e.getWidth() > i) {
                i = e.getWidth();
            }
        }
        return i;
    }

    public int getPlaceholderEntriesRenderOffsetX() {
        int totalScrollWidth = Math.max(0, this.getTotalPlaceholderEntriesWidth() - this.placeholderMenuWidth);
        return -(int)(((float)totalScrollWidth / 100.0F) * (this.horizontalScrollBarPlaceholderMenu.getScroll() * 100.0F));
    }

    public int getPlaceholderEntriesRenderOffsetY() {
        int totalScrollHeight = Math.max(0, this.getTotalPlaceholderEntriesHeight() - this.getEditorAreaHeight());
        return -(int)(((float)totalScrollHeight / 100.0F) * (this.verticalScrollBarPlaceholderMenu.getScroll() * 100.0F));
    }

    public void updatePlaceholderEntries(@Nullable String category, boolean clearList, boolean addBackButton) {

        if (clearList) {
            this.placeholderMenuEntries.clear();
        }

        Map<String, List<Placeholder>> categories = this.getPlaceholdersOrderedByCategories();
        if (!categories.isEmpty()) {
            List<Placeholder> otherCategory = categories.get(Locals.localize("fancymenu.helper.ui.dynamicvariabletextfield.categories.other"));
            if (otherCategory != null) {

                if (category == null) {

                    //Add category entries
                    for (Map.Entry<String, List<Placeholder>> m : categories.entrySet()) {
                        if (m.getValue() != otherCategory) {
                            PlaceholderMenuEntry entry = new PlaceholderMenuEntry(this, Component.literal(m.getKey()), () -> {
                                this.updatePlaceholderEntries(m.getKey(), true, true);
                            });
                            entry.dotColor = this.placeholderEntryDotColorCategory;
                            entry.entryLabelColor = this.placeholderEntryLabelColor;
                            this.placeholderMenuEntries.add(entry);
                        }
                    }
                    //Add placeholder entries of the "Other" category to the end of the categories list (because other = no category)
                    this.updatePlaceholderEntries(Locals.localize("fancymenu.helper.ui.dynamicvariabletextfield.categories.other"), false, false);

                } else {

                    if (addBackButton) {
                        PlaceholderMenuEntry backToCategoriesEntry = new PlaceholderMenuEntry(this, Component.literal(Locals.localize("fancymenu.ui.text_editor.placeholders.back_to_categories")), () -> {
                            this.updatePlaceholderEntries(null, true, true);
                        });
                        backToCategoriesEntry.dotColor = this.placeholderEntryDotColorCategory;
                        backToCategoriesEntry.entryLabelColor = this.placeholderEntryBackToCategoriesLabelColor;
                        this.placeholderMenuEntries.add(backToCategoriesEntry);
                    }

                    List<Placeholder> placeholders = categories.get(category);
                    if (placeholders != null) {
                        for (Placeholder p : placeholders) {
                            PlaceholderMenuEntry entry = new PlaceholderMenuEntry(this, Component.literal(p.getDisplayName()), () -> {
                                this.pasteText(p.getDefaultPlaceholderString().toString());
                            });
                            List<String> desc = p.getDescription();
                            if (desc != null) {
                                entry.setDescription(desc.toArray(new String[0]));
                            }
                            entry.dotColor = this.placeholderEntryDotColorPlaceholder;
                            entry.entryLabelColor = this.placeholderEntryLabelColor;
                            this.placeholderMenuEntries.add(entry);
                        }
                    }

                }

                for (PlaceholderMenuEntry e : this.placeholderMenuEntries) {
                    e.backgroundColorIdle = this.placeholderEntryBackgroundColorIdle;
                    e.backgroundColorHover = this.placeholderEntryBackgroundColorHover;
                }

                this.verticalScrollBarPlaceholderMenu.setScroll(0.0F);
                this.horizontalScrollBarPlaceholderMenu.setScroll(0.0F);
            }
        }

    }

    protected Map<String, List<Placeholder>> getPlaceholdersOrderedByCategories() {
        //Build lists of all placeholders ordered by categories
        Map<String, List<Placeholder>> categories = new LinkedHashMap<>();
        for (Placeholder p : PlaceholderRegistry.getPlaceholdersList()) {
            String cat = p.getCategory();
            if (cat == null) {
                cat = Locals.localize("fancymenu.helper.ui.dynamicvariabletextfield.categories.other");
            }
            List<Placeholder> l = categories.get(cat);
            if (l == null) {
                l = new ArrayList<>();
                categories.put(cat, l);
            }
            l.add(p);
        }
        //Move the Other category to the end
        List<Placeholder> otherCategory = categories.get(Locals.localize("fancymenu.helper.ui.dynamicvariabletextfield.categories.other"));
        if (otherCategory != null) {
            categories.remove(Locals.localize("fancymenu.helper.ui.dynamicvariabletextfield.categories.other"));
            categories.put(Locals.localize("fancymenu.helper.ui.dynamicvariabletextfield.categories.other"), otherCategory);
        }
        return categories;
    }

    protected void renderLineNumberBackground(GuiGraphics graphics, int width) {
        graphics.fill(this.getEditorAreaX(), this.getEditorAreaY() - 1, this.getEditorAreaX() - width - 1, this.getEditorAreaY() + this.getEditorAreaHeight() + 1, this.sideBarColor.getRGB());
    }

    protected void renderLineNumber(GuiGraphics graphics, TextEditorLine line) {
        String lineNumberString = "" + (line.lineIndex+1);
        int lineNumberWidth = this.font.width(lineNumberString);
        graphics.drawString(this.font, lineNumberString, this.getEditorAreaX() - 3 - lineNumberWidth, line.getY() + (line.getHeight() / 2) - (this.font.lineHeight / 2), line.isFocused() ? this.lineNumberTextColorFocused.getRGB() : this.lineNumberTextColorNormal.getRGB(), false);
    }

    protected void renderEditorAreaBackground(GuiGraphics graphics) {
        graphics.fill(this.getEditorAreaX(), this.getEditorAreaY(), this.getEditorAreaX() + this.getEditorAreaWidth(), this.getEditorAreaY() + this.getEditorAreaHeight(), this.editorAreaBackgroundColor.getRGB());
    }

    protected void renderScreenBackground(GuiGraphics graphics) {
        graphics.fill(0, 0, this.width, this.height, this.screenBackgroundColor.getRGB());
    }

    protected void tickMouseHighlighting() {

        if (!MouseInput.isLeftMouseDown()) {
            this.startHighlightLine = null;
            for (TextEditorLine t : this.textFieldLines) {
                t.isInMouseHighlightingMode = false;
            }
            return;
        }

        //Auto-scroll if mouse outside of editor area and in mouse-highlighting mode
        if (this.isInMouseHighlightingMode()) {
            int mX = MouseInput.getMouseX();
            int mY = MouseInput.getMouseY();
            float speedMult = 0.008F;
            if (mX < this.borderLeft) {
                float f = Math.max(0.01F, (float)(this.borderLeft - mX) * speedMult);
                this.horizontalScrollBar.setScroll(this.horizontalScrollBar.getScroll() - f);
            } else if (mX > (this.getEditorAreaX() + this.getEditorAreaWidth())) {
                float f = Math.max(0.01F, (float)(mX - (this.getEditorAreaX() + this.getEditorAreaWidth())) * speedMult);
                this.horizontalScrollBar.setScroll(this.horizontalScrollBar.getScroll() + f);
            }
            if (mY < this.headerHeight) {
                float f = Math.max(0.01F, (float)(this.headerHeight - mY) * speedMult);
                this.verticalScrollBar.setScroll(this.verticalScrollBar.getScroll() - f);
            } else if (mY > (this.height - this.footerHeight)) {
                float f = Math.max(0.01F, (float)(mY - (this.height - this.footerHeight)) * speedMult);
                this.verticalScrollBar.setScroll(this.verticalScrollBar.getScroll() + f);
            }
        }

        if (!this.isMouseInsideEditorArea()) {
            return;
        }

        TextEditorLine first = this.startHighlightLine;
        TextEditorLine hovered = this.getHoveredLine();
        if ((hovered != null) && !hovered.isFocused() && (first != null)) {

            int firstIndex = this.getLineIndex(first);
            int hoveredIndex = this.getLineIndex(hovered);
            boolean firstIsBeforeHovered = hoveredIndex > firstIndex;
            boolean firstIsAfterHovered = hoveredIndex < firstIndex;

            if (first.isInMouseHighlightingMode) {
                if (firstIsAfterHovered) {
                    this.setFocusedLine(this.getLineIndex(hovered));
                    if (!hovered.isInMouseHighlightingMode) {
                        hovered.isInMouseHighlightingMode = true;
                        hovered.getAsAccessor().setShiftPressedFancyMenu(false);
                        hovered.moveCursorTo(hovered.getValue().length());
                    }
                } else if (firstIsBeforeHovered) {
                    this.setFocusedLine(this.getLineIndex(hovered));
                    if (!hovered.isInMouseHighlightingMode) {
                        hovered.isInMouseHighlightingMode = true;
                        hovered.getAsAccessor().setShiftPressedFancyMenu(false);
                        hovered.moveCursorTo(0);
                    }
                } else if (first == hovered) {
                    this.setFocusedLine(this.getLineIndex(first));
                }
            }

            int startIndex = Math.min(hoveredIndex, firstIndex);
            int endIndex = Math.max(hoveredIndex, firstIndex);
            int index = 0;
            for (TextEditorLine t : this.textFieldLines) {
                //Highlight all lines between the first and current line and remove highlighting from lines outside of highlight range
                if ((t != hovered) && (t != first)) {
                    if ((index > startIndex) && (index < endIndex)) {
                        if (firstIsAfterHovered) {
                            t.setCursorPosition(0);
                            t.setHighlightPos(t.getValue().length());
                        } else if (firstIsBeforeHovered) {
                            t.setCursorPosition(t.getValue().length());
                            t.setHighlightPos(0);
                        }
                    } else {
                        t.getAsAccessor().setShiftPressedFancyMenu(false);
                        t.moveCursorTo(0);
                        t.isInMouseHighlightingMode = false;
                    }
                }
                index++;
            }
            this.startHighlightLineIndex = startIndex;
            this.endHighlightLineIndex = endIndex;

            if (first != hovered) {
                first.getAsAccessor().setShiftPressedFancyMenu(true);
                if (firstIsAfterHovered) {
                    first.moveCursorTo(0);
                } else if (firstIsBeforeHovered) {
                    first.moveCursorTo(first.getValue().length());
                }
                first.getAsAccessor().setShiftPressedFancyMenu(false);
            }

        }

        TextEditorLine focused = this.getFocusedLine();
        if ((focused != null) && focused.isInMouseHighlightingMode) {
            if ((this.startHighlightLineIndex == -1) && (this.endHighlightLineIndex == -1)) {
                this.startHighlightLineIndex = this.getLineIndex(focused);
                this.endHighlightLineIndex = this.startHighlightLineIndex;
            }
            int i = Mth.floor(MouseInput.getMouseX()) - focused.getX();
            if (focused.getAsAccessor().getBorderedFancyMenu()) {
                i -= 4;
            }
            String s = this.font.plainSubstrByWidth(focused.getValue().substring(focused.getAsAccessor().getDisplayPosFancyMenu()), focused.getInnerWidth());
            focused.getAsAccessor().setShiftPressedFancyMenu(true);
            focused.moveCursorTo(this.font.plainSubstrByWidth(s, i).length() + focused.getAsAccessor().getDisplayPosFancyMenu());
            focused.getAsAccessor().setShiftPressedFancyMenu(false);
            if ((focused.getAsAccessor().getHighlightPosFancyMenu() == focused.getCursorPosition()) && (this.startHighlightLineIndex == this.endHighlightLineIndex)) {
                this.resetHighlighting();
            }
        }

    }

    public void updateLines(@Nullable Consumer<TextEditorLine> doAfterEachLineUpdate) {
        try {
            int index = 0;
            for (TextEditorLine line : this.textFieldLines) {
                line.lineIndex = index;
                line.setY(this.headerHeight + (this.lineHeight * index) + this.getLineRenderOffsetY());
                line.setX(this.borderLeft + this.getLineRenderOffsetX());
                line.setWidth(this.currentLineWidth);
                line.setHeight(this.lineHeight);
                line.getAsAccessor().setDisplayPosFancyMenu(0);
                if (doAfterEachLineUpdate != null) {
                    doAfterEachLineUpdate.accept(line);
                }
                index++;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void updateCurrentLineWidth() {
        //Find width of the longest line and update current line width
        int longestTextWidth = 0;
        for (TextEditorLine f : this.textFieldLines) {
            if (f.textWidth > longestTextWidth) {
                //Calculating the text size for every line every tick kills the CPU, so I'm calculating the size on value change in the text box
                longestTextWidth = f.textWidth;
            }
        }
        this.currentLineWidth = longestTextWidth + 30;
    }

    public int getLineRenderOffsetX() {
        return -(int)(((float)this.getTotalScrollWidth() / 100.0F) * (this.horizontalScrollBar.getScroll() * 100.0F));
    }

    public int getLineRenderOffsetY() {
        return -(int)(((float)this.getTotalScrollHeight() / 100.0F) * (this.verticalScrollBar.getScroll() * 100.0F));
    }

    public int getTotalLineHeight() {
        return this.lineHeight * this.textFieldLines.size();
    }

    @Nullable
    public TextEditorLine addLineAtIndex(int index) {
        if (!this.multilineMode && (this.getLineCount() > 0)) {
            this.multilineNotSupportedNotificationDisplayStart = System.currentTimeMillis();
            return null;
        }
        TextEditorLine f = new TextEditorLine(Minecraft.getInstance().font, 0, 0, 50, this.lineHeight, false, this.characterFilter, this);
        f.setMaxLength(Integer.MAX_VALUE);
        f.lineIndex = index;
        if (index > 0) {
            TextEditorLine before = this.getLine(index-1);
            if (before != null) {
                f.setY(before.getY() + this.lineHeight);
            }
        }
        this.textFieldLines.add(index, f);
        return f;
    }

    @Nullable
    public TextEditorLine addLine() {
        return this.addLineAtIndex(this.getLineCount());
    }

    public void removeLineAtIndex(int index) {
        if (index < 1) {
            return;
        }
        if (index <= this.getLineCount()-1) {
            this.textFieldLines.remove(index);
        }
    }

    public void removeLastLine() {
        this.removeLineAtIndex(this.getLineCount()-1);
    }

    public int getLineCount() {
        return this.textFieldLines.size();
    }

    @Nullable
    public TextEditorLine getLine(int index) {
        return this.textFieldLines.get(index);
    }

    public void setFocusedLine(int index) {
        if (index <= this.getLineCount()-1) {
            for (TextEditorLine f : this.textFieldLines) {
                f.setFocused(false);
            }
            this.getLine(index).setFocused(true);
        }
    }

    /**
     * Returns the index of the focused line or -1 if no line is focused.
     **/
    public int getFocusedLineIndex() {
        int index = 0;
        for (TextEditorLine f : this.textFieldLines) {
            if (f.isFocused()) {
                return index;
            }
            index++;
        }
        return -1;
    }

    @Nullable
    public TextEditorLine getFocusedLine() {
        int index = this.getFocusedLineIndex();
        if (index != -1) {
            return this.getLine(index);
        }
        return null;
    }

    public boolean isLineFocused() {
        return (this.getFocusedLineIndex() > -1);
    }

    @Nullable
    public TextEditorLine getLineAfter(TextEditorLine line) {
        int index = this.getLineIndex(line);
        if ((index > -1) && (index < (this.getLineCount()-1))) {
            return this.getLine(index+1);
        }
        return null;
    }

    @Nullable
    public TextEditorLine getLineBefore(TextEditorLine line) {
        int index = this.getLineIndex(line);
        if (index > 0) {
            return this.getLine(index-1);
        }
        return null;
    }

    public boolean isAtLeastOneLineInHighlightMode() {
        for (TextEditorLine t : this.textFieldLines) {
            if (t.isInMouseHighlightingMode) {
                return true;
            }
        }
        return false;
    }

    @Nullable
    /** Returns the lines between two indexes, EXCLUDING start AND end indexes! **/
    public List<TextEditorLine> getLinesBetweenIndexes(int startIndex, int endIndex) {
        startIndex = Math.min(Math.max(startIndex, 0), this.textFieldLines.size()-1);
        endIndex = Math.min(Math.max(endIndex, 0), this.textFieldLines.size()-1);
        List<TextEditorLine> l = new ArrayList<>();
        l.addAll(this.textFieldLines.subList(startIndex, endIndex));
        if (!l.isEmpty()) {
            l.remove(0);
        }
        return l;
    }

    @Nullable
    public TextEditorLine getHoveredLine() {
        for (TextEditorLine t : this.textFieldLines) {
            if (t.isHovered()) {
                return t;
            }
        }
        return null;
    }

    public int getLineIndex(TextEditorLine inputBox) {
        return this.textFieldLines.indexOf(inputBox);
    }

    public void goUpLine() {
        if (this.isLineFocused()) {
            int current = Math.max(0, this.getFocusedLineIndex());
            if (current > 0) {
                TextEditorLine currentLine = this.getLine(current);
                this.setFocusedLine(current - 1);
                if (currentLine != null) {
                    this.getFocusedLine().moveCursorTo(this.lastCursorPosSetByUser);
                }
            }
        }
    }

    public void goDownLine(boolean isNewLine) {
        if (this.isLineFocused()) {
            int current = Math.max(0, this.getFocusedLineIndex());
            if (isNewLine) {
                this.addLineAtIndex(current+1);
            }
            TextEditorLine currentLine = this.getLine(current);
            this.setFocusedLine(current+1);
            if (currentLine != null) {
                TextEditorLine nextLine = this.getFocusedLine();
                if (isNewLine) {
                    //Split content of currentLine at cursor pos and move text after cursor to next line if ENTER was pressed
                    String textBeforeCursor = currentLine.getValue().substring(0, currentLine.getCursorPosition());
                    String textAfterCursor = currentLine.getValue().substring(currentLine.getCursorPosition());
                    currentLine.setValue(textBeforeCursor);
                    nextLine.setValue(textAfterCursor);
                    nextLine.moveCursorTo(0);
                    //Add amount of spaces of the beginning of the old line to the beginning of the new line
                    if (textBeforeCursor.startsWith(" ")) {
                        int spaces = 0;
                        for (char c : textBeforeCursor.toCharArray()) {
                            if (String.valueOf(c).equals(" ")) {
                                spaces++;
                            } else {
                                break;
                            }
                        }
                        nextLine.setValue(textBeforeCursor.substring(0, spaces) + nextLine.getValue());
                        nextLine.moveCursorTo(spaces);
                    }
                } else {
                    nextLine.moveCursorTo(this.lastCursorPosSetByUser);
                }
            }
        }
    }

    public List<TextEditorLine> getCopyOfLines() {
        List<TextEditorLine> l = new ArrayList<>();
        for (TextEditorLine t : this.textFieldLines) {
            TextEditorLine n = new TextEditorLine(this.font, 0, 0, 0, 0, false, this.characterFilter, this);
            n.setValue(t.getValue());
            n.setFocused(t.isFocused());
            n.moveCursorTo(t.getCursorPosition());
            l.add(n);
        }
        return l;
    }

    public boolean isTextHighlighted() {
        return (this.startHighlightLineIndex != -1) && (this.endHighlightLineIndex != -1);
    }

    public boolean isHighlightedTextHovered() {
        if (this.isTextHighlighted()) {
            List<TextEditorLine> highlightedLines = new ArrayList<>();
            if (this.endHighlightLineIndex <= this.getLineCount()-1) {
                highlightedLines.addAll(this.textFieldLines.subList(this.startHighlightLineIndex, this.endHighlightLineIndex+1));
            }
            for (TextEditorLine t : highlightedLines) {
                if (t.isHighlightedHovered()) {
                    return true;
                }
            }
        }
        return false;
    }

    @NotNull
    public String getHighlightedText() {
        try {
            if ((this.startHighlightLineIndex != -1) && (this.endHighlightLineIndex != -1)) {
                List<TextEditorLine> lines = new ArrayList<>();
                lines.add(this.getLine(this.startHighlightLineIndex));
                if (this.startHighlightLineIndex != this.endHighlightLineIndex) {
                    lines.addAll(this.getLinesBetweenIndexes(this.startHighlightLineIndex, this.endHighlightLineIndex));
                    lines.add(this.getLine(this.endHighlightLineIndex));
                }
                StringBuilder s = new StringBuilder();
                boolean b = false;
                for (TextEditorLine t : lines) {
                    if (b) {
                        s.append("\n");
                    }
                    s.append(t.getHighlighted());
                    b = true;
                }
                String ret = s.toString();
                return ret;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    @NotNull
    public String cutHighlightedText() {
        String highlighted = this.getHighlightedText();
        this.deleteHighlightedText();
        return highlighted;
    }

    public void deleteHighlightedText() {
        int linesRemoved = 0;
        try {
            if ((this.startHighlightLineIndex != -1) && (this.endHighlightLineIndex != -1)) {
                if (this.startHighlightLineIndex == this.endHighlightLineIndex) {
                    this.getLine(this.startHighlightLineIndex).insertText("");
                } else {
                    TextEditorLine start = this.getLine(this.startHighlightLineIndex);
                    start.insertText("");
                    TextEditorLine end = this.getLine(this.endHighlightLineIndex);
                    end.insertText("");
                    if ((this.endHighlightLineIndex - this.startHighlightLineIndex) > 1) {
                        for (TextEditorLine line : this.getLinesBetweenIndexes(this.startHighlightLineIndex, this.endHighlightLineIndex)) {
                            this.removeLineAtIndex(this.getLineIndex(line));
                            linesRemoved++;
                        }
                    }
                    String oldStartValue = start.getValue();
                    start.setCursorPosition(start.getValue().length());
                    start.setHighlightPos(start.getCursorPosition());
                    start.insertText(end.getValue());
                    start.setCursorPosition(oldStartValue.length());
                    start.setHighlightPos(start.getCursorPosition());
                    this.removeLineAtIndex(this.getLineIndex(end));
                    linesRemoved++;
                    this.setFocusedLine(this.startHighlightLineIndex);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        this.correctYScroll(-linesRemoved);
        this.resetHighlighting();
    }

    public void resetHighlighting() {
        this.startHighlightLineIndex = -1;
        this.endHighlightLineIndex = -1;
        for (TextEditorLine t : this.textFieldLines) {
            t.setHighlightPos(t.getCursorPosition());
        }
    }

    public boolean isInMouseHighlightingMode() {
        return MouseInput.isLeftMouseDown() && (this.startHighlightLine != null);
    }

    public void pasteText(String text) {
        try {
            if ((text != null) && !text.equals("")) {
                int addedLinesCount = 0;
                if (this.isTextHighlighted()) {
                    this.deleteHighlightedText();
                }
                if (!this.isLineFocused()) {
                    this.setFocusedLine(this.getLineCount()-1);
                    this.getFocusedLine().moveCursorToEnd();
                }
                TextEditorLine focusedLine = this.getFocusedLine();
                //These two strings are for correctly pasting text within a char sequence (if the cursor is not at the end or beginning of the line)
                String textBeforeCursor = "";
                String textAfterCursor = "";
                if (focusedLine.getValue().length() > 0) {
                    textBeforeCursor = focusedLine.getValue().substring(0, focusedLine.getCursorPosition());
                    if (focusedLine.getCursorPosition() < focusedLine.getValue().length()) {
                        textAfterCursor = this.getFocusedLine().getValue().substring(focusedLine.getCursorPosition(), focusedLine.getValue().length());
                    }
                }
                focusedLine.setValue(textBeforeCursor);
                focusedLine.setCursorPosition(textBeforeCursor.length());
                String[] lines = new String[]{text};
                if (text.contains("\n")) {
                    lines = text.split("\n", -1);
                }
                if (!this.multilineMode && (lines.length > 1)) {
                    lines = new String[]{lines[0]};
                    this.multilineNotSupportedNotificationDisplayStart = System.currentTimeMillis();
                }
                Array.set(lines, lines.length-1, lines[lines.length-1] + textAfterCursor);
                if (lines.length == 1) {
                    this.getFocusedLine().insertText(lines[0]);
                } else if (lines.length > 1) {
                    int index = -1;
                    for (String s : lines) {
                        if (index == -1) {
                            index = this.getFocusedLineIndex();
                        } else {
                            this.addLineAtIndex(index);
                            addedLinesCount++;
                        }
                        this.getLine(index).insertText(s);
                        index++;
                    }
                    this.setFocusedLine(index - 1);
                    this.getFocusedLine().setCursorPosition(Math.max(0, this.getFocusedLine().getValue().length() - textAfterCursor.length()));
                    this.getFocusedLine().setHighlightPos(this.getFocusedLine().getCursorPosition());
                }
                this.correctYScroll(addedLinesCount);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        this.resetHighlighting();
    }

    public void setText(String text) {
        TextEditorLine t = this.getLine(0);
        this.textFieldLines.clear();
        this.textFieldLines.add(t);
        this.setFocusedLine(0);
        t.setValue("");
        t.moveCursorTo(0);
        this.pasteText(text);
        this.setFocusedLine(0);
        t.moveCursorTo(0);
        this.verticalScrollBar.setScroll(0.0F);
    }

    public String getText() {
        StringBuilder s = new StringBuilder();
        boolean b = false;
        for (TextEditorLine t : this.textFieldLines) {
            if (b) {
                s.append("\n");
            }
            s.append(t.getValue());
            b = true;
        }
        return s.toString();
    }

    /**
     * @return The text BEFORE the cursor or NULL if no line is focused.
     */
    @Nullable
    public String getTextBeforeCursor() {
        if (!this.isLineFocused()) {
            return null;
        }
        int focusedLineIndex = this.getFocusedLineIndex();
        List<TextEditorLine> lines = new ArrayList<>();
        if (focusedLineIndex == 0) {
            lines.add(this.getLine(0));
        } else if (focusedLineIndex > 0) {
            lines.addAll(this.textFieldLines.subList(0, focusedLineIndex+1));
        }
        TextEditorLine lastLine = lines.get(lines.size()-1);
        StringBuilder s = new StringBuilder();
        boolean b = false;
        for (TextEditorLine t : lines) {
            if (b) {
                s.append("\n");
            }
            if (t != lastLine) {
                s.append(t.getValue());
            } else {
                s.append(t.getValue().substring(0, t.getCursorPosition()));
            }
            b = true;
        }
        return s.toString();
    }

    /**
     * @return The text AFTER the cursor or NULL if no line is focused.
     */
    @Nullable
    public String getTextAfterCursor() {
        if (!this.isLineFocused()) {
            return null;
        }
        int focusedLineIndex = this.getFocusedLineIndex();
        List<TextEditorLine> lines = new ArrayList<>();
        if (focusedLineIndex == this.getLineCount()-1) {
            lines.add(this.getLine(this.getLineCount()-1));
        } else if (focusedLineIndex < this.getLineCount()-1) {
            lines.addAll(this.textFieldLines.subList(focusedLineIndex, this.getLineCount()));
        }
        TextEditorLine firstLine = lines.get(0);
        StringBuilder s = new StringBuilder();
        boolean b = false;
        for (TextEditorLine t : lines) {
            if (b) {
                s.append("\n");
            }
            if (t != firstLine) {
                s.append(t.getValue());
            } else {
                s.append(t.getValue().substring(t.getCursorPosition(), t.getValue().length()));
            }
            b = true;
        }
        return s.toString();
    }

    @Override
    public boolean charTyped(char character, int modifiers) {

        for (TextEditorLine l : this.textFieldLines) {
            l.charTyped(character, modifiers);
        }

        return super.charTyped(character, modifiers);

    }


    @Override
    public boolean keyPressed(int keycode, int i1, int i2) {

        for (TextEditorLine l : this.textFieldLines) {
            l.keyPressed(keycode, i1, i2);
        }

        //ENTER
        if (keycode == 257) {
            if (!this.isInMouseHighlightingMode() && this.multilineMode) {
                if (this.isLineFocused()) {
                    this.resetHighlighting();
                    this.goDownLine(true);
                    this.correctYScroll(1);
                }
            }
            if (!this.multilineMode) {
                this.multilineNotSupportedNotificationDisplayStart = System.currentTimeMillis();
            }
            return true;
        }
        //ARROW UP
        if (keycode == InputConstants.KEY_UP) {
            if (!this.isInMouseHighlightingMode()) {
                this.resetHighlighting();
                this.goUpLine();
                this.correctYScroll(0);
            }
            return true;
        }
        //ARROW DOWN
        if (keycode == InputConstants.KEY_DOWN) {
            if (!this.isInMouseHighlightingMode()) {
                this.resetHighlighting();
                this.goDownLine(false);
                this.correctYScroll(0);
            }
            return true;
        }

        //BACKSPACE
        if (keycode == InputConstants.KEY_BACKSPACE) {
            if (!this.isInMouseHighlightingMode()) {
                if (this.isTextHighlighted()) {
                    this.deleteHighlightedText();
                } else {
                    if (this.isLineFocused()) {
                        TextEditorLine focused = this.getFocusedLine();
                        focused.getAsAccessor().setShiftPressedFancyMenu(false);
                        focused.getAsAccessor().invokeDeleteTextFancyMenu(-1);
                        focused.getAsAccessor().setShiftPressedFancyMenu(Screen.hasShiftDown());
                    }
                }
                this.resetHighlighting();
            }
            return true;
        }
        //CTRL + C
        if (Screen.isCopy(keycode)) {
            Minecraft.getInstance().keyboardHandler.setClipboard(this.getHighlightedText());
            return true;
        }
        //CTRL + V
        if (Screen.isPaste(keycode)) {
            this.pasteText(Minecraft.getInstance().keyboardHandler.getClipboard());
            return true;
        }
        //CTRL + A
        if (Screen.isSelectAll(keycode)) {
            for (TextEditorLine t : this.textFieldLines) {
                t.setHighlightPos(0);
                t.setCursorPosition(t.getValue().length());
            }
            this.setFocusedLine(this.getLineCount()-1);
            this.startHighlightLineIndex = 0;
            this.endHighlightLineIndex = this.getLineCount()-1;
            return true;
        }
        //CTRL + U
        if (Screen.isCut(keycode)) {
            Minecraft.getInstance().keyboardHandler.setClipboard(this.cutHighlightedText());
            this.resetHighlighting();
            return true;
        }
        //Reset highlighting when pressing left/right arrow keys
        if ((keycode == InputConstants.KEY_RIGHT) || (keycode == InputConstants.KEY_LEFT)) {
            this.resetHighlighting();
            return true;
        }

        return super.keyPressed(keycode, i1, i2);

    }

    @Override
    public boolean keyReleased(int i1, int i2, int i3) {

        for (TextEditorLine l : this.textFieldLines) {
            l.keyReleased(i1, i2, i3);
        }

        return super.keyReleased(i1, i2, i3);

    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {

        if (!this.isMouseInteractingWithGrabbers()) {

            for (TextEditorLine l : this.textFieldLines) {
                l.mouseClicked(mouseX, mouseY, button);
            }

            if (this.isMouseInsideEditorArea()) {
                if (button == 1) {
                    this.rightClickContextMenu.closeMenu();
                }
                if ((button == 0) || (button == 1)) {
                    boolean isHighlightedHovered = this.isHighlightedTextHovered();
                    TextEditorLine hoveredLine = this.getHoveredLine();
                    if (!this.rightClickContextMenu.isOpen()) {
                        if ((button == 0) || !isHighlightedHovered) {
                            this.resetHighlighting();
                        }
                        if (hoveredLine == null) {
                            TextEditorLine focus = this.getLine(this.getLineCount()-1);
                            for (TextEditorLine t : this.textFieldLines) {
                                if ((MouseInput.getMouseY() >= t.y) && (MouseInput.getMouseY() <= t.y + t.getHeight())) {
                                    focus = t;
                                    break;
                                }
                            }
                            this.setFocusedLine(this.getLineIndex(focus));
                            this.getFocusedLine().moveCursorToEnd();
                            this.correctYScroll(0);
                        } else if ((button == 1) && !isHighlightedHovered) {
                            //Focus line in case it is right-clicked
                            this.setFocusedLine(this.getLineIndex(hoveredLine));
                            //Set cursor in case line is right-clicked
                            String s = this.font.plainSubstrByWidth(hoveredLine.getValue().substring(hoveredLine.getAsAccessor().getDisplayPosFancyMenu()), hoveredLine.getInnerWidth());
                            hoveredLine.moveCursorTo(this.font.plainSubstrByWidth(s, MouseInput.getMouseX() - hoveredLine.getX()).length() + hoveredLine.getAsAccessor().getDisplayPosFancyMenu());
                        }
                    }
                    if (button == 1) {
                        this.updateRightClickContextMenu();
                        UIBase.openScaledContextMenuAtMouse(this.rightClickContextMenu);
                    } else if (this.rightClickContextMenu.isOpen() && !this.rightClickContextMenu.isHoveredOrFocused()) {
                        this.rightClickContextMenu.closeMenu();
                        //Call mouseClicked of lines after closing the menu, so the focused line and cursor pos gets updated
                        this.textFieldLines.forEach((line) -> {
                            line.mouseClicked(mouseX, mouseY, button);
                        });
                        //Call mouseClicked of editor again to do everything that would happen when clicked without the context menu opened
                        this.mouseClicked(mouseX, mouseY, button);
                    }
                }
            }

        }

        return super.mouseClicked(mouseX, mouseY, button);

    }

    @Override
    public void tick() {

        for (TextEditorLine l : this.textFieldLines) {
            l.tick();
        }

        super.tick();

    }

    @Override
    public void onClose() {
        if (this.parentScreen != null) {
            Minecraft.getInstance().setScreen(this.parentScreen);
        } else {
            super.onClose();
        }
        if (this.callback != null) {
            this.callback.accept(null);
        }
    }

    public boolean isMouseInteractingWithGrabbers() {
        return this.verticalScrollBar.isGrabberGrabbed() || this.verticalScrollBar.isGrabberHovered() || this.horizontalScrollBar.isGrabberGrabbed() || this.horizontalScrollBar.isGrabberHovered();
    }

    public boolean isMouseInteractingWithPlaceholderGrabbers() {
        return this.verticalScrollBarPlaceholderMenu.isGrabberGrabbed() || this.verticalScrollBarPlaceholderMenu.isGrabberHovered() || this.horizontalScrollBarPlaceholderMenu.isGrabberGrabbed() || this.horizontalScrollBarPlaceholderMenu.isGrabberHovered();
    }

    public int getEditBoxCursorX(EditBox editBox) {
        try {
            IMixinEditBox b = (IMixinEditBox) editBox;
            String s = this.font.plainSubstrByWidth(editBox.getValue().substring(b.getDisplayPosFancyMenu()), editBox.getInnerWidth());
            int j = editBox.getCursorPosition() - b.getDisplayPosFancyMenu();
            boolean flag = j >= 0 && j <= s.length();
            boolean flag2 = editBox.getCursorPosition() < editBox.getValue().length() || editBox.getValue().length() >= b.getMaxLengthFancyMenu();
            int l = b.getBorderedFancyMenu() ? editBox.getX() + 4 : editBox.getX();
            int j1 = l;
            if (!s.isEmpty()) {
                String s1 = flag ? s.substring(0, j) : s;
                j1 += this.font.width(b.getFormatterFancyMenu().apply(s1, b.getDisplayPosFancyMenu()));
            }
            int k1 = j1;
            if (!flag) {
                k1 = j > 0 ? l + editBox.getWidth() : l;
            } else if (flag2) {
                k1 = j1 - 1;
                --j1;
            }
            return k1;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    public void scrollToLine(int lineIndex, boolean bottom) {
        if (bottom) {
            this.scrollToLine(lineIndex, -Math.max(0, this.getEditorAreaHeight() - this.lineHeight));
        } else {
            this.scrollToLine(lineIndex, 0);
        }
    }

    public void scrollToLine(int lineIndex, int offset) {
        int totalLineHeight = this.getTotalScrollHeight();
        float f = (float)Math.max(0, ((lineIndex + 1) * this.lineHeight) - this.lineHeight) / (float)totalLineHeight;
        if (offset != 0) {
            if (offset > 0) {
                f += ((float)offset / (float)totalLineHeight);
            } else {
                f -= ((float)Math.abs(offset) / (float)totalLineHeight);
            }
        }
        this.verticalScrollBar.setScroll(f);
    }

    public int getTotalScrollHeight() {
        if (this.overriddenTotalScrollHeight != -1) {
            return this.overriddenTotalScrollHeight;
        }
        return this.getTotalLineHeight();
    }

    public int getTotalScrollWidth() {
        //return Math.max(0, this.currentLineWidth - this.getEditorAreaWidth())
        return this.currentLineWidth;
    }

    public void correctYScroll(int lineCountOffsetAfterRemovingAdding) {

        //Don't fix scroll if in mouse-highlighting mode or no line is focused
        if (this.isInMouseHighlightingMode() || !this.isLineFocused()) {
            return;
        }

        int minY = this.getEditorAreaY();
        int maxY = this.getEditorAreaY() + this.getEditorAreaHeight();
        int currentLineY = this.getFocusedLine().getY();

        if (currentLineY < minY) {
            this.scrollToLine(this.getFocusedLineIndex(), false);
        } else if ((currentLineY + this.lineHeight) > maxY) {
            this.scrollToLine(this.getFocusedLineIndex(), true);
        } else if (lineCountOffsetAfterRemovingAdding != 0) {
            this.overriddenTotalScrollHeight = -1;
            int removedAddedLineCount = Math.abs(lineCountOffsetAfterRemovingAdding);
            if (lineCountOffsetAfterRemovingAdding > 0) {
                this.overriddenTotalScrollHeight = this.getTotalScrollHeight() - (this.lineHeight * removedAddedLineCount);
            } else if (lineCountOffsetAfterRemovingAdding < 0) {
                this.overriddenTotalScrollHeight = this.getTotalScrollHeight() + (this.lineHeight * removedAddedLineCount);
            }
            this.updateLines(null);
            this.overriddenTotalScrollHeight = -1;
            int diffToTop = Math.max(0, this.getFocusedLine().getY() - this.getEditorAreaY());
            this.scrollToLine(this.getFocusedLineIndex(), -diffToTop);
            this.correctYScroll(0);
        }

        if (this.getTotalLineHeight() <= this.getEditorAreaHeight()) {
            this.verticalScrollBar.setScroll(0.0F);
        }

    }

    public void correctXScroll(TextEditorLine line) {

        //Don't fix scroll if in mouse-highlighting mode
        if (this.isInMouseHighlightingMode()) {
            return;
        }

        if (this.isLineFocused() && (this.getFocusedLine() == line)) {

            int oldX = line.x;

            this.updateCurrentLineWidth();
            this.updateLines(null);

            int newX = line.x;
            String oldValue = line.lastTickValue;
            String newValue = line.getValue();

            //Make the lines scroll horizontally with the cursor position if the cursor is too far to the left or right
            int cursorWidth = 2;
            if (line.getCursorPosition() >= newValue.length()) {
                cursorWidth = 6;
            }
            int editorAreaCenterX = this.getEditorAreaX() + (this.getEditorAreaWidth() / 2);
            int cursorX = this.getEditBoxCursorX(line);
            if (cursorX > editorAreaCenterX) {
                cursorX += cursorWidth + 5;
            } else if (cursorX < editorAreaCenterX) {
                cursorX -= cursorWidth + 5;
            }
            int maxToRight = this.getEditorAreaX() + this.getEditorAreaWidth();
            int maxToLeft = this.getEditorAreaX();
            float currentScrollX = this.horizontalScrollBar.getScroll();
            int currentLineW = this.getTotalScrollWidth();
            boolean textGotDeleted = oldValue.length() > newValue.length();
            boolean textGotAdded = oldValue.length() < newValue.length();
            if (cursorX > maxToRight) {
                float f = (float)(cursorX - maxToRight) / (float)currentLineW;
                this.horizontalScrollBar.setScroll(currentScrollX + f);
            } else if (cursorX < maxToLeft) {
                //By default, move back the line just a little when moving the cursor to the left side by using the mouse or arrow keys
                float f = (float)(maxToLeft - cursorX) / (float)currentLineW;
                //But move it back a big chunk when deleting chars (by pressing backspace)
                if (textGotDeleted) {
                    f = (float)(maxToRight - maxToLeft) / (float)currentLineW;
                }
                this.horizontalScrollBar.setScroll(currentScrollX - f);
            } else if (textGotDeleted && (oldX < newX)) {
                float f = (float)(newX - oldX) / (float)currentLineW;
                this.horizontalScrollBar.setScroll(currentScrollX + f);
            } else if (textGotAdded && (oldX > newX)) {
                float f = (float)(oldX - newX) / (float)currentLineW;
                this.horizontalScrollBar.setScroll(currentScrollX - f);
            }
            if (line.getCursorPosition() == 0) {
                this.horizontalScrollBar.setScroll(0.0F);
            }

        }

    }

    public boolean isMouseInsideEditorArea() {
        int xStart = this.borderLeft;
        int yStart = this.headerHeight;
        int xEnd = this.getEditorAreaX() + this.getEditorAreaWidth();
        int yEnd = this.height - this.footerHeight;
        int mX = MouseInput.getMouseX();
        int mY = MouseInput.getMouseY();
        return (mX >= xStart) && (mX <= xEnd) && (mY >= yStart) && (mY <= yEnd);
    }

    public int getEditorAreaWidth() {
        int i = (this.width - this.borderRight) - this.borderLeft;
        if (showPlaceholderMenu) {
            i = i - this.placeholderMenuWidth - 15;
        }
        return i;
    }

    public int getEditorAreaHeight() {
        return (this.height - this.footerHeight) - this.headerHeight;
    }

    public int getEditorAreaX() {
        return this.borderLeft;
    }

    public int getEditorAreaY() {
        return this.headerHeight;
    }

    public static class PlaceholderMenuEntry extends UIBase {

        public TextEditorScreen parent;
        public final Component label;
        public Runnable clickAction;
        public int x;
        public int y;
        public final int labelWidth;
        public Color backgroundColorIdle = Color.GRAY;
        public Color backgroundColorHover = Color.LIGHT_GRAY;
        public Color dotColor = Color.BLUE;
        public Color entryLabelColor = Color.WHITE;
        public AdvancedButton buttonBase;
        public Font font = Minecraft.getInstance().font;

        public PlaceholderMenuEntry(@NotNull TextEditorScreen parent, @NotNull Component label, @NotNull Runnable clickAction) {
            this.parent = parent;
            this.label = label;
            this.clickAction = clickAction;
            this.labelWidth = this.font.width(this.label);
            this.buttonBase = new AdvancedButton(0, 0, this.getWidth(), this.getHeight(), "", true, (button) -> {
                this.clickAction.run();
            }) {
                @Override
                public boolean isHoveredOrFocused() {
                    if (PlaceholderMenuEntry.this.parent.isMouseInteractingWithPlaceholderGrabbers()) {
                        return false;
                    }
                    return super.isHoveredOrFocused();
                }
                @Override
                public void onClick(double p_93371_, double p_93372_) {
                    if (PlaceholderMenuEntry.this.parent.isMouseInteractingWithPlaceholderGrabbers()) {
                        return;
                    }
                    super.onClick(p_93371_, p_93372_);
                }
                @Override
                public void render(GuiGraphics p_93657_, int p_93658_, int p_93659_, float p_93660_) {
                    if (PlaceholderMenuEntry.this.parent.isMouseInteractingWithPlaceholderGrabbers()) {
                        this.isHovered = false;
                    }
                    super.render(p_93657_, p_93658_, p_93659_, p_93660_);
                }
            };
        }

        public void render(GuiGraphics graphics, int mouseX, int mouseY, float partial) {
            //Update the button colors
            this.buttonBase.setBackgroundColor(this.backgroundColorIdle, this.backgroundColorHover, this.backgroundColorIdle, this.backgroundColorHover, 1);
            //Update the button pos
            this.buttonBase.x = this.x;
            this.buttonBase.y = this.y;
            int yCenter = this.y + (this.getHeight() / 2);
            //Render the button
            this.buttonBase.render(graphics, mouseX, mouseY, partial);
            //Render dot
            renderListingDot(graphics, this.x + 5, yCenter - 2, this.dotColor);
            //Render label
            graphics.drawString(this.font, this.label, this.x + 5 + 4 + 3, yCenter - (this.font.lineHeight / 2), this.entryLabelColor.getRGB(), false);
        }

        public int getWidth() {
            return Math.max(this.parent.placeholderMenuWidth, 5 + 4 + 3 + this.labelWidth + 5);
        }

        public int getHeight() {
            return this.parent.placeholderMenuEntryHeight;
        }

        public boolean isHovered() {
            return this.buttonBase.isHoveredOrFocused();
        }

        public void setDescription(String... desc) {
            this.buttonBase.setDescription(desc);
        }

    }

}