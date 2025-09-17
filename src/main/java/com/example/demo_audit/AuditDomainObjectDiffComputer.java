package com.example.demo_audit;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Comparator;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import static java.lang.String.format;
import static java.util.stream.Collectors.joining;

public final class AuditDomainObjectDiffComputer {

    private AuditDomainObjectDiffComputer() {
        // Utility class
    }

    public static Map<String, Map<String, String>> computeChanges(final Object oldObj,
                                                                  final Object newObj,
                                                                  final Set<String> ignoredFields) {
        validateInputs(oldObj, newObj);
        return computeChangesInternal(oldObj, newObj, ignoredFields, new IdentityHashMap<>());
    }

    private static void validateInputs(final Object oldObj, final Object newObj) {
        if (oldObj == null || newObj == null) {
            throw new IllegalArgumentException("Objects cannot be null");
        }

        if (!oldObj.getClass().equals(newObj.getClass())) {
            throw new IllegalArgumentException("Objects must be of the same type");
        }
    }

    private static Map<String, Map<String, String>> computeChangesInternal(final Object oldObj,
                                                                           final Object newObj,
                                                                           final Set<String> ignoredFields,
                                                                           final Map<Object, Object> visitedObjects) {
        if (visitedObjects.containsKey(oldObj) || visitedObjects.containsKey(newObj)) {
            return new LinkedHashMap<>();
        }
        visitedObjects.put(oldObj, oldObj);
        visitedObjects.put(newObj, newObj);

        final Map<String, Map<String, String>> changes = new LinkedHashMap<>();
        final Class<?> clazz = oldObj.getClass();

        for (final Field field : clazz.getDeclaredFields()) {
            field.setAccessible(true); // NOSONAR: Needed to access private fields
            try {
                final Object oldVal = field.get(oldObj);
                final Object newVal = field.get(newObj);
                processFieldChanges(changes, field, oldVal, newVal);
            } catch (final IllegalAccessException e) {
                throw new IllegalStateException(
                        format("Error accessing field: %s with error: %s", field.getName(), e.getMessage()));
            }
        }
        return changes;
    }

    private static void processFieldChanges(final Map<String, Map<String, String>> changes,
                                            final Field field,
                                            final Object oldVal,
                                            final Object newVal) {
        if (isCollection(field.getType())) {
            processCollectionChanges(changes, field.getName(), oldVal, newVal);
        } else if (isMap(field.getType())) {
            processMapChanges(changes, field.getName(), oldVal, newVal);
        } else if (!Objects.equals(oldVal, newVal)) {
            changes.put(field.getName(), createChangeMap(oldVal, newVal));
        }
    }

    private static void processCollectionChanges(final Map<String, Map<String, String>> changes,
                                                 final String fieldName,
                                                 final Object oldVal,
                                                 final Object newVal) {
        if (oldVal != null && newVal != null) {
            final Collection<?> oldCollection = (Collection<?>) oldVal;
            final Collection<?> newCollection = (Collection<?>) newVal;

            if (oldCollection.isEmpty() && newCollection.isEmpty()) {
                return;
            }

            if (!Objects.equals(oldCollection.size(), newCollection.size()) || !Objects.equals(oldCollection, newCollection)) {

            }
        }
    }

    private static String processCollectionElementsRecursively(final Collection<?> collection,
                                                               final int depth) {
        return processCollectionElementsRecursively(collection, depth, new IdentityHashMap<>());
    }

    private static String processCollectionElementsRecursively(final Collection<?> collection,
                                                               final int depth,
                                                               final Map<Object, Object> visitedObjects) {
        if (collection.isEmpty()) {
            return "[]";
        }

        return collection.stream()
                .map(element -> processCollectionElement(element, depth, visitedObjects))
                .collect(joining(", ", "[", "]"));
    }

    private static String processCollectionElement(final Object element,
                                                   final int depth,
                                                   final Map<Object, Object> visitedObjects) {
        if (element == null) {
            return "null";
        }

        if (element instanceof Enum<?>) {
            return "\"" + element + "\"";
        }

        if (isCollection(element.getClass())) {
            return processCollectionElementsRecursively((Collection<?>) element, depth + 1, visitedObjects);
        }

        if (element instanceof Map<?, ?> map) {
            return formatMapElements(map);
        }

        return "\"" + element + "\"";
    }

    private static String processDomainObjectWithNestedFields(final Object element,
                                                              final List<String> packagesPrefix,
                                                              final int depth,
                                                              final Map<Object, Object> visitedObjects) {
        if (visitedObjects.containsKey(element)) {
            return "\"{...}\"";
        }
        visitedObjects.put(element, element);

        if (element instanceof Enum<?>) {
            return "\"" + element + "\"";
        }

        final var fields = element.getClass().getDeclaredFields();
        final var mainFields = new StringBuilder();

        if (hasId(element) && hasName(element)) {
            appendIdAndName(element, mainFields);
        } else if (hasId(element)) {
            appendIdIfExists(element, mainFields);
        } else {
            appendAllFields(element, fields, mainFields, packagesPrefix, depth, visitedObjects);
        }

        return "{" + mainFields + "}";
    }

    private static boolean hasId(final Object element) {
        try {
            final var idField = element.getClass().getDeclaredField("id");
            idField.setAccessible(true); // NOSONAR: Needed to access private fields
            final var id = idField.get(element);
            return id != null;
        } catch (final NoSuchFieldException | IllegalAccessException e) {
            return false;
        }
    }

    private static boolean hasName(final Object element) {
        try {
            final var nameField = element.getClass().getDeclaredField("name");
            nameField.setAccessible(true); // NOSONAR: Needed to access private fields
            final var name = nameField.get(element);
            return name != null;
        } catch (final NoSuchFieldException | IllegalAccessException e) {
            return false;
        }
    }

    private static void appendIdAndName(final Object element, final StringBuilder mainFields) {
        try {
            final var idField = element.getClass().getDeclaredField("id");
            final var nameField = element.getClass().getDeclaredField("name");
            idField.setAccessible(true); // NOSONAR: Needed to access private fields
            nameField.setAccessible(true); // NOSONAR: Needed to access private fields
            final var id = idField.get(element);
            final var name = nameField.get(element);
            mainFields.append("\"id\": \"").append(id).append("\", \"name\": \"").append(name).append("\"");
        } catch (final NoSuchFieldException | IllegalAccessException e) {
            // Não deve acontecer pois já verificamos que tem id e name
        }
    }

    private static void appendAllFields(final Object element,
                                        final Field[] fields,
                                        final StringBuilder mainFields,
                                        final List<String> packagesPrefix,
                                        final int depth,
                                        final Map<Object, Object> visitedObjects) {
        appendIdIfExists(element, mainFields);

        for (final var field : fields) {
            appendField(element, field, mainFields, packagesPrefix, depth, visitedObjects);
        }
    }

    private static void appendIdIfExists(final Object element, final StringBuilder mainFields) {
        try {
            final var idField = element.getClass().getDeclaredField("id");
            idField.setAccessible(true); // NOSONAR: Needed to access private fields
            final var id = idField.get(element);
            if (id != null) {
                mainFields.append("\"id\": \"").append(id).append("\"");
            }
        } catch (final NoSuchFieldException | IllegalAccessException e) {
            // Ignora se não tiver id
        }
    }

    private static void appendField(final Object element,
                                    final Field field,
                                    final StringBuilder mainFields,
                                    final List<String> packagesPrefix,
                                    final int depth,
                                    final Map<Object, Object> visitedObjects) {
        field.setAccessible(true); // NOSONAR: Needed to access private fields
        try {
            final var value = field.get(element);
            if (value == null) {
                return;
            }

            if (!mainFields.isEmpty()) {
                mainFields.append(", ");
            }

            if (value instanceof Enum<?>) {
                appendEnumField(field, value, mainFields);
            } else if (isCollection(field.getType())) {
                appendCollectionField(field, value, mainFields, packagesPrefix, depth, visitedObjects);
            } else if (!field.getName().startsWith("$")) {
                appendSimpleField(field, value, mainFields);
            }
        } catch (final IllegalAccessException e) {
            // Ignora campos que não podem ser acessados
        }
    }

    private static void appendEnumField(final Field field, final Object value, final StringBuilder mainFields) {
        mainFields.append("\"").append(field.getName()).append("\": \"").append(value).append("\"");
    }

    private static void appendDomainObjectField(final Field field,
                                                final Object value,
                                                final StringBuilder mainFields,
                                                final List<String> packagesPrefix,
                                                final int depth,
                                                final Map<Object, Object> visitedObjects) {
        mainFields.append("\"").append(field.getName()).append("\": ")
                .append(processDomainObjectWithNestedFields(value, packagesPrefix, depth + 1, visitedObjects));
    }

    private static void appendCollectionField(final Field field,
                                              final Object value,
                                              final StringBuilder mainFields,
                                              final List<String> packagesPrefix,
                                              final int depth,
                                              final Map<Object, Object> visitedObjects) {
        final var collection = (Collection<?>) value;
        if (!collection.isEmpty()) {
            mainFields.append("\"").append(field.getName()).append("\": ")
                    .append(processCollectionElementsRecursively(collection, depth + 1, visitedObjects));
        }
    }

    private static void appendSimpleField(final Field field, final Object value, final StringBuilder mainFields) {
        mainFields.append("\"").append(field.getName()).append("\": \"").append(value).append("\"");
    }

    private static void processDomainObjectChanges(final Map<String, Map<String, String>> changes,
                                                   final String fieldName,
                                                   final Object oldVal,
                                                   final Object newVal) {
        if (oldVal != null && newVal != null) {
            try {
                final Field idField = oldVal.getClass().getDeclaredField("id");
                idField.setAccessible(true); // NOSONAR: Needed to access private fields
                final UUID oldId = (UUID) idField.get(oldVal);
                final UUID newId = (UUID) idField.get(newVal);

                try {
                    final Field nameField = oldVal.getClass().getDeclaredField("name");
                    nameField.setAccessible(true); // NOSONAR: Needed to access private fields
                    final String oldName = (String) nameField.get(oldVal);
                    final String newName = (String) nameField.get(newVal);

                    if (!Objects.equals(oldId, newId) || !Objects.equals(oldName, newName)) {
                        changes.put(fieldName + ".old", Map.of(
                                "id", String.valueOf(oldId),
                                "name", oldName
                        ));
                        changes.put(fieldName + ".new", Map.of(
                                "id", String.valueOf(newId),
                                "name", newName
                        ));
                    }
                } catch (final NoSuchFieldException e) {
                    // If name field doesn't exist, just compare ids
                    if (!Objects.equals(oldId, newId)) {
                        changes.put(fieldName + ".id", createChangeMap(oldId, newId));
                    }
                }
            } catch (final NoSuchFieldException | IllegalAccessException e) {
                if (!Objects.equals(oldVal, newVal)) {
                    changes.put(fieldName, createChangeMap(oldVal, newVal));
                }
            }
        } else if (oldVal != newVal) {
            changes.put(fieldName, createChangeMap(oldVal, newVal));
        }
    }

    private static Map<String, String> createChangeMap(final Object oldVal, final Object newVal) {
        return Map.of(
                "old", String.valueOf(oldVal),
                "new", String.valueOf(newVal)
        );
    }

    private static boolean isCollection(final Class<?> type) {
        return Collection.class.isAssignableFrom(type);
    }

    private static boolean isMap(final Class<?> type) {
        return Map.class.isAssignableFrom(type);
    }

    private static void processMapChanges(final Map<String, Map<String, String>> changes,
                                          final String fieldName,
                                          final Object oldVal,
                                          final Object newVal) {
        if (oldVal != null && newVal != null) {
            final Map<?, ?> oldMap = (Map<?, ?>) oldVal;
            final Map<?, ?> newMap = (Map<?, ?>) newVal;

            if (oldMap.isEmpty() && newMap.isEmpty()) {
                return;
            }

            if (!Objects.equals(oldMap.size(), newMap.size()) || !Objects.equals(oldMap, newMap)) {
                final var oldElements = formatMapElements(oldMap);
                final var newElements = formatMapElements(newMap);
                changes.put(fieldName, Map.of("old", oldElements, "new", newElements));
            }
        }
    }

    private static String formatMapElements(final Map<?, ?> map) {
        if (map.isEmpty()) {
            return "[]";
        }

        return map.entrySet().stream()
                .sorted(Comparator.comparing(e -> e.getKey().toString()))
                .map(entry -> "\"" + entry.getKey() + "\": \"" + entry.getValue() + "\"")
                .collect(joining(", ", "[", "]"));
    }

} 