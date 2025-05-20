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
        return templates.stream()
                .filter(t ->
                        // must have all the directional flags
                        t.getDirectionFlags().containsAll(required.stream()
                                .filter(d->!"B".equals(d))
                                .collect(Collectors.toSet()))
                                // if boss room, template must also allow B
                                && (!needBoss || t.allowsBoss())
                )
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