package com.game.javasem.model.map;


import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class RoomLibrary {
    private static List<RoomTemplate> templates = new ArrayList<>();

    public static void loadTemplates(List<RoomTemplate> tpl) {
        templates = new ArrayList<>(tpl);
    }

    /**
     * @param required the set of door‐dirs (e.g. ["U","R"]) plus optionally "B"
     */
    public static List<RoomTemplate> findByFlags(Set<String> required, boolean needBoss) {
        // split off the B flag if present
        Set<String> dirsOnly = required.stream()
                .filter(d -> !"B".equals(d))
                .collect(Collectors.toSet());

        return templates.stream()
                .filter(tpl -> {
                    Set<String> tplDirs = tpl.getDirectionFlags();
                    // 1) the template must support exactly those directions:
                    if (!tplDirs.equals(dirsOnly)) return false;
                    // 2) if this is a boss‐room request, it must allowB:
                    if (needBoss && !tpl.allowsBoss()) return false;
                    // 3) if this is not a boss request, we must _not_ pick a B template?
                    //    (optional—probably you want to avoid putting B rooms except at the boss)
                    if (!needBoss && tpl.allowsBoss()) return false;
                    return true;
                })
                .collect(Collectors.toList());
    }
    @Override
    public String toString() {
        return "RoomLibrary{" +
                "templates=" + templates.stream()
                .map(RoomTemplate::getId)
                .collect(Collectors.toList()) +
                '}';
    }
}