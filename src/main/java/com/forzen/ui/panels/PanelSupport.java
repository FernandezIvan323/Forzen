package com.forzen.ui.panels;

import com.forzen.ui.theme.AppTheme;

import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

/**
 * Shared Soft Dark layout helpers for settings panels.
 */
public final class PanelSupport {

    private PanelSupport() {}

    public static VBox page(AppTheme theme, String title, String subtitle, Node... children) {
        VBox root = new VBox(18);
        root.setPadding(new Insets(8, 8, 24, 8));
        root.setStyle("-fx-background-color: transparent;");

        Label t = new Label(title);
        t.setStyle(theme.titleStyle());
        VBox head = new VBox(4, t);
        if (subtitle != null && !subtitle.isBlank()) {
            Label s = new Label(subtitle);
            s.setStyle(theme.subtitleStyle());
            s.setWrapText(true);
            head.getChildren().add(s);
        }
        root.getChildren().add(head);
        for (Node n : children) {
            if (n != null) root.getChildren().add(n);
        }
        return root;
    }

    public static VBox card(AppTheme theme, String sectionTitle, Node... body) {
        VBox card = new VBox(12);
        card.setStyle(theme.cardStyle());
        if (sectionTitle != null && !sectionTitle.isBlank()) {
            Label lbl = new Label(sectionTitle.toUpperCase());
            lbl.setStyle(theme.sectionLabelStyle());
            card.getChildren().add(lbl);
        }
        for (Node n : body) {
            if (n != null) card.getChildren().add(n);
        }
        return card;
    }

    public static HBox row(Node left, Node right) {
        HBox row = new HBox(12);
        row.setFillHeight(true);
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        if (left != null) row.getChildren().add(left);
        row.getChildren().add(spacer);
        if (right != null) row.getChildren().add(right);
        return row;
    }

    public static Label hint(AppTheme theme, String text) {
        Label h = new Label(text);
        h.setStyle(theme.mutedStyle());
        h.setWrapText(true);
        return h;
    }
}
