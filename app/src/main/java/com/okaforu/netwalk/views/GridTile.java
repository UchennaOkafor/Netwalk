package com.okaforu.netwalk.views;

import android.content.Context;
import android.widget.ImageView;

import com.okaforu.netwalk.models.GridLocation;

public class GridTile extends ImageView {

    private GridLocation gridLocation;

    public GridTile(Context context) {
        super(context);
    }

    public GridTile(Context context, GridLocation gridLocation) {
        this(context);
        this.gridLocation = gridLocation;
    }

    public GridLocation getGridLocation() {
        return gridLocation;
    }
}