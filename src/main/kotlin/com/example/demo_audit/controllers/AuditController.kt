package com.example.demo_audit.controllers

import com.example.demo_audit.domain.Audit
import com.example.demo_audit.repository.AuditRepository
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/audit")
class AuditController(
    private val auditRepository: AuditRepository,
) {
    private val logger = LoggerFactory.getLogger(AuditController::class.java)

    @PostMapping
    fun createAudit(
        @RequestBody request: AuditRequest,
    ): AuditResponse {
        logger.info(request.toString())
        val domain =
            Audit.create(
                before = request.before,
                after = request.after,
                origin = request.origin,
                userAgent = request.userAgent,
                ignoredFields = request.ignoredFields,
            )
        val saved = auditRepository.save(domain)
        logger.info(saved.toString())
        return AuditResponse(message = "Audit created successfully")
    }

    @GetMapping
    fun getAudits(): List<Audit> = auditRepository.fundAll()
}
