package com.github.mayconr.juoserver.game.gump;

import java.util.ArrayList;
import java.util.List;

public class DeclarativeGumpUI {

    private final List<Page> pages = new ArrayList<>();

    public DeclarativeGumpUI(Page... pages) {
        this.pages.addAll(List.of(pages));
    }

    public DeclarativeGumpUI addPage(Page page) {
        this.pages.add(page);
        return this;
    }

    public void render(GumpBuilder g) {
        for (Page page : pages) {
            page.layout(new LayoutContext(0, 0, 0, 0));
            page.render(g);
        }
    }

    /* =========================
     * DSL helpers
     * ========================= */

    public static Page Page(int index, UIElement... children) {
        Page p = new Page(index);
        for (UIElement e : children) p.add(e);
        return p;
    }

    public static Column Column(int spacing, UIElement... children) {
        Column c = new Column(spacing);
        for (UIElement e : children) c.add(e);
        return c;
    }

    public static Label Label(String text) {
        return new Label(text);
    }

    public static Button Button(int normalId, int pressedId, int buttonId) {
        return new Button(normalId, pressedId, buttonId);
    }

    public static Button Button(int normalId, int pressedId, int buttonId, String caption) {
        return new Button(normalId, pressedId, buttonId, caption);
    }

    public static PageButton PageButton(int normalId, int pressedId, int targetPage) {
        return new PageButton(normalId, pressedId, targetPage);
    }

    public static Image Image(int gumpPicId, int width, int height) {
        return new Image(gumpPicId, width, height);
    }

    public static Panel Panel(int width, int height, UIElement content) {
        return new Panel(width, height, content);
    }

    public static Panel Panel(int gumPicId, int width, int height, UIElement content) {
        return new Panel(gumPicId, width, height, false, content);
    }

    public static Panel Panel(
            int gumPicId, int width, int height, boolean resizable, UIElement content) {
        return new Panel(gumPicId, width, height, resizable, content);
    }

    public static Row Row(int gap, UIElement... children) {
        return new Row(gap, List.of(children));
    }

    public static FormField Field(UIElement label, UIElement field) {
        return FormField.stacked(label, field);
    }

    public static FormField InlineField(UIElement label, UIElement field) {
        return FormField.inline(label, field);
    }

    public static TextField TextField(int entryId, int width) {
        return new TextField(entryId, width);
    }

    public static TextField TextField(int entryId, int width, int height) {
        return new TextField(entryId, width, height);
    }

    public static Radio Radio(int uncheckedId, int checkedId, int switchId) {
        return new Radio(uncheckedId, checkedId, switchId);
    }

    public static Radio Radio(int uncheckedId, int checkedId, int switchId, boolean checked) {
        return new Radio(uncheckedId, checkedId, switchId, checked);
    }

    public static ItemIcon ItemIcon(int tileId) {
        return new ItemIcon(tileId);
    }

    public static Checkbox Checkbox(int uncheckedId, int checkedId, int switchId) {
        return new Checkbox(uncheckedId, checkedId, switchId, false);
    }

    public static Checkbox Checkbox(int uncheckedId, int checkedId, int switchId, boolean checked) {
        return new Checkbox(uncheckedId, checkedId, switchId, checked);
    }

    public static TextArea TextArea(int entryId, int width, int height) {
        return new TextArea(entryId, width, height);
    }

    public static ItemSlot ItemSlot(int size, UIElement content) {
        return new ItemSlot(size, content);
    }

    public static HtmlGump HtmlGump(String value, int width, int height) {
        return new HtmlGump(value, width, height);
    }

    public static HtmlGump HtmlGump(
            String value, int width, int height, boolean background, boolean scrollbar) {
        return new HtmlGump(value, width, height, background, scrollbar);
    }
}
