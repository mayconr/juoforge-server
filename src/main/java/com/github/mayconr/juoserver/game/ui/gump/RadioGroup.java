package com.github.mayconr.juoserver.game.ui.gump;

import com.github.mayconr.juoserver.game.model.event.message.MessageContent;
import java.util.Objects;
import java.util.function.Function;

/** Delimits a radio section without changing its content's layout. Groups should not be nested. */
public final class RadioGroup implements UIElement, Sized {
    private final int groupId;
    private final UIElement content;
    private final Sized sized;

    public RadioGroup(int groupId, UIElement content) {
        if (groupId < 0) throw new IllegalArgumentException("Radio group ID must be nonnegative");
        this.content = Objects.requireNonNull(content, "Radio group content is required");
        if (!(content instanceof Sized dimensions)) {
            throw new IllegalArgumentException("Radio group content must have layout dimensions");
        }
        this.groupId = groupId;
        this.sized = dimensions;
    }

    @Override
    public void resolveTexts(Function<MessageContent, String> resolver) {
        content.resolveTexts(resolver);
    }

    @Override
    public void layout(LayoutContext ctx) {
        content.layout(ctx);
    }

    @Override
    public void render(GumpBuilder builder) {
        builder.group(groupId);
        content.render(builder);
        builder.endGroup();
    }

    @Override
    public int getWidth() { return sized.getWidth(); }

    @Override
    public int getHeight() { return sized.getHeight(); }

    @Override
    public int getPreferredWidth() { return sized.getPreferredWidth(); }

    @Override
    public int getPreferredHeight() { return sized.getPreferredHeight(); }
}
