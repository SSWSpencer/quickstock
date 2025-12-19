package com.ssws.quickstock;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

public class QuickShopScanner {

	public static List<QuickShopEntry> scanArea(BlockPos center, int radius) {

		List<QuickShopEntry> results = new ArrayList<>();

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

					QuickShopEntry entry = parseQuickShopSign(sign, pos.toImmutable());
					if (entry != null) {
						results.add(entry);
					}
				}
			}
		}

		return results;
	}

	private static QuickShopEntry parseQuickShopSign(SignBlockEntity sign, BlockPos pos) {

		// Try FRONT text first
		Text[] lines = sign.getFrontText().getMessages(false);

		// If front is empty, try BACK
		if (isAllEmpty(lines)) {
			lines = sign.getBackText().getMessages(false);
		}

		if (lines.length < 4) return null;

		String seller = clean(lines[0]);
		String stockLine = clean(lines[1]).toLowerCase();
		String itemName = clean(lines[2]);
		String priceRaw = clean(lines[3]);

		// Hard reject if this doesn't look like a shop
		if (seller.isEmpty() || itemName.isEmpty()) return null;

		int quantity;

		if (stockLine.equals("out of stock")) {
			quantity = 0;
		} else if (stockLine.startsWith("selling")) {
			// supports "selling 64" or "selling: 64"
			String num = stockLine.replace("selling", "")
					.replace(":", "")
					.trim();
			try {
				quantity = Integer.parseInt(num);
			} catch (NumberFormatException e) {
				return null;
			}
		} else {
			return null;
		}

		return new QuickShopEntry(
				seller,
				itemName,
				quantity,
				priceRaw,
				pos
		);
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
				.replaceAll("§.", "") // strip formatting
				.trim();
	}
}
