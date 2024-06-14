package com.example.scratchgame.config;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

public class Probability {
    @JsonProperty("column")
    private int column;
    @JsonProperty("row")
    private int row;
    @JsonProperty("symbols")
    private Map<String, Integer> symbols;

    public int getColumn() {
        return column;
    }

    public void setColumn(int column) {
        this.column = column;
    }

    public int getRow() {
        return row;
    }

    public void setRow(int row) {
        this.row = row;
    }

    public Map<String, Integer> getSymbols() {
        return symbols;
    }

    public void setSymbols(Map<String, Integer> symbols) {
        this.symbols = symbols;
    }
}
