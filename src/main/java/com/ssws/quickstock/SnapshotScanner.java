package com.ssws.quickstock;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

public class SnapshotScanner {

    public static List<SnapshotShopEntry> scan(BlockPos center, int radius) {

        List<SnapshotShopEntry> results = new ArrayList<>();

        MinecraftClient client = MinecraftClient.getInstance();
        World world = client.world;
        if (world == null) return results;

        BlockPos.Mutable pos = new BlockPos.Mutable();

        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {

                    pos.set(
                            center.getX() + x,
                            center.getY() + y,
                            center.getZ() + z
                    );

                    BlockEntity be = world.getBlockEntity(pos);
                    if (!(be instanceof SignBlockEntity sign)) continue;

                    SnapshotShopEntry entry = parseSign(sign, pos.toImmutable());
                    if (entry != null) {
                        results.add(entry);
                    }
                }
            }
        }

        return results;
    }

    private static SnapshotShopEntry parseSign(SignBlockEntity sign, BlockPos pos) {

        Text[] lines = sign.getFrontText().getMessages(false);

        if (isAllEmpty(lines)) {
            lines = sign.getBackText().getMessages(false);
        }

        if (lines.length < 4) return null;

        String owner = clean(lines[0]);
        String typeLine = clean(lines[1]).toLowerCase();
        String item = clean(lines[2]);
        String price = clean(lines[3]);

        if (owner.isEmpty() || item.isEmpty()) return null;

        SnapshotShopEntry.Type type;
        int qty;

        if (typeLine.startsWith("buying")) {
            type = SnapshotShopEntry.Type.BUY;
            qty = parseQty(typeLine, "buying");
        } else if (typeLine.startsWith("selling")) {
            type = SnapshotShopEntry.Type.SELL;
            qty = parseQty(typeLine, "selling");
        } else {
            return null;
        }

        return new SnapshotShopEntry(
                owner,
                type,
                qty,
                item,
                price,
                pos
        );
    }

    private static int parseQty(String line, String keyword) {
        try {
            return Integer.parseInt(
                    line.replace(keyword, "")
                        .replace(":", "")
                        .trim()
            );
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private static boolean isAllEmpty(Text[] lines) {
        for (Text t : lines) {
            if (!t.getString().trim().isEmpty()) {
                return false;
            }
        }
        return true;
    }

    private static String clean(Text text) {
        return text.getString()
                .replaceAll("§.", "")
                .trim();
    }
}
