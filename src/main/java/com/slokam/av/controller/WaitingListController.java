package com.slokam.av.controller;

import com.slokam.av.dto.WaitingListRequest;
import com.slokam.av.entity.WaitingListEntry;
import com.slokam.av.service.WaitingListService;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.*;

@RestController
@RequestMapping("/api/v1/waiting-list")
public class WaitingListController {
    private final WaitingListService service;

    public WaitingListController(WaitingListService service) {
        this.service = service;
    }

    @GetMapping
    public List<WaitingListEntry> mine(Principal p) {
        return service.mine(p);
    }

    @GetMapping("/admin")
    public List<WaitingListEntry> all() {
        return service.all();
    }

    @PostMapping
    public WaitingListEntry join(Principal p, @Valid @RequestBody WaitingListRequest r) {
        return service.join(p, r);
    }

    @DeleteMapping("/{id}")
    public void cancel(@PathVariable String id, Principal p) {
        service.cancel(id, p);
    }

    @PostMapping("/{id}/claim")
    public WaitingListEntry claim(@PathVariable String id, Principal p) {
        return service.claim(id, p);
    }
}
