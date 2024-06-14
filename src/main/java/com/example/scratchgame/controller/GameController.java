package com.example.scratchgame.controller;

import com.example.scratchgame.config.Config;
import com.example.scratchgame.config.ConfigLoader;
import com.example.scratchgame.game.GameRequest;
import com.example.scratchgame.game.GameResponse;
import com.example.scratchgame.service.GameService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
public class GameController {

    private final GameService gameService;

    @Autowired
    public GameController(GameService gameService) {
        this.gameService = gameService;
    }

    @PostMapping("/play")
    public GameResponse play(@RequestBody GameRequest request) throws IOException {
        Config config = ConfigLoader.loadConfig("src/main/resources/config.json");
        return gameService.playGame(request, config);
    }
}