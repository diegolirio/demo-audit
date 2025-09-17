package com.example.demo_audit.controllers

import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/audit")
class AuditController {
    private val logger = LoggerFactory.getLogger(AuditController::class.java)

    @PostMapping
    fun createAudit(
        @RequestBody request: AuditRequest,
    ): AuditResponse {
        logger.info(request.toString())
        // Here you would typically call a service to handle the business logic
        return AuditResponse(message = "Audit created successfully")
    }
}
