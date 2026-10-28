package com.github.mayconr.juoserver.game.ui;

import com.github.mayconr.juoserver.game.model.UOPlayer;
import com.github.mayconr.juoserver.game.model.SendSkillType;
import com.github.mayconr.juoserver.game.ui.gump.DeclarativeGumpUI;
import com.github.mayconr.juoserver.game.ui.gump.GumpHandler;

import java.util.List;

/** UI operations available to shard code, without client packet handlers. */
public interface WorldUI {

    /**
     * Requests tooltips for mobiles, items, and entries in the player's vendor session.
     * Publishes a tooltip request when at least one object is collected.
     *
     * @param player the player who will receive the tooltips
     * @param serials the serial identifiers of the objects whose tooltips are requested
     * @throws com.github.mayconr.juoserver.game.world.ModuleOperationException
     *         if the tooltip request flow fails
     */
    void tooltipRequest(UOPlayer player, List<Integer> serials);

    /**
     * Requests that a mobile's paperdoll be opened for the player.
     * The target mobile may be the player itself.
     *
     * @param player the player who will view the paperdoll
     * @param mobileSerial the serial identifier of the mobile whose paperdoll is requested
     */
    void openPaperdoll(UOPlayer player, int mobileSerial);

    /**
     * Sends a declarative gump to the player and registers its response handler.
     *
     * @param player the player who will receive the gump
     * @param gumpUI the declarative definition of the gump to display
     * @param handler the handler invoked when the player responds to the gump
     */
    void sendGump(UOPlayer player, DeclarativeGumpUI gumpUI, GumpHandler handler);

    /**
     * Requests that a mobile's skill information be sent to the player using the specified format.
     * The type controls the packet format, whether skill caps are included, and whether
     * the client opens its skills window:
     * <ul>
     *   <li>{@link SendSkillType#FULL_LIST}: sends all skills without caps.
     *       The client uses a default cap of 100.0 for each skill.</li>
     *   <li>{@link SendSkillType#FULL_LIST_WITH_CAP}: sends all skills with their caps.</li>
     *   <li>{@link SendSkillType#FULL_LIST_WITH_CAP_AND_OPEN}: sends all skills with their
     *       caps and opens the skills window if it is not already open.</li>
     *   <li>{@link SendSkillType#SINGLE_UPDATE}: updates one skill without its cap;
     *       The client uses a default cap of 100.0 and may display a skill change message.</li>
     *   <li>{@link SendSkillType#SINGLE_UPDATE_WITH_CAP}: updates one skill with its cap
     *       and may display a skill change message.</li>
     * </ul>
     * Full-list types also open the window when the client has a pending skills request.
     * Single-update types refresh an existing window without opening it.
     *
     * <p>This method currently supplies the target mobile's entire skill collection
     * for every type. The client reads only the first entry for single-update types,
     * so use a full-list type to display all of the mobile's skills.
     * The client applies the received skill data to its local player; the packet
     * does not identify the target mobile.</p>
     *
     * @param player the player who will receive the skill information
     * @param requestedSkillSerialId the serial identifier of the mobile whose skills are requested
     * @param type the skill list or single-update format, cap inclusion, and window-opening
     *             behavior described above; must not be {@code null}
     * @throws NullPointerException if {@code type} is {@code null}
     */
    void sendSkillGump(UOPlayer player, int requestedSkillSerialId, SendSkillType type);

    /**
     * Requests that a mobile's status information be sent to the player.
     *
     * @param player the player who will receive the status information
     * @param requestedStatusSerial the serial identifier of the mobile whose status is requested
     * @throws com.github.mayconr.juoserver.game.world.ModuleOperationException
     *         if the status request flow fails, including when the mobile does not exist
     */
    void sendStatusGump(UOPlayer player, int requestedStatusSerial);
}
