package fr.minenorth.vip;

import fr.minenorth.api.GradeService;
import fr.minenorth.api.MineNorth;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.common.Mod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

@Mod(MineNorthVip.MOD_ID)
public class MineNorthVip {
    public static final String MOD_ID = "minenorthvip";
    public static final Logger LOG = LoggerFactory.getLogger("MineNorthVip");

    public MineNorthVip() {
        VipConfig.load();
        MineNorth.provide(GradeService.class, new GradeService() {
            @Override
            public String grade(MinecraftServer s, UUID player) {
                VipData.Entry e = VipData.get(s).active(player);
                return e == null ? null : e.grade();
            }

            @Override
            public double perk(MinecraftServer s, UUID player, String perk, double def) {
                VipData.Entry e = VipData.get(s).active(player);
                VipConfig.Grade g = e == null ? null : VipConfig.get().grade(e.grade());
                Double v = g == null ? null : g.avantages.get(perk);
                return v == null ? def : v;
            }
        });
    }
}
