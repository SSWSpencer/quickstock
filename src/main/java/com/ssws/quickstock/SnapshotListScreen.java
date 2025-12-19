package com.ssws.quickstock;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;

import java.util.ArrayList;
import java.util.List;

public class SnapshotListScreen extends Screen {

    private static final int ROW_HEIGHT = 20;
    private static final int VISIBLE_ROWS = 10;

    private static final int COL_TYPE = 20;
    private static final int COL_QTY = 40;
    private static final int COL_ITEM = 160;
    private static final int COL_COORDS = 100;

    private int scrollOffset = 0;
    private int maxScroll = 0;

    private Filter filter = Filter.ALL;
    private SnapshotShopEntry hoveredEntry = null;

    private enum Filter {
        ALL,
        BUY,
        SELL
    }

    public SnapshotListScreen() {
        super(Text.literal("QuickStock – Snapshot"));
    }

    @Override
    protected void init() {
        super.init();

        addDrawableChild(
                ButtonWidget.builder(getFilterText(), btn -> {
                    cycleFilter();
                    btn.setMessage(getFilterText());
                }).dimensions(width - 120, 10, 110, 20).build()
        );

        updateScroll();
    }

    private void cycleFilter() {
        filter = switch (filter) {
            case ALL -> Filter.BUY;
            case BUY -> Filter.SELL;
            case SELL -> Filter.ALL;
        };
        updateScroll();
    }

    private Text getFilterText() {
        return switch (filter) {
            case ALL -> Text.literal("Show: All");
            case BUY -> Text.literal("Show: Buying");
            case SELL -> Text.literal("Show: Selling");
        };
    }

    private List<SnapshotShopEntry> getFiltered() {
        return SnapshotData.entries.stream().filter(e -> {
            if (filter == Filter.BUY) return e.type == SnapshotShopEntry.Type.BUY;
            if (filter == Filter.SELL) return e.type == SnapshotShopEntry.Type.SELL;
            return true;
        }).toList();
    }

    private void updateScroll() {
        int total = getFiltered().size();
        maxScroll = Math.max(0, total - VISIBLE_ROWS);
        scrollOffset = MathHelper.clamp(scrollOffset, 0, maxScroll);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double h, double v) {
        if (maxScroll > 0) {
            scrollOffset = MathHelper.clamp(
                    scrollOffset - (int) Math.signum(v),
                    0,
                    maxScroll
            );
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, h, v);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {

        hoveredEntry = null;

        int leftX = (width - getTableWidth()) / 2;
        int headerY = 35;
        int startY = 50;

        context.drawTextWithShadow(
                textRenderer,
                title,
                width / 2 - textRenderer.getWidth(title) / 2,
                15,
                0xFFFFFFFF
        );

        context.drawText(textRenderer, Text.literal(""), leftX, headerY, 0xFFAAAAAA, false);
        context.drawText(textRenderer, Text.literal("Qty"), leftX + COL_TYPE + 5, headerY, 0xFFAAAAAA, false);
        context.drawText(textRenderer, Text.literal("Item"), leftX + COL_TYPE + COL_QTY + 5, headerY, 0xFFAAAAAA, false);
        context.drawText(textRenderer, Text.literal("Coords"), leftX + COL_TYPE + COL_QTY + COL_ITEM + 5, headerY, 0xFFAAAAAA, false);

        List<SnapshotShopEntry> list = getFiltered();

        int row = 0;
        for (int i = scrollOffset; i < list.size() && row < VISIBLE_ROWS; i++) {

            SnapshotShopEntry entry = list.get(i);
            int y = startY + row * ROW_HEIGHT;

            int rowLeft = leftX;
            int rowRight = leftX + getTableWidth();
            int rowTop = y;
            int rowBottom = y + ROW_HEIGHT;

            boolean hovered =
                    mouseX >= rowLeft && mouseX <= rowRight &&
                    mouseY >= rowTop && mouseY <= rowBottom;

            if (hovered) {
                hoveredEntry = entry;
                context.fill(rowLeft, rowTop, rowRight, rowBottom, 0x88222222);
                context.drawBorder(rowLeft, rowTop, rowRight - rowLeft, rowBottom - rowTop, 0xFFAAAAAA);
            }

            Text typeText = entry.type == SnapshotShopEntry.Type.BUY
                    ? Text.literal("B").formatted(Formatting.GREEN)
                    : Text.literal("S").formatted(Formatting.RED);

            context.drawTextWithShadow(textRenderer, typeText, leftX + 6, y + 6, 0xFFFFFFFF);

            String qty = String.valueOf(entry.quantity);
            context.drawTextWithShadow(
                    textRenderer,
                    qty,
                    leftX + COL_TYPE + COL_QTY - textRenderer.getWidth(qty) - 5,
                    y + 6,
                    0xFFFFFFFF
            );

            context.drawTextWithShadow(
                    textRenderer,
                    entry.itemName,
                    leftX + COL_TYPE + COL_QTY + 5,
                    y + 6,
                    0xFFFFFFFF
            );

            BlockPos p = entry.pos;
            String coords = p.getX() + " " + p.getY() + " " + p.getZ();
            context.drawTextWithShadow(
                    textRenderer,
                    coords,
                    leftX + COL_TYPE + COL_QTY + COL_ITEM + 5,
                    y + 6,
                    0xFFCCCCCC
            );

            row++;
        }

        drawScrollbar(context, leftX + getTableWidth() + 6, startY);

        super.render(context, mouseX, mouseY, delta);

        if (hoveredEntry != null) {
            drawCenteredTooltip(context, hoveredEntry, mouseX, mouseY);
        }
    }

    private void drawCenteredTooltip(DrawContext context, SnapshotShopEntry entry, int mouseX, int mouseY) {

        int tooltipWidth = 160;
        int lineHeight = 10;
        int padding = 6;

        List<Text> lines = new ArrayList<>();

        lines.add(Text.literal(entry.owner).formatted(Formatting.YELLOW));

        if (entry.type == SnapshotShopEntry.Type.BUY) {
            lines.add(Text.literal("Buying " + entry.quantity).formatted(Formatting.GREEN));
        } else {
            lines.add(Text.literal("Selling " + entry.quantity).formatted(Formatting.RED));
        }

        lines.add(Text.literal(entry.itemName).formatted(Formatting.WHITE));
        lines.add(Text.literal(entry.priceRaw).formatted(Formatting.GOLD));

        int tooltipHeight = lines.size() * lineHeight + padding * 2;
        int tx = mouseX + 12;
        int ty = mouseY - tooltipHeight / 2;

        context.fill(tx, ty, tx + tooltipWidth, ty + tooltipHeight, 0xF0101010);
        context.drawBorder(tx, ty, tooltipWidth, tooltipHeight, 0xFFAAAAAA);

        int textY = ty + padding;
        for (Text line : lines) {
            int textX = tx + (tooltipWidth - textRenderer.getWidth(line)) / 2;
            context.drawTextWithShadow(textRenderer, line, textX, textY, 0xFFFFFFFF);
            textY += lineHeight;
        }
    }

    private void drawScrollbar(DrawContext context, int x, int y) {

        int totalRows = getFiltered().size();
        if (totalRows <= VISIBLE_ROWS) return;

        int trackHeight = VISIBLE_ROWS * ROW_HEIGHT;
        int thumbHeight = Math.max(
                20,
                (int) ((float) VISIBLE_ROWS / totalRows * trackHeight)
        );

        float scrollPercent = (float) scrollOffset / maxScroll;
        int thumbY = y + (int) ((trackHeight - thumbHeight) * scrollPercent);

        context.fill(x, y, x + 4, y + trackHeight, 0xFF333333);
        context.fill(x, thumbY, x + 4, thumbY + thumbHeight, 0xFFAAAAAA);
    }

    private int getTableWidth() {
        return COL_TYPE + COL_QTY + COL_ITEM + COL_COORDS;
    }

    @Override
    public void close() {
        SnapshotData.clear();
        super.close();
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
