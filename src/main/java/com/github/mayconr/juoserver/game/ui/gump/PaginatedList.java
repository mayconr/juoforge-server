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

    private final List<Item> items;
    private int width;
    private int itemsPerPage = 4;
    private int spacing = 6;
    private Header header;
    private ItemStyle itemStyle = new ItemStyle(null, 44, 4005, 4007);
    private Navigation navigation = new Navigation(2466, 2467, 2469, 2470, 32, 24, 4, 0, 20);
    private int currentPage = 1;
    private IntUnaryOperator pageMapping = page -> page;
    private Column content;

    public PaginatedList(List<Item> items, int width) {
        this.items = List.copyOf(items);
        width(width);
        var ids = new HashSet<Integer>();
        for (Item item : this.items) {
            if (!ids.add(item.buttonId())) throw new IllegalArgumentException("Duplicate item buttonId");
        }
    }

    public PaginatedList width(int width) {
        if (width <= 0) throw new IllegalArgumentException("width must be positive");
        this.width = width;
        content = null;
        return this;
    }

    public PaginatedList itemsPerPage(int itemsPerPage) {
        if (itemsPerPage <= 0) throw new IllegalArgumentException("itemsPerPage must be positive");
        this.itemsPerPage = itemsPerPage;
        content = null;
        return this;
    }

    public PaginatedList spacing(int spacing) {
        if (spacing < 0) throw new IllegalArgumentException("spacing must not be negative");
        this.spacing = spacing;
        content = null;
        return this;
    }

    /** A null header omits the heading. */
    public PaginatedList header(Header header) {
        this.header = header;
        content = null;
        return this;
    }

    public PaginatedList itemStyle(ItemStyle itemStyle) {
        this.itemStyle = Objects.requireNonNull(itemStyle, "itemStyle");
        content = null;
        return this;
    }

    public PaginatedList navigation(Navigation navigation) {
        this.navigation = Objects.requireNonNull(navigation, "navigation");
        content = null;
        return this;
    }

    private void validatePage(int page, IntUnaryOperator mapping) {
        if (page < 1 || page > pageCount()) throw new IllegalArgumentException("Invalid page range");
        for (int index = 1; index <= pageCount(); index++) {
            if (mapping.applyAsInt(index) < 1) throw new IllegalArgumentException("Invalid native page");
        }
    }

    private Column content() {
        if (content == null) {
            validatePage(currentPage, pageMapping);
            if (pageCount() > 1 && (long) navigation.buttonWidth() * 2 + navigation.gap() * 2L
                    + navigation.rightInset() > width)
                throw new IllegalArgumentException("Navigation does not fit the list width");
            content = createContent(currentPage, pageMapping);
        }
        return content;
    }

    /** Empty lists still have one page. Single-page lists omit the pagination controls. */
    public int pageCount() {
        return Math.max(1, (int) ((items.size() + (long) itemsPerPage - 1) / itemsPerPage));
    }

    public PaginatedList page(int page) {
        return page(page, 1);
    }

    /** Maps a one-based list page to native pages starting at firstPage. */
    public PaginatedList page(int page, int firstPage) {
        if (firstPage < 1 || (long) firstPage + pageCount() - 1 > Integer.MAX_VALUE)
            throw new IllegalArgumentException("Invalid page range");
        return page(page, index -> firstPage + index - 1);
    }

    /** Maps list pages to native pages, allowing several paginated lists in one gump. */
    public PaginatedList page(int page, IntUnaryOperator pageMapping) {
        Objects.requireNonNull(pageMapping, "pageMapping");
        validatePage(page, pageMapping);
        var result = new PaginatedList(items, width).itemsPerPage(itemsPerPage).spacing(spacing)
                .header(header).itemStyle(itemStyle).navigation(navigation);
        result.currentPage = page;
        result.pageMapping = pageMapping;
        return result;
    }

    private Column createContent(int page, IntUnaryOperator pageMapping) {
        var rows = new ArrayList<UIElement>();
        if (header != null) {
            UIElement heading = header.contentFactory() != null
                    ? Objects.requireNonNull(header.contentFactory().apply(width), "header content")
                    : new Panel(header.backgroundId(), width, header.height(), true,
                    Label(header.text()).hue(header.hue()).font(header.font())
                            .horizontalCentered().verticallyCentered());
            rows.add(Panel(width, header.height(), heading));
        }
        int start = (page - 1) * itemsPerPage;
        int end = (int) Math.min((long) start + itemsPerPage, items.size());
        for (Item item : items.subList(start, end)) {
            rows.add(new Panel(itemStyle.backgroundId(), width, itemStyle.height(), true,
                    Button(item.normalId() != null ? item.normalId() : itemStyle.normalId(),
                            item.pressedId() != null ? item.pressedId() : itemStyle.pressedId(),
                            item.buttonId(), item.caption())));
        }
        if (pageCount() > 1) {
            Navigation nav = navigation;
            rows.add(Panel(width, nav.captionHeight(), Label(page + " / " + pageCount()).horizontalCentered()));
            int spacer = width - nav.buttonWidth() * 2 - nav.gap() * 2 - nav.rightInset();
            rows.add(Row(nav.gap(),
                    Panel(nav.buttonWidth(), nav.buttonHeight(), page > 1
                            ? PageButton(nav.previousId(), nav.previousPressedId(), pageMapping.applyAsInt(page - 1)) : Label("")),
                    Panel(spacer, nav.buttonHeight(), Label("")),
                    Panel(nav.buttonWidth(), nav.buttonHeight(), page < pageCount()
                            ? PageButton(nav.nextId(), nav.nextPressedId(), pageMapping.applyAsInt(page + 1)) : Label(""))));
        }
        return Column(spacing, rows.toArray(UIElement[]::new));
    }

    @Override
    public void resolveTexts(Function<MessageContent, String> resolver) { content().resolveTexts(resolver); }

    @Override
    public void layout(LayoutContext ctx) { content().layout(ctx); }

    @Override
    public void render(GumpBuilder builder) { content().render(builder); }

    @Override
    public int getWidth() { return width; }

    @Override
    public int getHeight() { return content().getHeight(); }
}
