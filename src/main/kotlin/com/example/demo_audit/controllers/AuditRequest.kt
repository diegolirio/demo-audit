package com.example.demo_audit.controllers

data class AuditRequest(
    val before: Map<String, Any>,
    val after: Map<String, Any>,
    val origin: String,
    val userAgent: String,
    val ignoredFields: Set<String>?,
)
