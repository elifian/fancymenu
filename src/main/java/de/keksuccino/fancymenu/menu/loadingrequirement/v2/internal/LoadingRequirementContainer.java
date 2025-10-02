
package de.keksuccino.fancymenu.menu.loadingrequirement.v2.internal;

import de.keksuccino.konkrete.properties.PropertiesSection;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

//TODO Neues Loading Requirement UI:
// - Loading Requirement entry in element context menu
//   - Öffnet Manage Requirement Screen
//     - Links von Screen: ScrollArea Liste, die oben Gruppen und unten einzelne Requirements beinhaltet
//       - Liste passt sich an Breite von linker Screen Hälfte an
//       - Requirements haben andere Entry Farbe als Groups
//     - Rechts von Screen: Controls für List Entries
//       - Add Requirement (Öffnet Add Requirement screen)
//         - Links liste mit allen requirements
//         - Rechts oben box mit beschreibung von requirement, wenn eins ausgewählt
//         - Rechts unten controls für Set Value, Done, etc.
//       - Add Group (Öffnet Add Group screen, in dem Identifier, Mode und children verwaltet werden)
//       - Edit (Öffnet je nach focused entry Add Group oder Add Requirement Screen; beide screens sind ausgefüllt mit Daten von Entry)
//       - Remove (löscht je nach Entry Typ die ausgewählte Gruppe oder das Requirement)
//       - Done (schließt menü)

public class LoadingRequirementContainer {

    private static final Logger LOGGER = LogManager.getLogger();

    protected final List<LoadingRequirementGroup> groups = new ArrayList<>();
    protected final List<LoadingRequirementInstance> instances = new ArrayList<>();
    public boolean forceRequirementsMet = false;
    public boolean forceRequirementsNotMet = false;

    public boolean requirementsMet() {
        if (this.forceRequirementsMet) {
            return true;
        }
        if (this.forceRequirementsNotMet) {
            return false;
        }
        try {
            for (LoadingRequirementGroup g : this.groups) {
                if (!g.requirementsMet()) {
                    return false;
                }
            }
            for (LoadingRequirementInstance i : this.instances) {
                if (!i.requirementMet()) {
                    return false;
                }
            }
        } catch (Exception e) {
            LOGGER.error("[FANCYMENU] Error while checking LoadingRequirements of LoadingRequirementContainer!");
            e.printStackTrace();
            return false;
        }
        return true;
    }

    @Nullable
    public LoadingRequirementGroup createAndAddGroup(@NotNull String identifier, @NotNull LoadingRequirementGroup.GroupMode mode) {
        if (!this.groupExists(identifier)) {
            LoadingRequirementGroup g = new LoadingRequirementGroup(identifier, mode, this);
            this.groups.add(g);
            return g;
        }
        return null;
    }

    public boolean addGroup(LoadingRequirementGroup group) {
        if (!this.groupExists(group.identifier)) {
            this.groups.add(group);
            return true;
        }
        return false;
    }

    public List<LoadingRequirementGroup> getGroups() {
        return new ArrayList<>(this.groups);
    }

    @Nullable
    public LoadingRequirementGroup getGroup(String identifier) {
        for (LoadingRequirementGroup g : this.groups) {
            if (g.identifier.equals(identifier)) {
                return g;
            }
        }
        return null;
    }

    public boolean groupExists(String identifier) {
        return this.getGroup(identifier) != null;
    }

    public boolean removeGroup(LoadingRequirementGroup group) {
        return this.groups.remove(group);
    }

    public boolean removeGroupByIdentifier(String identifier) {
        LoadingRequirementGroup g = this.getGroup(identifier);
        if (g != null) {
            return this.groups.remove(g);
        }
        return false;
    }

    public boolean addInstance(LoadingRequirementInstance instance) {
        if (!this.instances.contains(instance)) {
            this.instances.add(instance);
            return true;
        }
        return false;
    }

    public boolean removeInstance(LoadingRequirementInstance instance) {
        return this.instances.remove(instance);
    }

    public List<LoadingRequirementInstance> getInstances() {
        return new ArrayList<>(this.instances);
    }

    public void serializeContainerToExistingPropertiesSection(@NotNull PropertiesSection target) {
        PropertiesSection sec = serializeRequirementContainer(this);
        for (Map.Entry<String, String> m : sec.getEntries().entrySet()) {
            target.addEntry(m.getKey(), m.getValue());
        }
    }

    @NotNull
    public static PropertiesSection serializeRequirementContainer(LoadingRequirementContainer container) {
        PropertiesSection sec = new PropertiesSection("loading_requirement_container");
        for (LoadingRequirementGroup g : container.groups) {
            PropertiesSection sg = LoadingRequirementGroup.serializeRequirementGroup(g);
            for (Map.Entry<String, String> m : sg.getEntries().entrySet()) {
                sec.addEntry(m.getKey(), m.getValue());
            }
        }
        for (LoadingRequirementInstance i : container.instances) {
            List<String> l = LoadingRequirementInstance.serializeRequirementInstance(i);
            sec.addEntry(l.get(0), l.get(1));
        }
        return sec;
    }

    @NotNull
    public static LoadingRequirementContainer deserializeRequirementContainer(PropertiesSection sec) {
        LoadingRequirementContainer c = new LoadingRequirementContainer();
        for (Map.Entry<String, String> m : sec.getEntries().entrySet()) {
            if (m.getKey().startsWith("[loading_requirement_group:")) {
                LoadingRequirementGroup g = LoadingRequirementGroup.deserializeRequirementGroup(m.getKey(), m.getValue(), c);
                if (g != null) {
                    c.addGroup(g);
                }
            }
        }
        for (Map.Entry<String, String> m : sec.getEntries().entrySet()) {
            if (m.getKey().startsWith("[loading_requirement:")) {
                LoadingRequirementInstance i = LoadingRequirementInstance.deserializeRequirementInstance(m.getKey(), m.getValue(), c);
                if (i != null) {
                    if (i.group != null) {
                        i.group.addInstance(i);
                    } else {
                        c.addInstance(i);
                    }
                }
            }
        }
        LegacyRequirementConverter.deserializeLegacyAndAddTo(sec, c);
        return c;
    }

}
