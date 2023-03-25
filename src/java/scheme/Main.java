package scheme;

import mindustry.game.Schematics;
import mindustry.mod.Mod;
import scheme.moded.ModedSchematics;

import static arc.Core.*;
import static mindustry.Vars.*;
import static scheme.SchemeVars.*;

public class Main extends Mod {

    public Main() {
        // well, after the 136th build, it became much easier
        maxSchematicSize = 512;

        // mod reimported through mods dialog
        if (schematics.getClass().getSimpleName().startsWith("Moded")) return;

        assets.load(schematics = m_schematics = new ModedSchematics());
        assets.unload(Schematics.class.getSimpleName()); // prevent dual loading
    }

    @Override
    public void init() {
        ServerIntegration.load();
        ClajIntegration.load();
        SchemeVars.load();

        // ui.schematics = schemas;
        if (m_schematics.requiresDialog) ui.showOkText("@rename.name", "@rename.text", () -> {});
    }

    public static void copy(String text) {
        if (text == null) return;

        app.setClipboardText(text);
        ui.showInfoFade("@copied");
    }
}
