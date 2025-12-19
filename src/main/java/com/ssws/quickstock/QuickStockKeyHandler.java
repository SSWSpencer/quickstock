package com.ssws.quickstock;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

public class QuickStockKeyHandler {

	public static boolean scannerEnabled = false;
	private static int tickCounter = 0;
	private static final int SCAN_INTERVAL_TICKS = 20; // 20 ticks = 1 second

	public static boolean isScannerEnabled() {
		return scannerEnabled;
	}


	public static void register() {
		ClientTickEvents.END_CLIENT_TICK.register(client -> {

			if (client.player == null) return;

			/* ---------- Toggle scanner ---------- */
			if (QuickStockClient.scannerKeybind.wasPressed()) {
				scannerEnabled = !scannerEnabled;

				client.player.sendMessage(
						Text.literal(
								"QuickStock: Scanner " +
								(scannerEnabled ? "ENABLED" : "DISABLED")
						),
						true
				);
			}

			/* ---------- Scanner loop ---------- */
			if (scannerEnabled && client.currentScreen == null) {
				tickCounter++;

				if (tickCounter >= SCAN_INTERVAL_TICKS) {
					tickCounter = 0;

					BlockPos pos = client.player.getBlockPos();
					var results = QuickShopScanner.scanArea(
							pos,
							QuickStockConfig.scanRadius
					);

					for (QuickShopEntry entry : results) {
						if (entry.quantity <= QuickStockConfig.restockThreshold) {

							boolean wasNew = !QuickStockData.restockList.containsKey(entry.signPos);

							QuickStockData.restockList.putIfAbsent(
									entry.signPos,
									entry
							);

							if (wasNew) {
								ScannerHudOverlay.triggerFlash();
							}
						}
					}
				}
			}

			/* ---------- GUI key ---------- */
			if (QuickStockClient.guiKeybind.wasPressed()) {
				MinecraftClient.getInstance().setScreen(
						new RestockListScreen()
				);
			}

			/* ---------- Snapshot key ---------- */
			if (QuickStockClient.snapshotKeybind.wasPressed()) {

				if (scannerEnabled) {
					client.player.sendMessage(
							Text.literal("You can't do this while Scanner Mode is active."),
							true
					);
					return;
				}

				SnapshotData.clear();

				BlockPos pos = client.player.getBlockPos();
				SnapshotData.entries.addAll(
						SnapshotScanner.scan(pos, QuickStockConfig.scanRadius)
				);

				MinecraftClient.getInstance().setScreen(
						new SnapshotListScreen()
				);
			}

		});
	}
}
