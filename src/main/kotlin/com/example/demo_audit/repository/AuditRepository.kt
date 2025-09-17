package com.example.demo_audit.repository

import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
class AuditRepository {
    private val db: MutableList<AuditEntity> = mutableListOf()

    fun save(audit: AuditEntity): AuditEntity {
        audit.id = UUID.randomUUID()
        db.add(audit)
        return audit
    }

    fun fundAll(): List<AuditEntity> = db
}
