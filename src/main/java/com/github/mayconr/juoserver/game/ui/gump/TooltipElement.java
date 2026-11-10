package com.github.mayconr.juoserver.game.ui.gump;

/** A visual control that can show a client-localized tooltip. Layout-only containers do not inherit this. */
public abstract class TooltipElement implements Tooltipped {
    private int tooltipClilocId;
    private String[] tooltipArguments;
    private Integer propertySerialId;

    /** Uses an existing object's properties instead of a cliloc tooltip. The last setter wins. */
    @Override
    public TooltipElement itemProperty(int serialId) {
        if (serialId <= 0) throw new IllegalArgumentException("Item property serial must be positive");
        propertySerialId = serialId;
        tooltipClilocId = 0;
        tooltipArguments = null;
        return this;
    }

    /** Associates literal text through the client's ~1_NOTHING~ cliloc. */
    @Override
    public TooltipElement tooltip(String text) {
        return tooltip(GumpBuilder.CLILOC_1_NOTHING, text);
    }

    /** Associates a cliloc tooltip; arguments are substituted by the client. */
    @Override
    public TooltipElement tooltip(int clilocId, String... arguments) {
        GumpBuilder.tooltipCommand(clilocId, arguments);
        tooltipClilocId = clilocId;
        tooltipArguments = arguments.clone();
        propertySerialId = null;
        return this;
    }

    protected final void renderTooltip(GumpBuilder builder) {
        if (propertySerialId != null) {
            builder.itemProperty(propertySerialId);
        } else if (tooltipClilocId != 0) {
            builder.tooltip(tooltipClilocId, tooltipArguments);
        }
    }
}
