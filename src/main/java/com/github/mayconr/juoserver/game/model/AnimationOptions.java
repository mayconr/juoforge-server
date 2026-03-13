package com.github.mayconr.juoserver.game.model;

public record AnimationOptions(AnimationRepeat repeat, AnimationType type, AnimationDirection direction, int frame) {

    public static AnimationOptions simpleForward(AnimationType type, int frame) {
        return new AnimationOptions(AnimationRepeat.ONCE, type,AnimationDirection.FORWARD,  frame);
    }

    public static AnimationOptions simpleBackward(AnimationType type, int frame) {
        return new AnimationOptions(AnimationRepeat.ONCE, type,AnimationDirection.BACKWARD,  frame);
    }

    public static AnimationOptions of(AnimationRepeat repeat, AnimationType type, AnimationDirection direction, int frame) {
        return new AnimationOptions(repeat, type, direction,  frame);
    }

}
