package com.github.mayconr.juoserver.game.ui.gump;

/** A visual control that can show a client-localized tooltip. Layout-only containers do not inherit this. */
public abstract class TooltipElement implements UIElement {
    private int tooltipClilocId;
    private String[] tooltipArguments;

    /** Associates a cliloc tooltip; arguments are substituted by the client. */
    public TooltipElement tooltip(int clilocId, String... arguments) {
        GumpBuilder.tooltipCommand(clilocId, arguments);
        tooltipClilocId = clilocId;
        tooltipArguments = arguments.clone();
        return this;
    }

    protected final void renderTooltip(GumpBuilder builder) {
        if (tooltipClilocId != 0) {
            builder.tooltip(tooltipClilocId, tooltipArguments);
        }
    }
}
