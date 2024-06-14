package com.example.scratchgame.config;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class Probabilities {
    @JsonProperty("standard_symbols")
    private List<Probability> standardSymbols;
    @JsonProperty("bonus_symbols")
    private BonusSymbols bonusSymbols;

    public BonusSymbols getBonusSymbols() {
        return bonusSymbols;
    }

    public void setBonusSymbols(BonusSymbols bonusSymbols) {
        this.bonusSymbols = bonusSymbols;
    }

    public List<Probability> getStandardSymbols() {
        return standardSymbols;
    }

    public void setStandardSymbols(List<Probability> standardSymbols) {
        this.standardSymbols = standardSymbols;
    }
}
