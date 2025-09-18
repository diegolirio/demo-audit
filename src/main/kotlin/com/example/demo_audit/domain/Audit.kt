package com.example.demo_audit.domain

import java.util.UUID

data class Audit(
    var id: UUID? = null,
    val origin: String,
    val userAgent: String,
    val changes: Map<String, Map<String, String>>,
) {
    companion object {
        fun create(
            before: Map<String, Any>,
            after: Map<String, Any>,
            origin: String,
            userAgent: String,
            ignoredFields: Set<String>?,
        ): Audit {
            val changes = computeChanges(before, after, ignoredFields)
            return Audit(
                origin = origin,
                userAgent = userAgent,
                changes = changes,
            )
        }

        fun computeChanges(
            oldObj: Map<String, Any?>,
            newObj: Map<String, Any?>,
            ignoredFields: Set<String>? = null,
        ): Map<String, Map<String, String>> {
            val changes = mutableMapOf<String, Map<String, String>>()

            for (key in oldObj.keys) {
                if (ignoredFields?.contains(key) == true) {
                    continue
                }

                val oldValue = oldObj[key]
                val newValue = newObj[key]

                if (oldValue != newValue) {
                    val diff: Map<String, String> =
                        mapOf(
                            "old" to (oldValue?.toString() ?: ""),
                            "new" to (newValue?.toString() ?: ""),
                        )
                    changes[key] = diff
                }
            }

            return changes
        }
    }
}
