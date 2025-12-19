package com.ssws.quickstock;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import com.ssws.quickstock.RestockHighlightRenderer;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;


public class QuickStockClient implements ClientModInitializer {

	public static KeyBinding scannerKeybind;
	public static KeyBinding guiKeybind;
	public static KeyBinding snapshotKeybind;

	@Override
	public void onInitializeClient() {

		// 🔹 Load persisted config FIRST
		QuickStockConfig.load();

		scannerKeybind = KeyBindingHelper.registerKeyBinding(
				new KeyBinding(
						"key.quickstock.scan",
						InputUtil.Type.KEYSYM,
						QuickStockConfig.scannerKey,
						"category.quickstock"
				)
		);

		guiKeybind = KeyBindingHelper.registerKeyBinding(
				new KeyBinding(
						"key.quickstock.gui",
						InputUtil.Type.KEYSYM,
						QuickStockConfig.guiKey,
						"category.quickstock"
				)
		);

		snapshotKeybind = KeyBindingHelper.registerKeyBinding(
				new KeyBinding(
						"key.quickstock.snapshot",
						InputUtil.Type.KEYSYM,
						QuickStockConfig.snapshotKey,
						"category.quickstock"
				)
		);

		QuickStockKeyHandler.register();
		HudRenderCallback.EVENT.register(new ScannerHudOverlay());
		RestockHighlightRenderer.register();
	}

}
