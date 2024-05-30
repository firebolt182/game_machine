package org.javaacademy.gaming_machine.controller;

import lombok.RequiredArgsConstructor;
import org.javaacademy.gaming_machine.service.GameService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping
@RequiredArgsConstructor
public class GameController {
    private final GameService gameService;

    @PostMapping("/play")
    public ResponseEntity<String> play() {
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(gameService.play());
    }

    @GetMapping("/history")
    public ResponseEntity<String> showHistory() {
        return ResponseEntity.status(HttpStatus.OK).body(gameService.showHistory());
    }
}
