package com.example.demo_audit;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public final class AuditDiffComputer {

    public static Map<String, Map<String, String>> computeChanges(final Map<String, Object> oldObj,
                                                                  final Map<String, Object> newObj,
                                                                  final Set<String> ignoredFields) {
        Map<String, Map<String, String>> changes = new HashMap<>();

        for (String key : oldObj.keySet()) {
            if (ignoredFields != null && ignoredFields.contains(key)) {
                continue; // skip ignored fields
            }

            Object oldValue = oldObj.get(key);
            Object newValue = newObj.get(key);

            if (!Objects.equals(oldValue, newValue)) {
                Map<String, String> diff = new HashMap<>();
                diff.put("old", oldValue == null ? null : oldValue.toString());
                diff.put("new", newValue == null ? null : newValue.toString());
                changes.put(key, diff);
            }
        }

        return changes;
    }
} 