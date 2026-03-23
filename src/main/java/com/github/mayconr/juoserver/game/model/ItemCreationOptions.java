package com.github.mayconr.juoserver.game.model;

public class ItemCreationOptions {

    private boolean renderOnCreate = true;

    public boolean renderOnCreate() {
        return renderOnCreate;
    }

    public ItemCreationOptions renderOnCreate(boolean value) {
        this.renderOnCreate = value;
        return this;
    }

}
