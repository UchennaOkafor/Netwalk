package com.okaforu.netwalk.models;

public class GridLocation {
    private final int column, row;

    public GridLocation(int column, int row) {
        this.column = column;
        this.row = row;
    }

    public int getColumn() {
        return column;
    }

    public int getRow() {
        return row;
    }
}
