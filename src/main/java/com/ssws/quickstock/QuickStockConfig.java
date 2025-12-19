package com.ssws.quickstock;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import org.lwjgl.glfw.GLFW;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class QuickStockConfig {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH =
            FabricLoader.getInstance().getConfigDir().resolve("quickstock.json");

    /* ---------- Defaults ---------- */

    public static int scanRadius = 10;
    public static int restockThreshold = 5;

    public static int scannerKey = GLFW.GLFW_KEY_K;
    public static int guiKey = GLFW.GLFW_KEY_J;
    public static int snapshotKey = GLFW.GLFW_KEY_L;

    /* ---------- Load / Save ---------- */

    public static void load() {

        if (!Files.exists(CONFIG_PATH)) {
            save(); // create default config
            return;
        }

        try {
            String json = Files.readString(CONFIG_PATH);
            Data data = GSON.fromJson(json, Data.class);

            if (data != null) {
                scanRadius = data.scanRadius;
                restockThreshold = data.restockThreshold;
            }

        } catch (IOException e) {
            System.err.println("[QuickStock] Failed to load config");
            e.printStackTrace();
        }
    }

    public static void save() {
        try {
            Data data = new Data();
            data.scanRadius = scanRadius;
            data.restockThreshold = restockThreshold;

            Files.writeString(CONFIG_PATH, GSON.toJson(data));

        } catch (IOException e) {
            System.err.println("[QuickStock] Failed to save config");
            e.printStackTrace();
        }
    }

    /* ---------- Internal DTO ---------- */

    private static class Data {
        int scanRadius;
        int restockThreshold;
    }
}
