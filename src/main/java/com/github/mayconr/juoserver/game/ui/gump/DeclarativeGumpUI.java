package com.github.mayconr.juoserver.game.ui.gump;

import com.github.mayconr.juoserver.game.model.event.message.MessageContent;
import java.util.function.Function;
import java.util.ArrayList;
import java.util.List;

public class DeclarativeGumpUI {

    private final List<Page> pages = new ArrayList<>();
    private boolean movable = true;
    private boolean closeWithRightClick = true;
    private boolean closeWithEsc = true;

    public DeclarativeGumpUI(Page... pages) {
        this.pages.addAll(List.of(pages));
    }

    public DeclarativeGumpUI addPage(Page page) {
        this.pages.add(page);
        return this;
    }

    /** Prevents dragging the entire gump window on the client. */
    public DeclarativeGumpUI noMove() {
        movable = false;
        return this;
    }

    /** Prevents closing the gump with the right mouse button. */
    public DeclarativeGumpUI noClose() {
        closeWithRightClick = false;
        return this;
    }

    /** Prevents closing the gump with the Esc key. */
    public DeclarativeGumpUI noDispose() {
        closeWithEsc = false;
        return this;
    }

    public void render(GumpBuilder g) {
        render(g, GumpText::fallback);
    }

    public synchronized void render(GumpBuilder g, Function<MessageContent, String> resolver) {
        for (Page page : pages) page.resolveTexts(resolver);
        if (!closeWithRightClick) {
            g.noClose();
        }
        if (!closeWithEsc) {
            g.noDispose();
        }
        if (!movable) {
            g.noMove();
        }
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

    public static PaginatedList PaginatedList(List<PaginatedList.Item> items, int width) {
        return new PaginatedList(items, width);
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

    public static ItemButton ItemButton(int normalId, int pressedId, int buttonId, int itemId) {
        return new ItemButton(normalId, pressedId, buttonId, itemId);
    }

    public static ItemButton ItemButton(int normalId, int pressedId, int buttonId, int itemId, String caption) {
        return new ItemButton(normalId, pressedId, buttonId, itemId, caption);
    }

    public static ItemButton ItemButton(int normalId, int pressedId, int buttonId, int itemId, MessageContent caption) {
        return new ItemButton(normalId, pressedId, buttonId, itemId, caption);
    }

    public static PageButton PageButton(int normalId, int pressedId, int targetPage) {
        return new PageButton(normalId, pressedId, targetPage);
    }

    public static VirtueImage VirtueImage(int graphicId) {
        return new VirtueImage(graphicId);
    }

    public static VirtueImage VirtueImage(int graphicId, int width, int height) {
        return new VirtueImage(graphicId, width, height);
    }

    public static Image Image(int gumpPicId, int width, int height) {
        return new Image(gumpPicId, width, height);
    }

    public static Separator Separator(int gumpPicId, int width, int height) {
        return new Separator(gumpPicId, width, height);
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

    public static TextField TextField(int entryId, int width, int height, int maxCharacters) {
        return new TextField(entryId, width, height, maxCharacters);
    }

    public static RadioGroup RadioGroup(int groupId, UIElement content) {
        return new RadioGroup(groupId, content);
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

    public static ItemIcon ItemIcon(int tileId, int width, int height) {
        return new ItemIcon(tileId, width, height);
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

    public static ItemSlot ItemSlot(int width, int height, UIElement content) {
        return new ItemSlot(width, height, content);
    }

    /** Creates an item slot with a custom resizepic background; other overloads default to 3000. */
    public static ItemSlot ItemSlot(int backgroundId, int width, int height, UIElement content) {
        return new ItemSlot(backgroundId, width, height, content);
    }

    public static HtmlGump HtmlGump(int clilocId, int width, int height) {
        return new HtmlGump(clilocId, width, height);
    }

    public static HtmlGump HtmlGump(int clilocId, int width, int height, boolean background, boolean scrollbar) {
        return new HtmlGump(clilocId, width, height, background, scrollbar);
    }

    public static HtmlGump HtmlGump(String value, int width, int height) {
        return new HtmlGump(value, width, height);
    }

    public static HtmlGump HtmlGump(
            String value, int width, int height, boolean background, boolean scrollbar) {
        return new HtmlGump(value, width, height, background, scrollbar);
    }

    public static Label Label(MessageContent text) {
        return new Label(text);
    }

    public static Button Button(int normalId, int pressedId, int buttonId, MessageContent caption) {
        return new Button(normalId, pressedId, buttonId, caption);
    }

    public static HtmlGump HtmlGump(MessageContent value, int width, int height) {
        return new HtmlGump(value, width, height);
    }

    public static HtmlGump HtmlGump(MessageContent value, int width, int height, boolean background, boolean scrollbar) {
        return new HtmlGump(value, width, height, background, scrollbar);
    }

    public static TextField TextField(int entryId, int width, MessageContent initialText) {
        return new TextField(entryId, width, 24, 0, initialText, true);
    }

    public static TextField TextField(int entryId, int width, int height, MessageContent initialText) {
        return new TextField(entryId, width, height, 0, initialText, true);
    }

    public static TextField TextField(int entryId, int width, int height,
                                      MessageContent initialText, int maxCharacters) {
        return new TextField(entryId, width, height, 0, initialText, true, maxCharacters);
    }
}
