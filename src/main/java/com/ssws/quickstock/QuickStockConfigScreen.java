package com.ssws.quickstock;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

public class QuickStockConfigScreen extends Screen {

    private final Screen parent;

    private RadiusSlider radiusSlider;
    private TextFieldWidget radiusField;

    private ThresholdSlider thresholdSlider;
    private TextFieldWidget thresholdField;

    private static final int MIN_RADIUS = 1;
    private static final int MAX_RADIUS = 128;

    private static final int MIN_THRESHOLD = 0;
    private static final int MAX_THRESHOLD = 1728;

    private boolean suppressCallbacks = false;

    protected QuickStockConfigScreen(Screen parent) {
        super(Text.literal("QuickStock Config"));
        this.parent = parent;
    }

    @Override
    protected void init() {

        int centerX = this.width / 2;
        int startY = this.height / 2 - 40;

        /* ---------- Scan Radius ---------- */

        radiusField = new TextFieldWidget(
                this.textRenderer,
                centerX + 110,
                startY,
                50,
                20,
                Text.literal("Radius")
        );

        radiusField.setText(String.valueOf(QuickStockConfig.scanRadius));
        radiusField.setChangedListener(this::onRadiusTyped);
        this.addDrawableChild(radiusField);

        radiusSlider = new RadiusSlider(
                centerX - 100,
                startY,
                200,
                20,
                radiusToSlider(QuickStockConfig.scanRadius)
        );

        radiusSlider.setTooltip(Tooltip.of(Text.literal(
                "The distance from the player that will be scanned for shops"
        )));

        this.addDrawableChild(radiusSlider);

        /* ---------- Restock Threshold ---------- */

        thresholdField = new TextFieldWidget(
                this.textRenderer,
                centerX + 110,
                startY + 30,
                50,
                20,
                Text.literal("Threshold")
        );

        thresholdField.setText(String.valueOf(QuickStockConfig.restockThreshold));
        thresholdField.setChangedListener(this::onThresholdTyped);
        this.addDrawableChild(thresholdField);

        thresholdSlider = new ThresholdSlider(
                centerX - 100,
                startY + 30,
                200,
                20,
                thresholdToSlider(QuickStockConfig.restockThreshold)
        );

        thresholdSlider.setTooltip(Tooltip.of(Text.literal(
                "Minimum stock before restock entry is added"
        )));

        this.addDrawableChild(thresholdSlider);

        /* ---------- Done ---------- */

        this.addDrawableChild(ButtonWidget.builder(
                Text.literal("Done"),
                btn -> this.client.setScreen(parent)
        ).dimensions(centerX - 50, this.height - 40, 100, 20).build());
    }

    /* ---------- Text Input ---------- */

    private void onRadiusTyped(String text) {
        if (suppressCallbacks) return;

        try {
            int value = clamp(Integer.parseInt(text), MIN_RADIUS, MAX_RADIUS);
            QuickStockConfig.scanRadius = value;
            QuickStockConfig.save();

            suppressCallbacks = true;
            radiusSlider.setNormalized(radiusToSlider(value));
            suppressCallbacks = false;

        } catch (NumberFormatException ignored) {}
    }

    private void onThresholdTyped(String text) {
        if (suppressCallbacks) return;

        try {
            int value = clamp(Integer.parseInt(text), MIN_THRESHOLD, MAX_THRESHOLD);
            QuickStockConfig.restockThreshold = value;
            QuickStockConfig.save();

            suppressCallbacks = true;
            thresholdSlider.setNormalized(thresholdToSlider(value));
            suppressCallbacks = false;

        } catch (NumberFormatException ignored) {}
    }

    /* ---------- Slider Mapping ---------- */

    private double radiusToSlider(int radius) {
        return (radius - MIN_RADIUS) / (double) (MAX_RADIUS - MIN_RADIUS);
    }

    private double thresholdToSlider(int threshold) {
        return threshold / (double) MAX_THRESHOLD;
    }

    private int clamp(int v, int min, int max) {
        return Math.max(min, Math.min(max, v));
    }

    /* ---------- Sliders ---------- */

    private class RadiusSlider extends SliderWidget {

        protected RadiusSlider(int x, int y, int width, int height, double value) {
            super(x, y, width, height, Text.empty(), value);
            updateMessage();
        }

        @Override
        protected void updateMessage() {
            setMessage(Text.literal("Scan Radius: " + QuickStockConfig.scanRadius));
        }

        @Override
        protected void applyValue() {
            QuickStockConfig.scanRadius =
                    MIN_RADIUS + (int) Math.round(this.value * (MAX_RADIUS - MIN_RADIUS));

            QuickStockConfig.save();

            if (!suppressCallbacks) {
                suppressCallbacks = true;
                radiusField.setText(String.valueOf(QuickStockConfig.scanRadius));
                suppressCallbacks = false;
            }
        }

        public void setNormalized(double value) {
            this.value = value;
            applyValue();
            updateMessage();
        }
    }

    private class ThresholdSlider extends SliderWidget {

        protected ThresholdSlider(int x, int y, int width, int height, double value) {
            super(x, y, width, height, Text.empty(), value);
            updateMessage();
        }

        @Override
        protected void updateMessage() {
            setMessage(Text.literal("Restock Threshold: " + QuickStockConfig.restockThreshold));
        }

        @Override
        protected void applyValue() {
            QuickStockConfig.restockThreshold =
                    (int) Math.round(this.value * MAX_THRESHOLD);

            QuickStockConfig.save();

            if (!suppressCallbacks) {
                suppressCallbacks = true;
                thresholdField.setText(String.valueOf(QuickStockConfig.restockThreshold));
                suppressCallbacks = false;
            }
        }

        public void setNormalized(double value) {
            this.value = value;
            applyValue();
            updateMessage();
        }
    }

    @Override
    public void close() {
        this.client.setScreen(parent);
    }
}
