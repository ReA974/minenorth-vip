package fr.minenorth.vip;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraftforge.fml.loading.FMLPaths;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * config/minenorth_vip.json (créé au premier démarrage). Chaque grade a un préfixe de nom (codes § acceptés) et des avantages :
 * « casinoMax » = multiplicateur des mises maximales au casino (1 = pas de changement). Les autres mods lisent les avantages par nom.
 */
public final class VipConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private static volatile VipConfig current = defaults();

    public static final class Grade {
        public String id = "vip";
        public String nom = "VIP";
        public String prefixe = "§6[VIP] §r";
        public Map<String, Double> avantages = new LinkedHashMap<>();
    }

    public String _aide = "Un grade se donne avec /grade donner <joueur> <grade> [jours]. Avantages : casinoMax = multiplicateur des mises maximales du casino. Rechargez avec /grade recharger.";
    public List<Grade> grades = new ArrayList<>();

    public static VipConfig get() { return current; }

    public Grade grade(String id) {
        if (id == null) return null;
        for (Grade g : grades) if (g.id.equalsIgnoreCase(id)) return g;
        return null;
    }

    private static VipConfig defaults() {
        VipConfig c = new VipConfig();
        Grade vip = new Grade();
        vip.avantages.put("casinoMax", 2.0);
        Grade plus = new Grade();
        plus.id = "vip_plus";
        plus.nom = "VIP+";
        plus.prefixe = "§b[VIP+] §r";
        plus.avantages.put("casinoMax", 5.0);
        c.grades.add(vip);
        c.grades.add(plus);
        return c;
    }

    private static Path file() { return FMLPaths.CONFIGDIR.get().resolve("minenorth_vip.json"); }

    /** Charge (ou crée) la config ; renvoie faux si le fichier est illisible (l'ancienne config est conservée). */
    public static boolean load() {
        Path f = file();
        try {
            if (!Files.exists(f)) {
                Files.writeString(f, GSON.toJson(defaults()), StandardCharsets.UTF_8);
                current = defaults();
                return true;
            }
            VipConfig c = GSON.fromJson(Files.readString(f, StandardCharsets.UTF_8), VipConfig.class);
            if (c == null || c.grades == null) return false;
            c.grades.removeIf(g -> g == null || g.id == null || g.id.isBlank());
            for (Grade g : c.grades) {
                if (g.avantages == null) g.avantages = new LinkedHashMap<>();
                if (g.prefixe == null) g.prefixe = "";
                if (g.nom == null) g.nom = g.id;
            }
            current = c;
            return true;
        } catch (IOException | RuntimeException e) {
            MineNorthVip.LOG.error("[VIP] minenorth_vip.json illisible : {}", e.toString());
            return false;
        }
    }
}
