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
import java.util.Map;
import java.util.Comparator;

public class RestockListScreen extends Screen {

    private static final int ROW_HEIGHT = 20;
    private static final int VISIBLE_ROWS = 10;

    private static final int COL_QTY = 40;
    private static final int COL_ITEM = 160;
    private static final int COL_COORDS = 100;
    private static final int COL_X = 20;

    private int scrollOffset = 0;
    private int maxScroll = 0;

    private final List<ButtonWidget> deleteButtons = new ArrayList<>();
    private QuickShopEntry hoveredEntry = null;

    public RestockListScreen() {
        super(Text.literal("QuickStock – Restock List"));
    }

    @Override
    protected void init() {
        super.init();
        rebuildButtons();
    }

    private void rebuildButtons() {
        clearChildren();
        deleteButtons.clear();

        // ---------- Clear All button (top-right) ----------
        addDrawableChild(
                ButtonWidget.builder(
                        Text.literal("Clear All"),
                        btn -> {
                            QuickStockData.clear();
                            rebuildButtons();
                        }
                ).dimensions(width - 120, 10, 110, 20).build()
        );

        int totalRows = QuickStockData.restockList.size();
        maxScroll = Math.max(0, totalRows - VISIBLE_ROWS);
        scrollOffset = MathHelper.clamp(scrollOffset, 0, maxScroll);

        int leftX = (width - getTableWidth()) / 2;
        int startY = 40;

        int row = 0;
        List<Map.Entry<BlockPos, QuickShopEntry>> sortedEntries =
        QuickStockData.restockList.entrySet().stream()
                .sorted(Comparator.comparingInt(e -> e.getValue().quantity))
                .toList();

            for (Map.Entry<BlockPos, QuickShopEntry> mapEntry : sortedEntries) {


            if (row < scrollOffset) {
                row++;
                continue;
            }
            if (row >= scrollOffset + VISIBLE_ROWS) break;

            BlockPos pos = mapEntry.getKey();
            int visualRow = row - scrollOffset;
            int y = startY + visualRow * ROW_HEIGHT;

            ButtonWidget delete = ButtonWidget.builder(
                Text.literal("X").formatted(Formatting.RED),
                btn -> {
                    // 🔥 REMOVE HIGHLIGHT FIRST
                    QuickStockData.removeHighlightedPosition(pos);

                    // then remove entry
                    QuickStockData.restockList.remove(pos);

                    rebuildButtons();
                }
            ).dimensions(
                    leftX + COL_QTY + COL_ITEM + COL_COORDS + 5,
                    y,
                    COL_X,
                    ROW_HEIGHT - 2
            ).build();

            addDrawableChild(delete);
            deleteButtons.add(delete);

            row++;
        }
    }

    private int getTableWidth() {
        return COL_QTY + COL_ITEM + COL_COORDS + COL_X;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontal, double vertical) {
        if (maxScroll > 0) {
            scrollOffset = MathHelper.clamp(
                    scrollOffset - (int) Math.signum(vertical),
                    0,
                    maxScroll
            );
            rebuildButtons();
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, horizontal, vertical);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {

        hoveredEntry = null;

        // ✅ build ONCE per frame, deterministic order
        List<QuickShopEntry> visibleEntries = QuickStockData.restockList.entrySet().stream()
                .sorted(Comparator.comparingInt(e -> e.getValue().quantity))
                .map(Map.Entry::getValue)
                .toList();

        int leftX = (width - getTableWidth()) / 2;
        int headerY = 25;
        int startY = 40;

        context.drawTextWithShadow(
                textRenderer,
                title,
                width / 2 - textRenderer.getWidth(title) / 2,
                10,
                0xFFFFFFFF
        );

        context.drawText(textRenderer, Text.literal("Qty"), leftX + 5, headerY, 0xFFAAAAAA, false);
        context.drawText(textRenderer, Text.literal("Item"), leftX + COL_QTY + 5, headerY, 0xFFAAAAAA, false);
        context.drawText(textRenderer, Text.literal("Coords"), leftX + COL_QTY + COL_ITEM + 5, headerY, 0xFFAAAAAA, false);

        int row = 0;
        for (QuickShopEntry entry : visibleEntries) {

            if (row < scrollOffset) {
                row++;
                continue;
            }
            if (row >= scrollOffset + VISIBLE_ROWS) break;

            boolean isHighlighted =
                    QuickStockData.getHighlightedPositions().contains(entry.signPos);

            int visualRow = row - scrollOffset;
            int y = startY + visualRow * ROW_HEIGHT;

            int rowLeft = leftX;
            int rowRight = leftX + COL_QTY + COL_ITEM + COL_COORDS;
            int rowTop = y;
            int rowBottom = y + ROW_HEIGHT;

            if (isHighlighted) {
                context.fill(rowLeft, rowTop, rowRight, rowBottom, 0x3322FF22);
                context.drawBorder(rowLeft, rowTop, rowRight - rowLeft, rowBottom - rowTop, 0xFF22FF22);
            }

            boolean hovered =
                    mouseX >= rowLeft && mouseX <= rowRight &&
                    mouseY >= rowTop && mouseY <= rowBottom;

            if (hovered) {
                hoveredEntry = entry;
                context.fill(rowLeft, rowTop, rowRight, rowBottom, 0x88222222);
                context.drawBorder(rowLeft, rowTop, rowRight - rowLeft, rowBottom - rowTop, 0xFFAAAAAA);
            }

            String qty = String.valueOf(entry.quantity);
            context.drawTextWithShadow(
                    textRenderer,
                    qty,
                    leftX + COL_QTY - textRenderer.getWidth(qty) - 5,
                    y + 6,
                    0xFFFFFFFF
            );

            context.drawTextWithShadow(
                    textRenderer,
                    entry.itemName,
                    leftX + COL_QTY + 5,
                    y + 6,
                    0xFFFFFFFF
            );

            BlockPos p = entry.signPos;
            context.drawTextWithShadow(
                    textRenderer,
                    p.getX() + " " + p.getY() + " " + p.getZ(),
                    leftX + COL_QTY + COL_ITEM + 5,
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

    private void drawCenteredTooltip(DrawContext context, QuickShopEntry entry, int mouseX, int mouseY) {

        int tooltipWidth = 160;
        int lineHeight = 10;
        int padding = 6;

        List<Text> lines = new ArrayList<>();
        lines.add(Text.literal(entry.seller).formatted(Formatting.YELLOW));

        if (entry.quantity <= 0) {
            lines.add(Text.literal("Out of Stock").formatted(Formatting.RED));
        } else {
            lines.add(Text.literal("Selling " + entry.quantity).formatted(Formatting.GREEN));
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

        int totalRows = QuickStockData.restockList.size();
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

    @Override
        public boolean shouldPause() {
            return false;
        }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        for (ButtonWidget delete : deleteButtons) {
            if (delete.isMouseOver(mouseX, mouseY)) {
                return super.mouseClicked(mouseX, mouseY, button);
            }
        }

        // Left click only
        if (button != 0) {
            return super.mouseClicked(mouseX, mouseY, button);
        }

        int leftX = (width - getTableWidth()) / 2;
        int startY = 40;

        int row = 0;

        // IMPORTANT: use the same iteration order as render()
        List<QuickShopEntry> entries =
                new ArrayList<>(QuickStockData.restockList.values());

        entries.sort((a, b) -> Integer.compare(a.quantity, b.quantity));

        for (QuickShopEntry entry : entries) {

            if (row < scrollOffset) {
                row++;
                continue;
            }
            if (row >= scrollOffset + VISIBLE_ROWS) break;

            int visualRow = row - scrollOffset;
            int y = startY + visualRow * ROW_HEIGHT;

            int rowLeft = leftX;
            int rowRight = leftX + getTableWidth() - COL_X;
            int rowTop = y;
            int rowBottom = y + ROW_HEIGHT;

            boolean insideRow =
                    mouseX >= rowLeft && mouseX <= rowRight &&
                    mouseY >= rowTop && mouseY <= rowBottom;

            if (insideRow) {
                // Toggle highlight for this shop
                QuickStockData.toggleHighlight(entry.signPos);
                return true;
            }

            row++;
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

}
