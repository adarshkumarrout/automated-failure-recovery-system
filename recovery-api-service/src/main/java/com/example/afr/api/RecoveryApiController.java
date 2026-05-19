package com.example.afr.api;

import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.CrossOrigin;

@RestController
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173"})
@RequestMapping("/api/recovery")
public class RecoveryApiController {

    private final RecoveryCommandService recoveryCommandService;

    public RecoveryApiController(RecoveryCommandService recoveryCommandService) {
        this.recoveryCommandService = recoveryCommandService;
    }

    @PostMapping("/execute")
    public ResponseEntity<RecoveryResponse> execute(@Valid @RequestBody ExecuteRecoveryRequest request) {
        return ResponseEntity.accepted().body(RecoveryResponse.from(recoveryCommandService.execute(request)));
    }

    @GetMapping("/{idempotencyKey}")
    public RecoveryResponse get(@PathVariable String idempotencyKey) {
        return RecoveryResponse.from(recoveryCommandService.get(idempotencyKey));
    }

    @GetMapping
    public List<RecoveryResponse> recent() {
        return recoveryCommandService.recent().stream().map(RecoveryResponse::from).toList();
    }
}
