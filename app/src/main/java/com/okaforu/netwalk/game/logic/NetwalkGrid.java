package com.okaforu.netwalk.game.logic;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * A grid representation for the netwalk game
 */
public class NetwalkGrid implements Parcelable {

    private static final int CONNECTED_MASK = 0b111111;
    private static final int CONNECTORMASK = 0b1111;
    private static final int PROPMASK = ~0b1111;

    /** Bit value to indicate a connected node */
    private static final int CONNECTED = 0b1000000;
    /** Bit value to indicate a node */
    private static final int NODE          = 0b100000;
    /** Bit value to indicate the server */
    private static final int SERVER        = 0b10000;
    /** Bit value to indicate a connector to the left */
    private static final int LEFT          = 0b1000;
    /** Bit value to indicate a connector upwards */
    private static final int UP            = 0b100;
    /** Bit value to indicate a connector to the right */
    private static final int RIGHT         = 0b10;
    /** Bit value to indicate a connector downwards */
    private static final int DOWN          = 0b1;

    private static final int[] ROTATE_RIGHT_TABLE =
            {
                    0b0000,       //0b0000
                    0b1000,       //0b0001
                    0b0001,       //0b0010
                    0b1001,       //0b0011
                    0b0010,       //0b0100
                    0b1010,       //0b0101
                    0b0011,       //0b0110
                    0b1011,       //0b0111
                    0b0100,       //0b1000
                    0b1100,       //0b1001
                    0b0101,       //0b1010
                    0b1101,       //0b1011
                    0b0110,       //0b1100
                    0b1110,       //0b1101
                    0b0111,       //0b1110
                    0b1111,       //0b1111
            };

    /** The actual grid as a flat array */
    private final int[] mGrid;

    /** The width of the grid */
    private final int mColumns;
    /** The height of the grid */
    private final int mRows;
    private final int mServerPos;

    /** A flag to indicate if the game has finished generating the grid puzzle */
    private final boolean mGridInitialized;

    /** Holds a record of which grid element in mGrid has been traversed */
    private boolean[] mTraversedGridLocations;

    public NetwalkGrid(int columns, int rows) {
        if (columns + rows <= 1) {
            throw new RuntimeException("Invalid grid size, grid must be grater than 1x2");
        }

        this.mColumns = columns;
        this.mRows = rows;
        mGrid = new int[columns * rows];

        mServerPos = generate();
        mGridInitialized = true;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeIntArray(mGrid);
        dest.writeInt(mColumns);
        dest.writeInt(mRows);
        dest.writeInt(mServerPos);
        dest.writeByte(mGridInitialized ? (byte) 1 : (byte) 0);
    }

    protected NetwalkGrid(Parcel in) {
        mGrid = in.createIntArray();
        mColumns = in.readInt();
        mRows = in.readInt();
        mServerPos = in.readInt();
        mGridInitialized = in.readByte() != 0;
    }

    public static final Parcelable.Creator<NetwalkGrid> CREATOR = new Parcelable.Creator<NetwalkGrid>() {
        @Override
        public NetwalkGrid createFromParcel(Parcel source) {
            return new NetwalkGrid(source);
        }

        @Override
        public NetwalkGrid[] newArray(int size) {
            return new NetwalkGrid[size];
        }
    };

    private int generate() {
        Random random = new Random();

        // First determine a random position for the server
        int serverPos = random.nextInt(mGrid.length);
        mGrid[serverPos] = SERVER | CONNECTED; // The server is always connected

        // The leaves list contains positions that may have a connector added.
        List<Integer> leaves = new ArrayList<>();
        leaves.add(serverPos);

        // As long as there is a place where we can add a connector
        while (leaves.size() > 0) {
            // Determine which element of the list to connect to
            int leavePos = random.nextInt(leaves.size());
            // The actual position for this element
            int leave = leaves.get(leavePos);

            // Find a connecting position
            int nextPos = nextPosFrom(leave, random);
            // Do the actual connecting (set the bits in both origin and destination)
            connect(leave, nextPos);

            // Make sure that all leaves in the list are still valid (still have an empty neighbor)
            for (int i = leaves.size() - 1; i >= 0; --i) {
                int leaf = leaves.get(i);
                if (!canExtendFrom(leaf)) {
                    leaves.remove(i);
                }
            }

            // Add this new cell if it has empty neighbors
            if (canExtendFrom(nextPos)) {
                leaves.add(nextPos);
            }

        }

        // Those cells with only a single connector are nodes/terminals so mark them
        for (int i = mGrid.length - 1; i >= 0; --i) {
            switch (mGrid[i]) { // the server has an extra bit so will not match anyway
                case LEFT:
                case RIGHT:
                case UP:
                case DOWN:
                    notifyChange();
                    mGrid[i] |= NODE;
            }
        }

        return serverPos;
    }

    public boolean canExtendFrom(final int pos) {
        switch (mGrid[pos] & CONNECTORMASK) { // don't allow more than 3 connections, just hardcode the 4 options
            case 0b1011:
            case 0b111:
            case 0b1101:
            case 0b1110:
                return false;
        }
        int x = x(pos);
        int y = y(pos);
        // At least one of the neighbors must be empty
        return (x > 0 && mGrid[pos - 1] == 0) ||
                (x + 1 < mColumns && mGrid[pos + 1] == 0) ||
                (y > 0 && mGrid[pos - mColumns] == 0) ||
                (y + 1 < mRows && mGrid[pos + mColumns] == 0);
    }

    /**
     * Connect the two positions
     * @param pos1 The first position
     * @param pos2 The second position
     */
    private void connect(final int pos1, final int pos2) {
        if (pos1 > pos2) { // If the first position is to the bottom right of the second, just reverse the parameters
            connect(pos2, pos1);
            return;
        }
        // The grid works such that horizontal cells are adjacent, we know that pos2 is to the right
        if (pos2 - pos1 == 1) {
            mGrid[pos1] |= RIGHT;
            mGrid[pos2] |= LEFT;
        } else {
            mGrid[pos1] |= DOWN;
            mGrid[pos2] |= UP;
        }
        // Call the hook that can be used to redraw
        notifyChange();
    }

    /**
     * Hook function that may be used to handle changes.
     */
    protected void notifyChange() {
        checkConnectedness();
    }

    public void rotateRight(int col, int row) {
        final int gridPos = gridPos(col, row);
        final int extraBits = mGrid[gridPos] & PROPMASK;
        mGrid[gridPos] = extraBits | ROTATE_RIGHT_TABLE[mGrid[gridPos]&CONNECTORMASK];
        notifyChange();
    }

    /**
     * Helper function to get the x coordinate of a position in the array.
     * @param gridPos Position in the array
     * @return The x coordinate
     */
    private int x(int gridPos) {
        return gridPos % mColumns;
    }

    /**
     * Helper function to get the y coordinate of a position in the array.
     * @param gridPos Position in the array
     * @return The y coordinate
     */
    private int y(int gridPos) {
        return gridPos / mColumns;
    }

    /**
     * Helper function to get the array position of a coordinate pair.
     * @param x x coordinate
     * @param y y coordinate
     * @return The position in the array
     */
    private int gridPos(int x, int y) {
        return y * mColumns + x;
    }

    /**
     * Function that finds a cell to connect to the base position
     * @param basePos The position to connect from
     * @param random The random generator to use
     * @return A new gridposition for a cell
     */
    private int nextPosFrom(final int basePos, final Random random) {
        int baseX = x(basePos); // Get the X and Y coordinates
        int baseY = y(basePos);

        while (true) { // until we have a valid position
            int direction = random.nextInt(4); // 4 directions
            int x = baseX;
            int y = baseY;
            switch (direction) {
                case 0:
                    x++;
                    break;
                case 1:
                    y++;
                    break;
                case 2:
                    x--;
                    break;
                case 3:
                    y--;
                    break;
            }
            // If the new position is free (and valid) return it as a grid position.
            if (isFreePos(x, y)) { return gridPos(x, y); }
        }
    }

    /**
     * Determine whether the coordinates represent a valid free position
     * @param x X coordinate
     * @param y Y coordinate
     * @return Position is valid and empty
     */
    private boolean isFreePos(int x, int y) {
        return !(x < 0 || y < 0 || x >= mColumns || y >= mRows) && mGrid[gridPos(x, y)] == 0;
    }

    /**
     * Getter for the amount of columns in the grid
     * @return The amount of columns
     */
    public int getColumns() {
        return mColumns;
    }

    /**
     * Getter for the amount of rows in the grid
     * @return The amount of rows
     */
    public int getRows() {
        return mRows;
    }

    private void checkCoordinateBounds(int x, int y) {
        if (x < 0 || y < 0 || x >= mColumns || y >= mRows) {
            throw new IndexOutOfBoundsException("The coordinates (" + x + ", " + y + ") are not valid.");
        }
    }

    /**
     * Get the value for the particular grid element. This is for users of the class (usage of a 1dim array is an implementation detail)
     * @param x The X coordinate
     * @param y The Y coordinate
     * @return The value
     */
    public int getGridElem(int x, int y) {
        checkCoordinateBounds(x, y);
        return mGrid[gridPos(x, y)];
    }

    /**
     * Helper function to determine whether the position contains the server
     * @param x The X coordinate
     * @param y The Y coordinate
     * @return If the coordinate contains the server
     */
    public boolean isServer(int x, int y) {
        int gridpos = gridPos(x, y);
        return (mGrid[gridpos] & SERVER) != 0;
    }

    /**
     * Helper function to determine whether the position contains a terminal node
     * @param x The X coordinate
     * @param y The Y coordinate
     * @return If the coordinate is a terminal node
     */
    public boolean isNode(int x, int y) {
        int gridpos = gridPos(x, y);
        return (mGrid[gridpos] & NODE) != 0;
    }

    /**
     * Function to determine if all game elements have been connected
     * @return boolean result of operation
     */
    public boolean isGridSolved() {
        for (int col = 0; col < mColumns; col++) {
            for (int row = 0; row < mRows; row++) {
                if (! isConnected(col, row)) {
                    return false;
                }
            }
        }

        return true;
    }

    /**
     * Function to determine if a specific element node is connected
     * @param x The X Coordinate
     * @param y The Y Coordinate
     * @return boolean result of operation
     */
    public boolean isConnected(int x, int y) {
        int pos = gridPos(x, y);
        return (mGrid[pos] & CONNECTED) == CONNECTED;
    }

    /**
     * Sets the connected bit value of a grid element
     * @param x
     * @param y
     */
    private void setGridElemConnected(int x, int y) {
        checkCoordinateBounds(x, y);
        mGrid[gridPos(x, y)] |= CONNECTED;
    }

    /**
     * Clears the connected bit value of a grid element
     * @param x
     * @param y
     */
    private void unsetGridElemConnected(int x, int y) {
        checkCoordinateBounds(x, y);

        if (! isServer(x, y)) {
            mGrid[gridPos(x, y)] &= CONNECTED_MASK;
        }
    }

    /**
     * Removes the connected bit value for every grid element
     */
    private void clearConnectedness() {
        for (int col = 0; col < mColumns; col++) {
            for (int row = 0; row < mRows; row++) {
                unsetGridElemConnected(col, row);
            }
        }
    }

    /**
     * A function to check for connectedness of grid elements
     */
    private void checkConnectedness() {
        //Only runs after it the generate() function has been called for improve efficiency
        if (mGridInitialized) {
            mTraversedGridLocations = new boolean[mGrid.length];

            int serverCol = x(mServerPos);
            int serverRow = y(mServerPos);

            clearConnectedness();
            traverseGrid(serverCol, serverRow);
        }
    }

    /**
     * A recursive operation that checks for connectedness on each grid element's neighbour
     * @param col
     * @param row
     */
    private void traverseGrid(int col, int row) {
        int gridPos = gridPos(col, row);

        //This is to avoid it checking the same node twice as otherwise the recursive operation will eventually fall into an infinite loop
        if (mTraversedGridLocations[gridPos]) {
            return;
        } else {
            mTraversedGridLocations[gridPos] = true;
        }

        int currentElement = getGridElem(col, row) & CONNECTORMASK;
        int rowUp = row - 1, rowDown = row + 1;
        int colLeft = col - 1, colRight = col + 1;

        /**
         * Each if statement checks if they can go in their chosen direction.
         * If it can, it will check if the neighbouring element is facing in the direction that would mean it's connected
         * If that is true, it will set that element as connected and the recursive operation is called on the connected element
         */

        //Can go up
        if (rowUp >=  0) {
            int upElement = getGridElem(col, rowUp) & CONNECTORMASK;
            if ((currentElement & UP) == UP && (upElement & DOWN) == DOWN) {
                setGridElemConnected(col, rowUp);
                traverseGrid(col, rowUp);
            }
        }

        //Can go down
        if (rowDown <= mRows - 1) {
            int downElement = getGridElem(col, rowDown) & CONNECTORMASK;
            if ((currentElement & DOWN) == DOWN && (downElement & UP) == UP) {
                setGridElemConnected(col, rowDown);
                traverseGrid(col, rowDown);
            }
        }

        //Can go left
        if (colLeft >= 0) {
            int leftElement = getGridElem(colLeft, row) & CONNECTORMASK;
            if ((currentElement & LEFT) == LEFT && (leftElement & RIGHT) == RIGHT) {
                setGridElemConnected(colLeft, row);
                traverseGrid(colLeft, row);
            }
        }

        //Can go right
        if (colRight <= mColumns - 1) {
            int rightElement = getGridElem(colRight, row) & CONNECTORMASK;
            if ((currentElement & RIGHT) == RIGHT && (rightElement & LEFT) == LEFT) {
                setGridElemConnected(colRight, row);
                traverseGrid(colRight, row);
            }
        }
    }
}