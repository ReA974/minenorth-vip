package fr.minenorth.vip;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** Grades attribués : joueur -> (grade, expiration en ms, 0 = permanent). Sauvegardé avec le monde. */
public class VipData extends SavedData {
    public record Entry(String grade, long expiry) {
        public boolean expired() { return expiry > 0 && System.currentTimeMillis() >= expiry; }
    }

    private final Map<UUID, Entry> entries = new HashMap<>();

    public static VipData get(MinecraftServer s) {
        return s.overworld().getDataStorage().computeIfAbsent(VipData::load, VipData::new, "minenorth_vip");
    }

    /** Grade encore valide du joueur, sinon null (un grade expiré est retiré). */
    public Entry active(UUID id) {
        Entry e = entries.get(id);
        if (e == null) return null;
        if (e.expired()) {
            entries.remove(id);
            setDirty();
            return null;
        }
        return e;
    }

    public void set(UUID id, String grade, long expiry) {
        entries.put(id, new Entry(grade, expiry));
        setDirty();
    }

    public boolean remove(UUID id) {
        boolean had = entries.remove(id) != null;
        if (had) setDirty();
        return had;
    }

    public Map<UUID, Entry> all() { return entries; }

    public static VipData load(CompoundTag tag) {
        VipData d = new VipData();
        for (Tag t : tag.getList("grades", Tag.TAG_COMPOUND)) {
            CompoundTag c = (CompoundTag) t;
            if (c.hasUUID("id")) d.entries.put(c.getUUID("id"), new Entry(c.getString("grade"), c.getLong("expiry")));
        }
        return d;
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        ListTag list = new ListTag();
        for (Map.Entry<UUID, Entry> e : entries.entrySet()) {
            CompoundTag c = new CompoundTag();
            c.putUUID("id", e.getKey());
            c.putString("grade", e.getValue().grade());
            c.putLong("expiry", e.getValue().expiry());
            list.add(c);
        }
        tag.put("grades", list);
        return tag;
    }
}
