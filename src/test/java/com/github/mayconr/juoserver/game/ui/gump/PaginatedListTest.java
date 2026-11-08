package com.github.mayconr.juoserver.game.ui.gump;

import com.github.mayconr.juoserver.game.model.event.message.MessageContent;
import java.util.List;
import java.util.HashSet;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static com.github.mayconr.juoserver.game.ui.gump.DeclarativeGumpUI.Page;

class PaginatedListTest {
    @Test
    void mapsNavigationToPagesSharedWithAnotherList() {
        var items = IntStream.rangeClosed(101, 112)
                .mapToObj(id -> new PaginatedList.Item(id, MessageContent.localized("item." + id))).toList();
        var list = new PaginatedList(items, style);
        var builder = new GumpBuilder();
        new DeclarativeGumpUI(Page(5, list.page(2, page -> 2 + (page - 1) * 3))).render(builder);
        assertTrue(builder.buildLayout().contains("2466 2467 0 2 0}"));
        assertTrue(builder.buildLayout().contains("2469 2470 0 8 0}"));
    }

    @Test
    void itemGraphicsOverrideDefaultsAndSurvivePagination() {
        var list = new PaginatedList(List.of(
                new PaginatedList.Item(101, MessageContent.localized("first")),
                new PaginatedList.Item(102, MessageContent.localized("selected"), 2151, 2152)),
                new PaginatedList.Style(152, 1, 6, null,
                        new PaginatedList.ItemStyle(null, 44, 0x25F8, 0x25FA), style.navigation()));
        var first = new GumpBuilder();
        var second = new GumpBuilder();
        new DeclarativeGumpUI(Page(1, list.page(1))).render(first);
        new DeclarativeGumpUI(Page(2, list.page(2))).render(second);
        assertTrue(first.buildLayout().contains("9720 9722 1 0 101}"));
        assertTrue(second.buildLayout().contains("2151 2152 1 0 102}"));
    }

    private final PaginatedList.Style style = new PaginatedList.Style(152, 4, 6,
            new PaginatedList.Header(MessageContent.localized("heading"), 3500, 56, 1255, 255),
            new PaginatedList.ItemStyle(3000, 44, 4005, 4007),
            new PaginatedList.Navigation(2466, 2467, 2469, 2470, 32, 24, 4, 25, 20));

    @Test
    void customHeaderReceivesWidthAndCreatesIndependentLocalizedContentForEachPage() {
        var headings = new HashSet<UIElement>();
        var header = new PaginatedList.Header(56, width -> {
            assertEquals(152, width);
            var heading = DeclarativeGumpUI.Panel(3500, width, 56, true,
                    DeclarativeGumpUI.Label(MessageContent.localized("custom.heading")));
            assertTrue(headings.add(heading));
            return heading;
        });
        var customStyle = new PaginatedList.Style(152, 1, 6, header, style.item(), style.navigation());
        var list = new PaginatedList(List.of(
                new PaginatedList.Item(101, MessageContent.localized("first")),
                new PaginatedList.Item(102, MessageContent.localized("second"))), customStyle);
        var first = new GumpBuilder();
        var second = new GumpBuilder();
        var firstPage = new DeclarativeGumpUI(Page(1, list));
        var secondPage = new DeclarativeGumpUI(Page(2, list.page(2)));
        firstPage.render(first, content -> "Categories");
        secondPage.render(second, content -> "Categorias");
        assertEquals("Categories", first.buildTexts().getFirst());
        assertEquals("Categorias", second.buildTexts().getFirst());
        var rerender = new GumpBuilder();
        firstPage.render(rerender, content -> "Categories");
        assertEquals(first.buildLayout(), rerender.buildLayout());
        assertEquals(2, headings.size());
        assertTrue(first.buildLayout().contains("{resizepic 0 0 3500 152 56}"));
        assertTrue(first.buildLayout().contains("{button 10 72 4005 4007 1 0 101}"));
    }

    @Test
    void rendersEveryItemOnceWithNativeNavigationAndLocalizedText() {
        var items = IntStream.rangeClosed(101, 118)
                .mapToObj(id -> new PaginatedList.Item(id, MessageContent.localized("item." + id))).toList();
        var list = new PaginatedList(items, style);
        assertEquals(5, list.pageCount());
        for (int page = 1; page <= list.pageCount(); page++) {
            var builder = new GumpBuilder();
            new DeclarativeGumpUI(Page(page + 6, list.page(page, 7)))
                    .render(builder, content -> "Translated");
            String layout = builder.buildLayout();
            assertEquals(page > 1, layout.contains("2466 2467 0 " + (page + 5) + " 0}"));
            assertEquals(page < 5, layout.contains("2469 2470 0 " + (page + 7) + " 0}"));
            assertTrue(builder.buildTexts().contains(page + " / 5"));
            assertTrue(builder.buildTexts().contains("Translated"));
            for (int id = 101; id <= 118; id++) {
                assertEquals((id - 101) / 4 + 1 == page, layout.contains("4005 4007 1 0 " + id + "}"));
            }
            assertTrue(list.page(page).getHeight() <= 330);
        }
    }

    @Test
    void emptyListHasOnePageWithoutNavigationButtons() {
        var list = new PaginatedList(List.of(), style);
        var builder = new GumpBuilder();
        new DeclarativeGumpUI(Page(1, list)).render(builder);
        assertEquals(1, list.pageCount());
        assertFalse(builder.buildLayout().contains("{button"));
        assertTrue(builder.buildTexts().contains("1 / 1"));
    }

    @Test
    void rejectsInvalidPagesAndDuplicateResponseIds() {
        var item = new PaginatedList.Item(101, MessageContent.localized("item"));
        assertThrows(IllegalArgumentException.class, () -> new PaginatedList(List.of(item, item), style));
        var list = new PaginatedList(List.of(item), style);
        assertThrows(IllegalArgumentException.class, () -> list.page(0));
        assertThrows(IllegalArgumentException.class, () -> list.page(2));
        assertThrows(IllegalArgumentException.class, () -> list.page(1, 0));
        assertThrows(IllegalArgumentException.class, () -> new PaginatedList.Style(10, 4, 6,
                null, style.item(), style.navigation()));
        assertThrows(IllegalArgumentException.class, () -> new PaginatedList.Style(152, 0, 6,
                null, style.item(), style.navigation()));
    }
}
