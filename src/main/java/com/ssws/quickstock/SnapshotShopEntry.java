package com.ssws.quickstock;

import net.minecraft.util.math.BlockPos;

public class SnapshotShopEntry {

    public enum Type {
        BUY,
        SELL
    }

    public final String owner;
    public final Type type;
    public final int quantity;
    public final String itemName;
    public final String priceRaw;
    public final BlockPos pos;

    public SnapshotShopEntry(
            String owner,
            Type type,
            int quantity,
            String itemName,
            String priceRaw,
            BlockPos pos
    ) {
        this.owner = owner;
        this.type = type;
        this.quantity = quantity;
        this.itemName = itemName;
        this.priceRaw = priceRaw;
        this.pos = pos;
    }
}
