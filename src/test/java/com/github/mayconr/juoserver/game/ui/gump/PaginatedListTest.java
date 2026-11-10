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
        var list = list(items);
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
                152).itemsPerPage(1).header(null)
                .itemStyle(new PaginatedList.ItemStyle(null, 44, 0x25F8, 0x25FA));
        var first = new GumpBuilder();
        var second = new GumpBuilder();
        new DeclarativeGumpUI(Page(1, list.page(1))).render(first);
        new DeclarativeGumpUI(Page(2, list.page(2))).render(second);
        assertTrue(first.buildLayout().contains("9720 9722 1 0 101}"));
        assertTrue(second.buildLayout().contains("2151 2152 1 0 102}"));
    }

    private PaginatedList list(List<PaginatedList.Item> items) {
        return new PaginatedList(items, 152).itemsPerPage(4).spacing(6)
                .header(new PaginatedList.Header(MessageContent.localized("heading"), 3500, 56, 1255, 255))
                .itemStyle(new PaginatedList.ItemStyle(3000, 44, 4005, 4007))
                .navigation(new PaginatedList.Navigation(2466, 2467, 2469, 2470, 32, 24, 4, 25, 20));
    }

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
        var list = list(List.of(
                new PaginatedList.Item(101, MessageContent.localized("first")),
                new PaginatedList.Item(102, MessageContent.localized("second")))).itemsPerPage(1).header(header);
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
        var list = list(items);
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
        var list = list(List.of());
        var builder = new GumpBuilder();
        new DeclarativeGumpUI(Page(1, list)).render(builder);
        assertEquals(1, list.pageCount());
        assertFalse(builder.buildLayout().contains("{button"));
        assertFalse(builder.buildTexts().contains("1 / 1"));
        assertEquals(56, list.getHeight());
    }

    @Test
    void singlePageWithItemsOmitsPaginationAndItsSpace() {
        var list = list(List.of(
                new PaginatedList.Item(101, MessageContent.localized("item"))));
        var builder = new GumpBuilder();
        new DeclarativeGumpUI(Page(1, list)).render(builder);
        assertEquals(1, list.pageCount());
        assertFalse(builder.buildTexts().contains("1 / 1"));
        assertFalse(builder.buildLayout().contains("2466 2467"));
        assertFalse(builder.buildLayout().contains("2469 2470"));
        assertTrue(builder.buildLayout().contains("4005 4007 1 0 101}"));
        assertEquals(56 + 6 + 44, list.getHeight());
    }

    @Test
    void configurationChangesRebuildContentWithoutChangingExistingPages() {
        var list = list(List.of(new PaginatedList.Item(101, MessageContent.plain("first")),
                new PaginatedList.Item(102, MessageContent.plain("second"))));
        assertEquals(156, list.getHeight());
        var snapshot = list.page(1);
        list.width(200).spacing(10).header(null).itemsPerPage(1);
        assertEquals(2, list.pageCount());
        assertEquals(108, list.getHeight());
        assertEquals(200, list.getWidth());
        assertEquals(1, snapshot.pageCount());
        assertEquals(152, snapshot.getWidth());
        assertEquals(156, snapshot.getHeight());
        var builder = new GumpBuilder();
        new DeclarativeGumpUI(Page(2, list.page(2))).render(builder);
        assertTrue(builder.buildTexts().contains("second"));
        assertTrue(builder.buildTexts().contains("2 / 2"));
    }

    @Test
    void rejectsInvalidPagesAndDuplicateResponseIds() {
        var item = new PaginatedList.Item(101, MessageContent.localized("item"));
        assertThrows(IllegalArgumentException.class, () -> list(List.of(item, item)));
        var list = list(List.of(item));
        assertThrows(IllegalArgumentException.class, () -> list.page(0));
        assertThrows(IllegalArgumentException.class, () -> list.page(2));
        assertThrows(IllegalArgumentException.class, () -> list.page(1, 0));
        assertThrows(IllegalArgumentException.class, () -> new PaginatedList(List.of(item), 0));
        assertThrows(IllegalArgumentException.class, () -> list.itemsPerPage(0));
        assertThrows(IllegalArgumentException.class, () -> list.spacing(-1));
        var overflowing = list(List.of(item, new PaginatedList.Item(102, MessageContent.plain("other"))))
                .itemsPerPage(1).width(10);
        assertThrows(IllegalArgumentException.class, overflowing::getHeight);
    }
}
