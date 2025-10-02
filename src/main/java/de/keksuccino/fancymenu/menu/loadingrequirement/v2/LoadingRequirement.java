
package de.keksuccino.fancymenu.menu.loadingrequirement.v2;

import de.keksuccino.fancymenu.menu.fancy.helper.ui.texteditor.TextEditorFormattingRule;
import de.keksuccino.konkrete.input.CharacterFilter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.charset.UnsupportedCharsetException;
import java.util.List;
import java.util.Objects;

/**
 * A LoadingRequirement.<br><br>
 *
 * Needs to be registered to the {@link LoadingRequirementRegistry} on mod init.
 */
public abstract class LoadingRequirement {

    protected final String identifier;

    /**
     * The identifier needs to be unique! It is not possible to register two requirements with the same identifier.
     */
    public LoadingRequirement(@NotNull String uniqueRequirementIdentifier) {
        if (!CharacterFilter.getBasicFilenameCharacterFilter().isAllowed(uniqueRequirementIdentifier)) {
            throw new UnsupportedCharsetException("[FANCYMENU] Illegal characters in LoadingRequirement name: " + uniqueRequirementIdentifier);
        }
        this.identifier = Objects.requireNonNull(uniqueRequirementIdentifier);
    }

    /**
     * If the requirement has a value.<br><br>
     *
     * Keep in mind that the value string will be checked for placeholders, so don't mess with similar JSON-like parsing in it.
     */
    public abstract boolean hasValue();

    /**
     * The magic happens here. This is where you put the actual logic of the requirement.<br>
     * It checks if the requirement is met.<br><br>
     *
     * <b>For example:</b> If your requirement checks if the window is fullscreen, and it IS currently in fullscreen, then return TRUE here.<br><br>
     *
     * Keep in mind that placeholders get replaced in the value string, so don't mess with similar JSON-like parsing in it.<br>
     * Placeholders got replaced already at this point, so you get the final value string and don't need to care about raw placeholders here.
     *
     * @param value The value of the requirement, if it has one. Placeholders got replaced already. This is NULL if the requirement has no value!
     */
    public abstract boolean isRequirementMet(@Nullable String value);

    /**
     * The display name of the requirement.<br>
     * It is shown in the requirement options of the layout editor.
     */
    @NotNull
    public abstract String getDisplayName();

    /**
     * The description of the requirement.<br>
     * It is shown in the requirement options of the layout editor.<br><br>
     *
     * Every entry in the returned list counts as a text line.
     */
    @Nullable
    public abstract List<String> getDescription();

    /**
     * The name of the category this requirement should be in.<br>
     * Requirements don't need to be in a category so if you don't want that, return NULL here.
     */
    @Nullable
    public abstract String getCategory();

    /**
     * The display name of the VALUE of the requirement, if it has one.<br>
     * It is shown in the requirement options of the layout editor.<br><br>
     *
     * Return NULL here if the requirement has no value.
     */
    @Nullable
    public abstract String getValueDisplayName();

    /**
     * The preset/example of the value, if it has one.<br>
     * It is shown in the value input field of the requirement options of the layout editor.<br><br>
     *
     * Keep in mind that the value string will be checked for placeholders, so don't mess with similar JSON-like parsing here.<br><br>
     *
     * Return NULL here if the requirement has no value.
     */
    @Nullable
    public abstract String getValuePreset();

    /**
     * This returns a list with NEW instances of formatting rules used to format the value string in the {@link de.keksuccino.fancymenu.menu.fancy.helper.ui.texteditor.TextEditorScreen}.<br><br>
     *
     * Formatting rules are not mandatory, so if you don't want to use them, return NULL here.<br>
     * Same applies for when the requirement has no value.
     *
     * @return A list with formatting rules used for editing the requirement value in the {@link de.keksuccino.fancymenu.menu.fancy.helper.ui.texteditor.TextEditorScreen}.
     */
    @Nullable
    public abstract List<TextEditorFormattingRule> getValueFormattingRules();

    /**
     * The identifier of the requirement.
     */
    @NotNull
    public String getIdentifier() {
        return this.identifier;
    }

}
