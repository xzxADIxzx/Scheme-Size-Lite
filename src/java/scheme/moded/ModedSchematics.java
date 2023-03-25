package scheme.moded;

import arc.files.Fi;
import arc.math.geom.Point2;
import arc.struct.IntMap;
import arc.struct.Seq;
import arc.struct.StringMap;
import arc.util.Log;
import arc.util.Reflect;
import arc.util.io.Reads;
import arc.util.serialization.Base64Coder;
import mindustry.content.Blocks;
import mindustry.ctype.ContentType;
import mindustry.game.Schematic;
import mindustry.game.Schematics;
import mindustry.game.Schematic.Stile;
import mindustry.io.*;
import mindustry.world.Block;
import mindustry.world.blocks.legacy.LegacyBlock;

import static mindustry.Vars.*;

import java.io.*;
import java.util.zip.InflaterInputStream;

/** Last update - Aug 26, 2022 */
public class ModedSchematics extends Schematics {

    /** Too large schematic file extension. */
    public static final String largeSchematicExtension = "mtls";

    /** Copu paste from {@link Schematics}. */
    public static final byte[] header = { 'm', 's', 'c', 'h' };

    /** Do need to show the dialog. */
    public boolean requiresDialog;

    // region too large schematics fix

    @Override
    public void loadSync() {
        super.loadSync();
        for (Fi file : schematicDirectory.list()) fix(file);
    }

    private void fix(Fi file) { // dont check size for mtls files
        if (!file.extension().equals(largeSchematicExtension) && !isTooLarge(file)) return;

        try {
            if (file.extension().equals(schematicExtension))
                file = rename(file, file.nameWithoutExtension() + "." + largeSchematicExtension);
            all().add(read(file));
        } catch (Throwable error) {
            Log.err("Failed to read schematic from file '@'", file);
            Log.err(error);
        }
    }

    private static boolean isTooLarge(Fi file) {
        try (DataInputStream stream = new DataInputStream(file.read())) {
            for (byte b : header)
                if (stream.read() != b) return false; // missing header

            stream.skip(1L); // schematic version or idk what is it

            DataInputStream dis = new DataInputStream(new InflaterInputStream(stream));
            return dis.readShort() > 128 || dis.readShort() > 128; // next two shorts is a width and height
        } catch (Throwable ignored) {
            return false;
        }
    }

    private Fi rename(Fi file, String to) {
        requiresDialog = true; // show dialog on startup

        Fi dest = file.parent().child(to);
        file.file().renameTo(dest.file());
        return dest;
    }

    public static Schematic readBase64(String schematic) {
        try {
            return read(new ByteArrayInputStream(Base64Coder.decode(schematic.trim())));
        } catch (IOException error) {
            throw new RuntimeException(error);
        }
    }

    public static Schematic read(Fi file) throws IOException {
        Schematic schematic = read(new DataInputStream(file.read(1024)));
        schematic.file = file;

        if (!schematic.tags.containsKey("name")) schematic.tags.put("name", file.nameWithoutExtension());

        return schematic;
    }

    public static Schematic read(InputStream input) throws IOException {
        input.skip(4L); // header bytes already checked
        int ver = input.read();

        try (DataInputStream stream = new DataInputStream(new InflaterInputStream(input))) {
            short width = stream.readShort(), height = stream.readShort();

            StringMap map = new StringMap();
            int tags = stream.readUnsignedByte();
            for (int i = 0; i < tags; i++)
                map.put(stream.readUTF(), stream.readUTF());

            String[] labels = null;
            try { // try to read the categories, but skip if it fails
                labels = JsonIO.read(String[].class, map.get("labels", "[]"));
            } catch (Exception ignored) {}

            IntMap<Block> blocks = new IntMap<>();
            byte length = stream.readByte();
            for (int i = 0; i < length; i++) {
                String name = stream.readUTF();
                Block block = content.getByName(ContentType.block, SaveFileReader.fallback.get(name, name));
                blocks.put(i, block == null || block instanceof LegacyBlock ? Blocks.air : block);
            }

            int total = stream.readInt();
            Seq<Stile> tiles = new Seq<>(total);
            for (int i = 0; i < total; i++) {
                Block block = blocks.get(stream.readByte());
                int position = stream.readInt();
                Object config = ver == 0 ?
                        Reflect.invoke(Schematics.class, "mapConfig", new Object[] { block, stream.readInt(), position }, Block.class, int.class, int.class) :
                        TypeIO.readObject(Reads.get(stream));
                if (block != Blocks.air)
                    tiles.add(new Stile(block, Point2.x(position), Point2.y(position), config, stream.readByte()));
            }

            Schematic out = new Schematic(tiles, map, width, height);
            if (labels != null) out.labels.addAll(labels);
            return out;
        }
    }

    // endregion
}
