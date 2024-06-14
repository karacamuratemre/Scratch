package com.example.scratchgame;

import com.example.scratchgame.config.*;
import com.example.scratchgame.game.GameRequest;
import com.example.scratchgame.game.GameResponse;
import com.example.scratchgame.service.GameServiceImpl;
import static org.junit.jupiter.api.Assertions.*;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GameServiceTest {


    private GameServiceImpl gameService;
    private Config config;

    @BeforeEach
    void setUp() {
        gameService = new GameServiceImpl();
        config = createTestConfig();
    }

    @Test
    void testGenerateMatrix() {
        GameRequest request = new GameRequest();
        request.setBetAmount(100);
        GameResponse response = gameService.playGame(request, config);

        List<List<String>> matrix = response.getMatrix();
        assertNotNull(matrix);
        assertEquals(3, matrix.size());
        assertEquals(3, matrix.get(0).size());
    }

    @Test
    void testCalculateRewardWithWinningCombination() {
        GameRequest request = new GameRequest();
        request.setBetAmount(100);
        GameResponse response = gameService.playGame(request, config);

        assertNotNull(response);
        assertTrue(response.getReward() > 0);
        assertNotNull(response.getAppliedWinningCombinations());
        assertFalse(response.getAppliedWinningCombinations().isEmpty());
    }

    @Test
    void testGameResponseFormat() {
        GameRequest request = new GameRequest();
        request.setBetAmount(100);
        GameResponse response = gameService.playGame(request, config);

        assertNotNull(response);
        assertNotNull(response.getMatrix());
        assertTrue(response.getReward() >= 0);
        assertNotNull(response.getAppliedWinningCombinations());
        assertNotNull(response.getAppliedBonusSymbol());
    }

    private Config createTestConfig() {
        Config config = new Config();
        config.setColumns(3);
        config.setRows(3);

        Map<String, Symbol> symbols = new HashMap<>();
        symbols.put("A", createSymbol(5, "standard", null, null));
        symbols.put("B", createSymbol(3, "standard", null, null));
        symbols.put("C", createSymbol(2.5, "standard", null, null));
        symbols.put("D", createSymbol(2, "standard", null, null));
        symbols.put("E", createSymbol(1.2, "standard", null, null));
        symbols.put("F", createSymbol(1, "standard", null, null));
        symbols.put("10x", createSymbol(10, "bonus", null, "multiply_reward"));
        symbols.put("5x", createSymbol(5, "bonus", null, "multiply_reward"));
        symbols.put("+1000", createSymbol(0, "bonus", 1000.0, "extra_bonus"));
        symbols.put("+500", createSymbol(0, "bonus", 500.0, "extra_bonus"));
        symbols.put("MISS", createSymbol(0, "bonus", null, "miss"));
        config.setSymbols(symbols);

        List<Probability> standardSymbols = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                standardSymbols.add(createProbability(i, j, Map.of(
                        "A", 1,
                        "B", 2,
                        "C", 3,
                        "D", 4,
                        "E", 5,
                        "F", 6
                )));
            }
        }
        BonusSymbols bonusSymbols = new BonusSymbols();
        bonusSymbols.setSymbols(Map.of(
                "10x", 1,
                "5x", 2,
                "+1000", 3,
                "+500", 4,
                "MISS", 5
        ));
        Probabilities probabilities = new Probabilities();
        probabilities.setStandardSymbols(standardSymbols);
        probabilities.setBonusSymbols(bonusSymbols);
        config.setProbabilities(probabilities);

        Map<String, WinCombination> winCombinations = new HashMap<>();
        winCombinations.put("same_symbol_3_times", createWinCombination(1, "same_symbols", 3, "same_symbols", null));
        winCombinations.put("same_symbol_4_times", createWinCombination(1.5, "same_symbols", 4, "same_symbols", null));
        winCombinations.put("same_symbol_5_times", createWinCombination(2, "same_symbols", 5, "same_symbols", null));
        winCombinations.put("same_symbol_6_times", createWinCombination(3, "same_symbols", 6, "same_symbols", null));
        winCombinations.put("same_symbol_7_times", createWinCombination(5, "same_symbols", 7, "same_symbols", null));
        winCombinations.put("same_symbol_8_times", createWinCombination(10, "same_symbols", 8, "same_symbols", null));
        winCombinations.put("same_symbol_9_times", createWinCombination(20, "same_symbols", 9, "same_symbols", null));
        winCombinations.put("same_symbols_horizontally", createWinCombination(2, "linear_symbols", 0, "horizontally_linear_symbols", List.of(
                List.of("0:0", "0:1", "0:2"),
                List.of("1:0", "1:1", "1:2"),
                List.of("2:0", "2:1", "2:2")
        )));
        winCombinations.put("same_symbols_vertically", createWinCombination(2, "linear_symbols", 0, "vertically_linear_symbols", List.of(
                List.of("0:0", "1:0", "2:0"),
                List.of("0:1", "1:1", "2:1"),
                List.of("0:2", "1:2", "2:2")
        )));
        winCombinations.put("same_symbols_diagonally_left_to_right", createWinCombination(5, "linear_symbols", 0, "ltr_diagonally_linear_symbols", List.of(
                List.of("0:0", "1:1", "2:2")
        )));
        winCombinations.put("same_symbols_diagonally_right_to_left", createWinCombination(5, "linear_symbols", 0, "rtl_diagonally_linear_symbols", List.of(
                List.of("0:2", "1:1", "2:0")
        )));
        config.setWinCombinations(winCombinations);

        return config;
    }

    private Symbol createSymbol(double rewardMultiplier, String type, Double extra, String impact) {
        Symbol symbol = new Symbol();
        symbol.setRewardMultiplier(rewardMultiplier);
        symbol.setType(type);
        symbol.setExtra(extra);
        symbol.setImpact(impact);
        return symbol;
    }

    private Probability createProbability(int column, int row, Map<String, Integer> symbols) {
        Probability probability = new Probability();
        probability.setColumn(column);
        probability.setRow(row);
        probability.setSymbols(symbols);
        return probability;
    }

    private WinCombination createWinCombination(double rewardMultiplier, String when, int count, String group, List<List<String>> coveredAreas) {
        WinCombination winCombination = new WinCombination();
        winCombination.setRewardMultiplier(rewardMultiplier);
        winCombination.setWhen(when);
        winCombination.setCount(count);
        winCombination.setGroup(group);
        winCombination.setCoveredAreas(coveredAreas);
        return winCombination;
    }

    private Config modifyConfigForNoWinScenario(Config config) {
        Config noWinConfig = createTestConfig();
        // Clear win combinations and set to empty to ensure no winning combination
        noWinConfig.setWinCombinations(new HashMap<>());
        return noWinConfig;
    }
}
