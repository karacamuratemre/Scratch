package com.example.scratchgame.service;

import com.example.scratchgame.config.Config;
import com.example.scratchgame.game.GameRequest;
import com.example.scratchgame.game.GameResponse;

public interface GameService {

    GameResponse playGame(GameRequest request, Config config);
}
