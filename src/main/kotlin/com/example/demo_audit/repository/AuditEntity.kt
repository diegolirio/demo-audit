package com.example.demo_audit.repository

import java.util.UUID

class AuditEntity(
    var id: UUID? = null,
    val origin: String,
    val userAgent: String,
    val changes: Map<String, Map<String, String>>,
)
