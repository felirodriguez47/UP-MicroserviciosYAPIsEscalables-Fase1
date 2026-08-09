package com.vetSystem.vet_system.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Sprint 1: endpoint mínimo de verificación.
 *
 * Su único objetivo es demostrar que el contexto de Spring levanta y que el
 * DispatcherServlet mapea rutas correctamente. El CRUD real de Dueño
 * (GET/POST/PUT/DELETE contra la base) se implementa en el Sprint 2,
 * cuando existan DuenoRepository y DuenoService.
 */
@RestController
@RequestMapping("/api/duenos")
public class DuenoController {

    @GetMapping
    public String estado() {
        return "vet-system OK - Sprint 1. CRUD de duenos: Sprint 2.";
    }
}
