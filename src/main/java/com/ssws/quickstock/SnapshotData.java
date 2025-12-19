package com.ssws.quickstock;

import java.util.ArrayList;
import java.util.List;

public class SnapshotData {

    public static final List<SnapshotShopEntry> entries = new ArrayList<>();

    public static void clear() {
        entries.clear();
    }
}
