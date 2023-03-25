package scheme;

import arc.Events;
import arc.struct.IntMap;
import arc.util.Reflect;
import mindustry.game.EventType.*;
import mindustry.gen.Call;
import mindustry.io.JsonIO;

import static mindustry.Vars.*;
import static scheme.SchemeVars.*;

/** https://github.com/xzxADIxzx/Scheme-Size/blob/main/src/java/scheme/ServerIntegration.java */
@SuppressWarnings("unchecked")
public class ServerIntegration {

    /** List of user ids that use this mod. */
    public static IntMap<String> SSUsers = new IntMap<>(8);

    public static void load() {
        // region Server

        Events.on(PlayerJoin.class, event -> Call.clientPacketReliable(event.player.con, "SendMeSubtitle", String.valueOf(player.id)));
        Events.on(PlayerLeave.class, event -> SSUsers.remove(event.player.id));

        netServer.addPacketHandler("MySubtitle", (target, args) -> {
            SSUsers.put(target.id, args);
            Call.clientPacketReliable("Subtitles", JsonIO.write(SSUsers));
        });

        // endregion
        // region Client

        Events.run(HostEvent.class, ServerIntegration::clear);
        Events.run(ClientPreConnectEvent.class, ServerIntegration::clear);

        netClient.addPacketHandler("SendMeSubtitle", args -> {
            if (!antiModIPs.contains(Reflect.<String>get(ui.join, "lastIp"))) Call.serverPacketReliable("MySubtitle", subtitle);
        });

        netClient.addPacketHandler("Subtitles", args -> {
            SSUsers = JsonIO.read(IntMap.class, args);
        });

        // endregion
    }

    /** Clears all data about users. */
    public static void clear() {
        SSUsers.clear();
        SSUsers.put(player.id, subtitle); // put the host's subtitle so that you do not copy the int map later
    }
}
