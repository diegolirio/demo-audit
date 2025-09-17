package com.example.demo_audit.controllers

import com.example.demo_audit.AuditDiffComputer
import com.example.demo_audit.repository.AuditEntity
import com.example.demo_audit.repository.AuditRepository
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

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
        val computeChanges =
            AuditDiffComputer.computeChanges(
                request.before,
                request.after,
                request.ignoredFields,
            )
        val auditEntity =
            AuditEntity(
                origin = request.origin,
                userAgent = request.userAgent,
                changes = computeChanges,
            )
        auditRepository.save(auditEntity)
        logger.info(computeChanges.toString())
        return AuditResponse(message = "Audit created successfully")
    }

    @GetMapping
    fun getAudits(): List<AuditEntity> = auditRepository.fundAll()
}
