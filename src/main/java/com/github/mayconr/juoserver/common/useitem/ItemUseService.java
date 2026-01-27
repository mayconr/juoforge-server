package com.github.mayconr.juoserver.common.useitem;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class ItemUseService {
    private final ItemUseRegistry registry;

    public void use(ItemUseContext context) {
        boolean handled = registry.dispatch(context);

        if (!handled) {
            log.debug("No item use trigger found for item [{}-{}]",
                    context.item().getSerialId(),
                    context.item().getName()
            );
        }
    }
}
