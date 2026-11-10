# Public gump API

Use components from `com.github.mayconr.juoserver.game.ui.gump` to compose the interface and `world.ui().sendGump(...)` to display it. Examples are method snippets with access to `world`, `player`, and, where indicated, `events`.

## Sending and handling responses

```java
import com.github.mayconr.juoserver.game.ui.gump.Button;
import com.github.mayconr.juoserver.game.ui.gump.Column;
import com.github.mayconr.juoserver.game.ui.gump.DeclarativeGumpUI;
import com.github.mayconr.juoserver.game.ui.gump.Label;
import com.github.mayconr.juoserver.game.ui.gump.Page;
import com.github.mayconr.juoserver.game.ui.gump.Panel;
import com.github.mayconr.juoserver.game.ui.gump.TextField;
import java.util.List;

var input = new TextField(10, 180, 24).maxCharacters(40);
var content = new Column(12, List.of(
        new Label("Name"), input, new Button(4005, 4007, 100, "Send")
));
var gump = new DeclarativeGumpUI(
        new Page(0).add(new Panel(3000, 240, 180, content).center())
);
world.ui().sendGump(player, gump, (context, selection) -> {
    if (selection.getButtonId() != 100) return;
    String name = selection.getTextById(10).trim();
    if (name.isEmpty() || name.length() > 40) return;
    world.message().send(context.player(), "Hello, " + name);
});
```

The field ID (`10`) identifies incoming text; the button ID (`100`) identifies the action. Both are independent of graphic IDs.

- `sendGump(player, gump, handler)` uses the configured timeout, defaulting to 30 seconds. The overload with `expirationSeconds` overrides it per send.
- `GumpHandler` receives `GumpContext` and the selection. Use `context.player()`, `selection.getButtonId()`, `getTextById(id)`, and `isSwitchChecked(id)`.
- A regular response can be used only once. Send another gump to continue the flow. Expiration does not visually close the client window.
- Validate IDs, text, and operation rules in the handler, even when controls impose limits.
- `DeclarativeGumpUI.noMove()`, `.noClose()`, and `.noDispose()` prevent dragging, closing with right-click, and closing with Esc, respectively.

## Layout

| Component | Usage |
|---|---|
| `DeclarativeGumpUI` | Takes pages in the constructor or through `addPage(page)`. |
| `Page` | `new Page(index).add(element)`. Page 0 is shared; positive pages are switched on the client. |
| `Panel` | `new Panel(width, height, content)` or `new Panel(backgroundId, width, height, content)`. |
| `Row` | `new Row(gap, List.of(...))`: children side by side. |
| `Column` | `new Column(spacing, List.of(...))` or `.add(element)`: children from top to bottom. |
| `FormField` | `stacked(label, field)` puts the label above the field; `inline(label, field)` puts it beside the field. |

```java
import com.github.mayconr.juoserver.game.ui.gump.Column;
import com.github.mayconr.juoserver.game.ui.gump.Label;
import com.github.mayconr.juoserver.game.ui.gump.Panel;
import com.github.mayconr.juoserver.game.ui.gump.Row;
import java.util.List;

var left = new Column(8, List.of(new Label("A"), new Label("B"))).fillChildren();
var right = new Column(8, List.of(new Label("C"), new Label("D"))).fillChildren();
var row = new Row(12, List.of(left, right)).fillChildren();
var panel = new Panel(3000, 320, 180, row).fillChildren();
```

- Set the window size on the root panel. A panel with a background subtracts 10 pixels of padding on each side.
- `Panel.fillChildren()` assigns the inner area to its content. `Row.fillChildren()` divides width among children; `Column.fillChildren()` divides height. Spacing is subtracted before division.
- `Panel.center()` centers on both axes; `Row.center()` centers horizontally; `Column.center()` centers vertically. On the same component, `fillChildren()` takes precedence over `center()`.
- Filling is not recursive: configure each container that should distribute space among its children. Native artwork and fonts are not automatically stretched.
- `center()` aligns content within its area, rather than the window on screen. Containers do not provide general clipping or scrolling.
- The `Panel` constructor with a background enables resizing by default. The `DeclarativeGumpUI.Panel(backgroundId, width, height, content)` factory disables it; use the overload with `boolean resizable` to choose explicitly.

## Text and input

| Component | Main API |
|---|---|
| `Label` | Constructor with `String` or `MessageContent`; `hue()`, `center()`, `horizontalCentered()`, `verticallyCentered()`, and `cropped(width, height)`. |
| `HtmlGump` | Constructor with text, `MessageContent`, or cliloc, followed by width and height. An overload accepts background and scrollbar options. |
| `TextField` | `new TextField(entryId, width, height).maxCharacters(limit)`. Default height: 24. Overloads accept initial text, hue, and background. |
| `TextArea` | Created through `DeclarativeGumpUI.TextArea(entryId, width, height)`. |

`MessageContent` uses server localization; see [shard messages](message-bundles.md). The `HtmlGump` constructor with a cliloc ID uses client localization:

```java
import com.github.mayconr.juoserver.game.ui.gump.HtmlGump;

var plain = new HtmlGump(1051003, 240, 28);
var colored = new HtmlGump(1051003, 240, 28).color(0x03E0);
var template = new HtmlGump(1060658, 240, 28)
        .color(0x7FFF)
        .arguments("Hello", "World");
```

Cliloc `1060658` contains `~1_val~: ~2_val~`, producing `Hello: World`. `.color()` and `.arguments()` are exclusive to the cliloc constructor. Color accepts UO-encoded values between 0 and `0xFFFF`, rather than 24-bit RGB. Clilocs must exist on the client.

**Usage limitations:**

- `Label.cropped()` visually limits width. Plain-text centering uses estimated dimensions; `font()` depends on client support and is ignored by the reference ClassicUO.
- Supported HTML is the UO client subset. The `HtmlGump` scrollbar does not apply to other controls.
- `TextField.maxCharacters()` is optional and accepts 1 to 239 UTF-16 code units. The field has 4 pixels of padding per side; reserve enough height. Revalidate incoming values on the server.
- `TextArea` does not support multiline editing in the reference ClassicUO.
- Use unique text IDs throughout the gump, including across pages. Prefer `getTextById()` over positional access.

## Images and items

| Component | Main API |
|---|---|
| `Image` | `new Image(graphicId, width, height)`; `hue()`, `partialHue()`, and `cropped(sourceX, sourceY, width, height)`. |
| `ItemIcon` | Item artwork: `new ItemIcon(itemGraphic)`; `hue()`, `center()`, `visibleBounds()`, and `offset()`. |
| `ItemSlot` | Frame: `DeclarativeGumpUI.ItemSlot(width, height, content)`, with an optional background in another overload. |
| `Separator` | Repeats an image: `new Separator(graphicId, width, height)`. |
| `VirtueImage` | `new VirtueImage(graphicId)` or explicit dimensions; supports `hue()`. |

```java
import com.github.mayconr.juoserver.game.ui.gump.Image;
import com.github.mayconr.juoserver.game.ui.gump.ItemIcon;

var region = new Image(0x006F, 50, 50).cropped(5, 5, 30, 30).partialHue(4);
var item = new ItemIcon(0x0F61, 82, 46)
        .visibleBounds(38, 0, 30, 30).hue(2118).center();
```

Image dimensions reserve layout space without resizing artwork. `Image.cropped()` selects an actual image region; `ItemIcon.visibleBounds()` only guides alignment without cropping it. Graphics must exist on the client.

Partial hue and pages containing uncropped colored images have limitations in the reference ClassicUO; verify results on the version used by the shard. Cropping with partial hue is supported by that reference.

## Buttons and selections

| Component | Main API |
|---|---|
| `Button` | `new Button(normalId, pressedId, buttonId, caption)`; optional caption as text or `MessageContent`; `captionCentered()`. |
| `ItemButton` | Adds an item graphic: `new ItemButton(normalId, pressedId, buttonId, itemGraphic)`; `hue()` and `itemOffset(x, y)`. |
| `PageButton` | `new PageButton(normalId, pressedId, targetPage)`: switches pages without invoking the handler. |
| `Checkbox` | `DeclarativeGumpUI.Checkbox(uncheckedId, checkedId, switchId, checked)`: independent selection. |
| `Radio` | `new Radio(uncheckedId, checkedId, switchId, checked)`: mutually exclusive option within a group. |
| `RadioGroup` | `new RadioGroup(groupId, content)`: groups options in a `Row` or `Column`. |

Buttons use the clickable area of client graphics. The estimated layout size of `Button` and `ItemButton` is 80x22; `itemOffset()` moves only the item artwork. `hoverId()` has no effect in the reference ClassicUO. A `PageButton` can be placed in a panel when explicit reserved space is needed.

Do not nest `RadioGroup` or reuse a group ID to try to merge separate groups. Each group must use option IDs distinct from other groups. Check `selection.isSwitchChecked(switchId)` and validate expected exclusivity in the handler. `Radio.setChecked()` defines the state for the next render.

## PaginatedList

`PaginatedList` takes items and a style. Item buttons send their IDs; navigation buttons only switch pages.

```java
import com.github.mayconr.juoserver.game.model.event.message.MessageContent;
import com.github.mayconr.juoserver.game.ui.gump.DeclarativeGumpUI;
import com.github.mayconr.juoserver.game.ui.gump.Page;
import com.github.mayconr.juoserver.game.ui.gump.PaginatedList;
import java.util.List;

var list = new PaginatedList(List.of(
        new PaginatedList.Item(100, MessageContent.plain("Swords"))
), 300).itemsPerPage(4).spacing(6)
        .header(new PaginatedList.Header(MessageContent.plain("Categories"), 3500, 56, 1152, 0xFF))
        .itemStyle(new PaginatedList.ItemStyle(3000, 44, 4005, 4007))
        .navigation(new PaginatedList.Navigation(2466, 2467, 2469, 2470, 32, 24, 4, 0, 20));
var gump = new DeclarativeGumpUI();
for (int index = 1; index <= list.pageCount(); index++) {
    gump.addPage(new Page(index).add(list.page(index)));
}
```

`Style` defines width, items per page, spacing, `Header`, `ItemStyle`, and `Navigation`. A null header omits the heading; it can also take a content factory. `Item` accepts entry-specific graphics. Item IDs must be positive and unique.

`pageCount()` returns at least 1. Add each `page(n)` to an interface `Page`. Overloads allow choosing the first page or mapping destinations through a function. Use distinct visual instances for each position and consider the number of pages sent.

## Tooltips and virtues

Components implementing `Tooltipped` provide:

```java
import com.github.mayconr.juoserver.game.ui.gump.Image;

var image = new Image(0x006F, 50, 50);
image.tooltip("Hello World");
image.tooltip(1060658, "Name", "Value");
image.itemProperty(player.getSerialId());
```

The last call takes precedence. `tooltip(String)` uses the free-text cliloc; the overload with an ID accepts arguments. `itemProperty()` takes the serial of an existing object, not its graphic. It requires available properties and tooltips enabled on the client. A panel needs a background to have its own tooltip.

Tooltip text and cliloc arguments accept only printable ASCII, without `@`, braces, tabs, or line breaks. Configure component-specific methods before chaining a tooltip, or keep the concrete variable for separate calls.

`VirtueImage` provides native tooltips and double-click handling for graphics from `0x69` to `0x70`: Compassion, Honesty, Honor, Humility, Justice, Sacrifice, Spirituality, and Valor. Interaction publishes `VirtueSelected`, leaving the gump open:

```java
import com.github.mayconr.juoserver.game.model.event.VirtueSelected;
import com.github.mayconr.juoserver.game.ui.gump.VirtueImage;

var virtue = new VirtueImage(0x006F);
// Register once during module initialization.
events.register(VirtueSelected.class, event ->
        world.message().send(event.player(), "Virtue: " + event.graphicId()));
```

The module defines virtue effects. The response does not identify the originating window when several offer the same graphic.

## Factories and low-level API

`DeclarativeGumpUI` provides public component factories, including `Checkbox`, `TextArea`, and `ItemSlot`. `UIElement` is the common element contract; `Tooltipped` identifies elements supporting tooltips.

To build commands directly, `GumpBuilder` provides text, image, interaction, and transparency (`checkerTrans`) methods. Start with `page(index)`; use `buildLayout()`, `buildTexts()`, or `build()` to obtain the result.

See the [public gump package types](../src/main/java/com/github/mayconr/juoserver/game/ui/gump/). Validate artwork, fonts, and appearance on the shard client.
