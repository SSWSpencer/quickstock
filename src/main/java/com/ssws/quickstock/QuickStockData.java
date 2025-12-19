package com.ssws.quickstock;

import net.minecraft.util.math.BlockPos;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class QuickStockData {

    public static final Map<BlockPos, QuickShopEntry> restockList = new HashMap<>();

    private static final Set<BlockPos> highlightedPositions = new HashSet<>();

    public static void toggleHighlight(BlockPos pos) {
        if (!highlightedPositions.add(pos)) {
            highlightedPositions.remove(pos);
        }
    }

    public static void clearHighlights() {
        highlightedPositions.clear();
    }

	public static void removeHighlightedPosition(BlockPos pos) {
		highlightedPositions.remove(pos);
	}
	

    public static Set<BlockPos> getHighlightedPositions() {
        return highlightedPositions;
    }

    public static void clear() {
        restockList.clear();
        clearHighlights();
    }
}
