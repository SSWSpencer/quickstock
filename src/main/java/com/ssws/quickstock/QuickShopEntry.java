package com.ssws.quickstock;

import net.minecraft.util.math.BlockPos;

public class QuickShopEntry {

	public final String seller;
	public final String itemName;
	public final int quantity;
	public final String priceRaw;
	public final BlockPos signPos;

	public QuickShopEntry(
			String seller,
			String itemName,
			int quantity,
			String priceRaw,
			BlockPos signPos
	) {
		this.seller = seller;
		this.itemName = itemName;
		this.quantity = quantity;
		this.priceRaw = priceRaw;
		this.signPos = signPos;
	}
}
