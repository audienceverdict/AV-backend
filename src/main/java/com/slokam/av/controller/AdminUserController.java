package com.slokam.av.controller;

import com.slokam.av.dto.AccountStatusRequest;
import com.slokam.av.dto.RoleUpdateRequest;
import com.slokam.av.dto.UserResponse;
import com.slokam.av.service.AuthService;

import jakarta.validation.Valid;

import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/users")
public class AdminUserController {
    private final AuthService service;

    public AdminUserController(AuthService service) {
        this.service = service;
    }

    @GetMapping
    public Page<UserResponse> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return service.list(page, size);
    }

    @GetMapping("/{id}")
    public UserResponse get(@PathVariable String id) {
        return service.current(id);
    }

    @PatchMapping("/{id}/role")
    public UserResponse role(@PathVariable String id, @Valid @RequestBody RoleUpdateRequest r) {
        return service.role(id, r.role());
    }

    @PatchMapping("/{id}/status")
    public UserResponse status(
            @PathVariable String id, @Valid @RequestBody AccountStatusRequest r) {
        return service.status(id, r.enabled());
    }
}
