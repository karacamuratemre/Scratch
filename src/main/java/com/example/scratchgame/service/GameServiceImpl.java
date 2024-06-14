package com.example.scratchgame.service;

import com.example.scratchgame.config.*;
import com.example.scratchgame.game.GameRequest;
import com.example.scratchgame.game.GameResponse;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class GameServiceImpl implements GameService {

    private final Random random = new Random();

    @Override
    public GameResponse playGame(GameRequest request, Config config) {
        int betAmount = request.getBetAmount();
        List<List<String>> matrix = generateMatrix(config);

        // Calculate applied winning combinations
        Map<String, List<String>> appliedWinningCombinations = calculateAppliedWinningCombinations(matrix, config);

        // Calculate reward
        double reward = calculateReward(matrix, betAmount, config, appliedWinningCombinations);

        // Apply bonus symbol only if there are winning combinations
        String appliedBonusSymbol = null;
        if (!appliedWinningCombinations.isEmpty()) {
            appliedBonusSymbol = calculateAppliedBonusSymbol(config);
            reward = applyBonusSymbol(appliedBonusSymbol, reward);
        } else {
            reward = 0;
        }

        GameResponse response = new GameResponse();
        response.setMatrix(matrix);
        response.setReward(reward);
        response.setAppliedWinningCombinations(appliedWinningCombinations);
        response.setAppliedBonusSymbol(appliedBonusSymbol);

        return response;
    }

    private List<List<String>> generateMatrix(Config config) {
        int columns = config.getColumns();
        int rows = config.getRows();
        List<List<String>> matrix = new ArrayList<>();

        for (int i = 0; i < rows; i++) {
            List<String> row = new ArrayList<>();
            for (int j = 0; j < columns; j++) {
                String symbol = generateRandomSymbol(config.getProbabilities().getStandardSymbols(), j, i);
                row.add(symbol);
            }
            matrix.add(row);
        }

        // Randomly add bonus symbols
        int numberOfBonusSymbols = random.nextInt(columns * rows / 4); // Random number of bonus symbols to add
        for (int i = 0; i < numberOfBonusSymbols; i++) {
            int randomRow = random.nextInt(rows);
            int randomCol = random.nextInt(columns);
            String bonusSymbol = generateRandomBonusSymbol(config.getProbabilities().getBonusSymbols());
            matrix.get(randomRow).set(randomCol, bonusSymbol);
        }

        return matrix;
    }

    private String generateRandomSymbol(List<Probability> probabilities, int column, int row) {
        Map<String, Integer> symbolProbabilities = probabilities.stream()
                .filter(p -> p.getColumn() == column && p.getRow() == row)
                .findFirst()
                .orElse(probabilities.get(0))
                .getSymbols();

        double totalProbability = symbolProbabilities.values().stream().mapToDouble(Integer::doubleValue).sum();
        double randomValue = random.nextDouble() * totalProbability;

        double cumulativeProbability = 0;
        for (Map.Entry<String, Integer> entry : symbolProbabilities.entrySet()) {
            cumulativeProbability += entry.getValue();
            if (randomValue < cumulativeProbability) {
                return entry.getKey();
            }
        }
        throw new RuntimeException("Failed to generate symbol based on probabilities.");
    }

    private String generateRandomBonusSymbol(BonusSymbols bonusSymbols) {
        double totalProbability = calculateTotalProbability(bonusSymbols);
        double randomValue = random.nextDouble() * totalProbability;

        double cumulativeProbability = 0;
        for (Map.Entry<String, Integer> entry : bonusSymbols.getSymbols().entrySet()) {
            cumulativeProbability += entry.getValue();
            if (randomValue < cumulativeProbability) {
                return entry.getKey();
            }
        }
        throw new RuntimeException("Failed to generate bonus symbol based on probabilities.");
    }

    private double calculateTotalProbability(BonusSymbols bonusSymbols) {
        return bonusSymbols.getSymbols().values().stream().mapToDouble(Integer::doubleValue).sum();
    }

    private double calculateReward(List<List<String>> matrix, int betAmount, Config config, Map<String, List<String>> appliedWinningCombinations) {
        double totalReward = 0;

        for (Map.Entry<String, List<String>> entry : appliedWinningCombinations.entrySet()) {
            String symbol = entry.getKey();
            List<String> combinations = entry.getValue();

            Symbol symbolConfig = config.getSymbols().get(symbol);
            if (symbolConfig != null && symbolConfig.getType().equals("standard")) {
                double symbolReward = betAmount * symbolConfig.getRewardMultiplier();
                for (String combination : combinations) {
                    WinCombination winCombination = config.getWinCombinations().get(combination);
                    symbolReward *= winCombination.getRewardMultiplier();
                }
                totalReward += symbolReward;
            }
        }

        return totalReward;
    }

    private Map<String, List<String>> calculateAppliedWinningCombinations(List<List<String>> matrix, Config config) {
        Map<String, List<String>> appliedWinningCombinations = new HashMap<>();
        checkCombinations(matrix, config, appliedWinningCombinations, config.getWinCombinations().get("same_symbols_horizontally").getCoveredAreas(), "same_symbols_horizontally");
        checkCombinations(matrix, config, appliedWinningCombinations, config.getWinCombinations().get("same_symbols_vertically").getCoveredAreas(), "same_symbols_vertically");
        checkCombinations(matrix, config, appliedWinningCombinations, config.getWinCombinations().get("same_symbols_diagonally_left_to_right").getCoveredAreas(), "same_symbols_diagonally_left_to_right");
        checkCombinations(matrix, config, appliedWinningCombinations, config.getWinCombinations().get("same_symbols_diagonally_right_to_left").getCoveredAreas(), "same_symbols_diagonally_right_to_left");

        // Check for same symbols count
        for (String symbol : config.getSymbols().keySet()) {
            long count = matrix.stream().flatMap(List::stream).filter(s -> s.equals(symbol)).count();
            for (Map.Entry<String, WinCombination> entry : config.getWinCombinations().entrySet()) {
                WinCombination winCombination = entry.getValue();
                if (winCombination.getWhen().equals("same_symbols") && count >= winCombination.getCount()) {
                    appliedWinningCombinations.putIfAbsent(symbol, new ArrayList<>());
                    appliedWinningCombinations.get(symbol).add(entry.getKey());
                }
            }
        }

        return appliedWinningCombinations;
    }

    private void checkCombinations(List<List<String>> matrix, Config config, Map<String, List<String>> appliedWinningCombinations, List<List<String>> coveredAreas, String combinationName) {
        for (List<String> area : coveredAreas) {
            String firstSymbol = null;
            boolean match = true;
            for (String position : area) {
                String[] pos = position.split(":");
                int row = Integer.parseInt(pos[0]);
                int col = Integer.parseInt(pos[1]);
                String symbol = matrix.get(row).get(col);

                if (firstSymbol == null) {
                    firstSymbol = symbol;
                } else if (!firstSymbol.equals(symbol)) {
                    match = false;
                    break;
                }
            }
            if (match && firstSymbol != null) {
                appliedWinningCombinations.putIfAbsent(firstSymbol, new ArrayList<>());
                appliedWinningCombinations.get(firstSymbol).add(combinationName);
            }
        }
    }

    private String calculateAppliedBonusSymbol(Config config) {
        Probabilities probabilities = config.getProbabilities();
        BonusSymbols bonusSymbols = probabilities.getBonusSymbols();
        double totalProbability = calculateTotalProbability(bonusSymbols);
        double randomValue = random.nextDouble() * totalProbability;
        double cumulativeProbability = 0;

        for (Map.Entry<String, Integer> entry : bonusSymbols.getSymbols().entrySet()) {
            cumulativeProbability += entry.getValue();
            if (randomValue < cumulativeProbability) {
                return entry.getKey();
            }
        }
        return null;
    }

    private double applyBonusSymbol(String bonusSymbol, double reward) {
        if (bonusSymbol == null || reward == 0) {
            return reward;
        }
        switch (bonusSymbol) {
            case "10x":
                return reward * 10;
            case "5x":
                return reward * 5;
            case "+1000":
                return reward + 1000;
            case "+500":
                return reward + 500;
            default:
                return reward;
        }
    }
}
