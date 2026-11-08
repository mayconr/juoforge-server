package com.github.mayconr.juoserver.game.ui.gump;

import com.github.mayconr.juoserver.game.model.event.message.MessageContent;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.IntUnaryOperator;

import static com.github.mayconr.juoserver.game.ui.gump.DeclarativeGumpUI.*;

/**
 * A vertical button list with native client pagination. Add {@link #page(int)} to each
 * corresponding declarative page; this component never emits page commands itself.
 * Item buttons submit their IDs, while navigation switches pages without submitting.
 */
public final class PaginatedList implements UIElement, Sized {
    public record Item(int buttonId, MessageContent caption, Integer normalId, Integer pressedId) {
        public Item(int buttonId, MessageContent caption) {
            this(buttonId, caption, null, null);
        }

        public Item {
            if (buttonId <= 0) throw new IllegalArgumentException("buttonId must be positive");
            Objects.requireNonNull(caption, "caption");
        }
    }

    public record Header(MessageContent text, Integer backgroundId, int height, int hue, int font,
                         IntFunction<UIElement> contentFactory) {
        public Header(MessageContent text, Integer backgroundId, int height, int hue, int font) {
            this(text, backgroundId, height, hue, font, null);
        }

        /**
         * Custom heading occupying the supplied height. The factory receives the list width
         * and must create a fresh element for each page, including any desired background.
         */
        public Header(int height, IntFunction<UIElement> contentFactory) {
            this(null, null, height, 0, 0xFF, Objects.requireNonNull(contentFactory, "contentFactory"));
        }

        public Header {
            if (contentFactory == null) Objects.requireNonNull(text, "text");
            if (height <= 0) throw new IllegalArgumentException("header height must be positive");
        }
    }

    public record ItemStyle(Integer backgroundId, int height, int normalId, int pressedId) {
        public ItemStyle {
            if (height <= 0) throw new IllegalArgumentException("item height must be positive");
        }
    }

    /** rightInset preserves space after the next button's layout box. */
    public record Navigation(int previousId, int previousPressedId, int nextId, int nextPressedId,
                             int buttonWidth, int buttonHeight, int gap, int rightInset,
                             int captionHeight) {
        public Navigation {
            if (buttonWidth <= 0 || buttonHeight <= 0 || gap < 0 || rightInset < 0 || captionHeight <= 0)
                throw new IllegalArgumentException("Invalid navigation dimensions");
        }
    }

    /** A null header omits the heading; null background IDs render transparent panels. */
    public record Style(int width, int itemsPerPage, int spacing, Header header,
                        ItemStyle item, Navigation navigation) {
        public Style {
            Objects.requireNonNull(item, "item");
            Objects.requireNonNull(navigation, "navigation");
            if (width <= 0 || itemsPerPage <= 0 || spacing < 0)
                throw new IllegalArgumentException("Invalid list dimensions");
            if ((long) navigation.buttonWidth() * 2 + navigation.gap() * 2L
                    + navigation.rightInset() > width)
                throw new IllegalArgumentException("Navigation does not fit the list width");
        }
    }

    private final List<Item> items;
    private final Style style;
    private final Column content;

    public PaginatedList(List<Item> items, Style style) {
        this(items, style, 1, 1);
    }

    private PaginatedList(List<Item> items, Style style, int page, int firstPage) {
        this(items, style, page, index -> Math.addExact(firstPage, index - 1));
        if (firstPage < 1 || (long) firstPage + pageCount() - 1 > Integer.MAX_VALUE)
            throw new IllegalArgumentException("Invalid page range");
    }

    private PaginatedList(List<Item> items, Style style, int page, IntUnaryOperator pageMapping) {
        this.items = List.copyOf(items);
        this.style = Objects.requireNonNull(style, "style");
        var ids = new HashSet<Integer>();
        for (Item item : this.items) {
            if (!ids.add(item.buttonId())) throw new IllegalArgumentException("Duplicate item buttonId");
        }
        if (page < 1 || page > pageCount())
            throw new IllegalArgumentException("Invalid page range");
        for (int index = 1; index <= pageCount(); index++) {
            if (pageMapping.applyAsInt(index) < 1) throw new IllegalArgumentException("Invalid native page");
        }
        content = createContent(page, pageMapping);
    }

    /** Empty lists still have one page, containing the heading and page indicator. */
    public int pageCount() {
        return Math.max(1, (int) ((items.size() + (long) style.itemsPerPage() - 1) / style.itemsPerPage()));
    }

    public PaginatedList page(int page) {
        return page(page, 1);
    }

    /** Maps a one-based list page to native pages starting at firstPage. */
    public PaginatedList page(int page, int firstPage) {
        return new PaginatedList(items, style, page, firstPage);
    }

    /** Maps list pages to native pages, allowing several paginated lists in one gump. */
    public PaginatedList page(int page, IntUnaryOperator pageMapping) {
        return new PaginatedList(items, style, page, Objects.requireNonNull(pageMapping, "pageMapping"));
    }

    private Column createContent(int page, IntUnaryOperator pageMapping) {
        var rows = new ArrayList<UIElement>();
        Header header = style.header();
        if (header != null) {
            UIElement heading = header.contentFactory() != null
                    ? Objects.requireNonNull(header.contentFactory().apply(style.width()), "header content")
                    : new Panel(header.backgroundId(), style.width(), header.height(), true,
                    Label(header.text()).hue(header.hue()).font(header.font())
                            .horizontalCentered().verticallyCentered());
            rows.add(Panel(style.width(), header.height(), heading));
        }
        int start = (page - 1) * style.itemsPerPage();
        int end = (int) Math.min((long) start + style.itemsPerPage(), items.size());
        for (Item item : items.subList(start, end)) {
            rows.add(new Panel(style.item().backgroundId(), style.width(), style.item().height(), true,
                    Button(item.normalId() != null ? item.normalId() : style.item().normalId(),
                            item.pressedId() != null ? item.pressedId() : style.item().pressedId(),
                            item.buttonId(), item.caption())));
        }
        Navigation nav = style.navigation();
        rows.add(Panel(style.width(), nav.captionHeight(), Label(page + " / " + pageCount()).horizontalCentered()));
        int spacer = style.width() - nav.buttonWidth() * 2 - nav.gap() * 2 - nav.rightInset();
        rows.add(Row(nav.gap(),
                Panel(nav.buttonWidth(), nav.buttonHeight(), page > 1
                        ? PageButton(nav.previousId(), nav.previousPressedId(), pageMapping.applyAsInt(page - 1)) : Label("")),
                Panel(spacer, nav.buttonHeight(), Label("")),
                Panel(nav.buttonWidth(), nav.buttonHeight(), page < pageCount()
                        ? PageButton(nav.nextId(), nav.nextPressedId(), pageMapping.applyAsInt(page + 1)) : Label(""))));
        return Column(style.spacing(), rows.toArray(UIElement[]::new));
    }

    @Override
    public void resolveTexts(Function<MessageContent, String> resolver) { content.resolveTexts(resolver); }

    @Override
    public void layout(LayoutContext ctx) { content.layout(ctx); }

    @Override
    public void render(GumpBuilder builder) { content.render(builder); }

    @Override
    public int getWidth() { return style.width(); }

    @Override
    public int getHeight() { return content.getHeight(); }
}
