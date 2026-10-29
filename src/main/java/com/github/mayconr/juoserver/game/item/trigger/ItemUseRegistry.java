package com.github.mayconr.juoserver.game.item.trigger;

import com.github.mayconr.juoserver.game.item.template.ItemTemplate;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class ItemUseRegistry {
    private final Map<String, ItemUseTrigger> triggers = new HashMap<>();

    public void register(String script, ItemUseTrigger trigger) {
        if (script == null || script.isBlank()) {
            throw new IllegalArgumentException("Item script name is required");
        }
        Objects.requireNonNull(trigger, "Item trigger is required");
        if (triggers.putIfAbsent(script, trigger) != null) {
            throw new IllegalArgumentException("Item script already registered: " + script);
        }
    }

    public void validateTemplates(Iterable<ItemTemplate> templates) {
        for (var template : templates) {
            if (template.script() != null && !triggers.containsKey(template.script())) {
                throw new IllegalArgumentException("Unknown item script '" + template.script()
                        + "' in template '" + template.name() + "'");
            }
        }
    }

    public boolean dispatch(ItemUseContext context) {
        var template = context.item().getTemplate();
        if (template == null || template.script() == null) return false;
        var trigger = triggers.get(template.script());
        if (trigger == null) return false;
        trigger.execute(context);
        return true;
    }
}
