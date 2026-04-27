package com.portfoliodb.ui;

import javax.swing.AbstractButton;

public interface DataRefreshable {
    void refreshData();

    default void refreshData(AbstractButton sourceButton) {
        refreshData();
    }
}
