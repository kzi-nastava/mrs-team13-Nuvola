package Nuvola.Projekatsiit2025.controller;

import Nuvola.Projekatsiit2025.dto.PanicCreateRequest;
import Nuvola.Projekatsiit2025.dto.PanicEventDto;
import Nuvola.Projekatsiit2025.services.PanicService;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/panic")
public class PanicController {

    private final PanicService service;

    public PanicController(PanicService service) {
        this.service = service;
    }

    @PostMapping
    public PanicEventDto trigger(@RequestBody PanicCreateRequest req) {
        return service.trigger(req);
    }

    @PatchMapping("/{id}/resolve")
    public void resolve(@PathVariable UUID id) {
        service.resolve(id);
    }
}
