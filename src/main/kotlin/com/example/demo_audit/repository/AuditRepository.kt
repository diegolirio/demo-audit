package com.example.demo_audit.repository

import com.example.demo_audit.domain.Audit
import org.springframework.stereotype.Repository
import java.util.*

@Repository
class AuditRepository {
    private val db: MutableList<Audit> = mutableListOf()

    fun save(audit: Audit): Audit {
        audit.id = UUID.randomUUID()
        db.add(audit)
        return audit
    }

    fun fundAll(): List<Audit> = db
}
