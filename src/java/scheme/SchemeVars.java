package scheme;

import arc.struct.Seq;
import scheme.dialogs.*;
import scheme.moded.ModedSchematics;

public class SchemeVars {

    public static final String subtitle = "I am using Scheme Size [accent]Lite[] btw";

    public static ModedSchematics m_schematics;

    public static SchemasDialog schemas;
    public static JoinViaClajDialog joinViaClaj;
    public static ManageRoomsDialog manageRooms;

    public static Seq<String> clajURLs = Seq.with(
            "n3.xpdustry.com:7025",
            "37.187.73.180:7025",
            "claj.phoenix-network.dev:4000",
            "167.235.159.121:4000");

    /** List of ip servers that block the mod. */
    public static Seq<String> antiModIPs = Seq.with(
            "play.thedimas.pp.ua",
            "91.209.226.11");

    public static void load() {
        schemas = new SchemasDialog();
        joinViaClaj = new JoinViaClajDialog();
        manageRooms = new ManageRoomsDialog();
    }
}
