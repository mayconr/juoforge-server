package com.github.mayconr.juoserver.game.shared.step;

import com.github.mayconr.juoserver.game.messaging.MessageModule;
import com.github.mayconr.juoserver.game.model.event.message.MessageContent;
import com.github.mayconr.juoserver.infrastructure.datafile.UOFileReader;
import com.github.mayconr.juoserver.infrastructure.flow.FlowStep;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ValidateLightOfSightStep<T extends LightOfSightContext> implements FlowStep<T> {

    private final UOFileReader fileReader;
    private final MessageModule messageModule;

    @Override
    public String name() {
        return "LightOfSight";
    }

    @Override
    public StepResult execute(LightOfSightContext context) {
        if (!hasLineOfSight(context)) {
            messageModule.send(context.targetSource(), MessageContent.localized("{validateLightOfSight.cannotSee}"));
            return StepResult.failure("You cannot see!");
        }
        return StepResult.success();
    }

    private boolean hasLineOfSight(LightOfSightContext ctx) {

        var from = ctx.targetSource();
        var to = ctx.targetDestination();

        int x = from.getX();
        int y = from.getY();

        int x1 = to.getX();
        int y1 = to.getY();

        int z0 = from.getZ();
        int z1 = to.getZ();

        int dx = Math.abs(x1 - x);
        int dy = Math.abs(y1 - y);

        int sx = x < x1 ? 1 : -1;
        int sy = y < y1 ? 1 : -1;

        int err = dx - dy;
        int maxSteps = Math.max(dx, dy);

        for (int step = 0; step <= maxSteps; step++) {
            int z = interpolateZ(z0, z1, step, maxSteps);

            boolean isOrigin = (x == from.getX() && y == from.getY());
            boolean isTarget = (x == x1 && y == y1);

            if (!isOrigin && !isTarget) {
                if (fileReader.hasBlockingStatics(x, y, z)) {
                    return false;
                }
            }

            if (x == x1 && y == y1) {
                break;
            }

            int prevX = x;
            int prevY = y;

            int e2 = err * 2;

            boolean movedX = false;
            boolean movedY = false;

            if (e2 > -dy) {
                err -= dy;
                x += sx;
                movedX = true;
            }

            if (e2 < dx) {
                err += dx;
                y += sy;
                movedY = true;
            }

            if (movedX && movedY) {

                int zMid = z;
                if (fileReader.hasBlockingStatics(prevX + sx, prevY, zMid) &&
                    fileReader.hasBlockingStatics(prevX, prevY + sy, zMid)) {
                    return false;
                }
            }
        }

        return true;
    }

    private int interpolateZ(int z0, int z1, int step, int maxSteps) {
        if (maxSteps == 0) return z0;
        return z0 + (z1 - z0) * step / maxSteps;
    }
}
