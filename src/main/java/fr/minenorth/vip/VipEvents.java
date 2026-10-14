package fr.minenorth.vip;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

/** Commande /grade (OP) et préfixe de grade devant le nom (chat, liste des joueurs). Pas de commande /vip. */
@Mod.EventBusSubscriber(modid = MineNorthVip.MOD_ID)
public final class VipEvents {
    private VipEvents() {}

    private static String prefix(ServerPlayer p) {
        VipData.Entry e = VipData.get(p.server).active(p.getUUID());
        VipConfig.Grade g = e == null ? null : VipConfig.get().grade(e.grade());
        return g == null ? "" : g.prefixe;
    }

    @SubscribeEvent
    public static void nameFormat(PlayerEvent.NameFormat e) {
        if (!(e.getEntity() instanceof ServerPlayer p)) return;
        String pre = prefix(p);
        if (!pre.isEmpty()) e.setDisplayname(Component.literal(pre).append(e.getDisplayname()));
    }

    @SubscribeEvent
    public static void tabFormat(PlayerEvent.TabListNameFormat e) {
        if (!(e.getEntity() instanceof ServerPlayer p)) return;
        String pre = prefix(p);
        if (!pre.isEmpty()) e.setDisplayName(Component.literal(pre).append(e.getDisplayName() != null ? e.getDisplayName() : p.getName()));
    }

    private static void refresh(ServerPlayer p) {
        p.refreshDisplayName();
        p.refreshTabListName();
    }

    /** Donne un grade (jours <= 0 : permanent) ; prévient le joueur s'il est connecté et met à jour son nom. Utilisé par /grade et le panneau admin. */
    public static void grant(net.minecraft.server.MinecraftServer s, java.util.UUID id, VipConfig.Grade g, int days) {
        long expiry = days <= 0 ? 0 : System.currentTimeMillis() + days * 86_400_000L;
        VipData.get(s).set(id, g.id, expiry);
        ServerPlayer p = s.getPlayerList().getPlayer(id);
        if (p != null) {
            refresh(p);
            p.sendSystemMessage(Component.literal("§6Vous avez reçu le grade §e" + g.nom + "§6 (" + until(expiry) + ")."));
        }
    }

    /** Retire le grade ; faux si le joueur n'en avait pas. */
    public static boolean revoke(net.minecraft.server.MinecraftServer s, java.util.UUID id) {
        boolean had = VipData.get(s).remove(id);
        ServerPlayer p = s.getPlayerList().getPlayer(id);
        if (had && p != null) refresh(p);
        return had;
    }

    private static String until(long expiry) {
        return expiry <= 0 ? "permanent" : "jusqu'au " + new SimpleDateFormat("dd/MM/yyyy HH:mm").format(new Date(expiry));
    }

    @SubscribeEvent
    public static void register(RegisterCommandsEvent e) {
        CommandDispatcher<CommandSourceStack> d = e.getDispatcher();
        d.register(Commands.literal("grade").requires(s -> s.hasPermission(2))
                .then(Commands.literal("donner")
                        .then(Commands.argument("joueur", EntityArgument.player())
                                .then(Commands.argument("grade", StringArgumentType.word())
                                        .suggests((c, b) -> net.minecraft.commands.SharedSuggestionProvider.suggest(VipConfig.get().grades.stream().map(g -> g.id), b))
                                        .executes(c -> give(c.getSource(), EntityArgument.getPlayer(c, "joueur"), StringArgumentType.getString(c, "grade"), 0))
                                        .then(Commands.argument("jours", IntegerArgumentType.integer(1, 3650))
                                                .executes(c -> give(c.getSource(), EntityArgument.getPlayer(c, "joueur"), StringArgumentType.getString(c, "grade"),
                                                        IntegerArgumentType.getInteger(c, "jours")))))))
                .then(Commands.literal("retirer")
                        .then(Commands.argument("joueur", EntityArgument.player()).executes(c -> {
                            ServerPlayer p = EntityArgument.getPlayer(c, "joueur");
                            boolean had = revoke(p.server, p.getUUID());
                            c.getSource().sendSuccess(() -> Component.literal(had ? "§aGrade retiré à " + p.getGameProfile().getName() + "." : "§7" + p.getGameProfile().getName() + " n'avait pas de grade."), true);
                            return had ? 1 : 0;
                        })))
                .then(Commands.literal("info")
                        .executes(c -> info(c.getSource(), c.getSource().getPlayerOrException()))
                        .then(Commands.argument("joueur", EntityArgument.player()).executes(c -> info(c.getSource(), EntityArgument.getPlayer(c, "joueur")))))
                .then(Commands.literal("liste").executes(c -> {
                    CommandSourceStack src = c.getSource();
                    src.sendSuccess(() -> Component.literal("§6Grades disponibles :"), false);
                    for (VipConfig.Grade g : VipConfig.get().grades)
                        src.sendSuccess(() -> Component.literal("§e" + g.id + " §7(" + g.nom + ") avantages : " + g.avantages), false);
                    Map<UUID, VipData.Entry> all = VipData.get(src.getServer()).all();
                    for (Map.Entry<UUID, VipData.Entry> en : all.entrySet()) {
                        if (en.getValue().expired()) continue;
                        String name = fr.minenorth.api.MineNorth.pseudo(src.getServer(), en.getKey());
                        src.sendSuccess(() -> Component.literal("§7- " + name + " : " + en.getValue().grade() + " (" + until(en.getValue().expiry()) + ")"), false);
                    }
                    return 1;
                }))
                .then(Commands.literal("recharger").executes(c -> {
                    boolean ok = VipConfig.load();
                    c.getSource().sendSuccess(() -> Component.literal(ok ? "§aConfiguration des grades rechargée." : "§cminenorth_vip.json illisible : ancienne configuration conservée."), true);
                    return ok ? 1 : 0;
                })));
    }

    private static int give(CommandSourceStack src, ServerPlayer p, String id, int days) {
        VipConfig.Grade g = VipConfig.get().grade(id);
        if (g == null) {
            src.sendFailure(Component.literal("Grade inconnu : " + id + " (voir /grade liste)."));
            return 0;
        }
        grant(p.server, p.getUUID(), g, days);
        long expiry = days <= 0 ? 0 : System.currentTimeMillis() + days * 86_400_000L;
        src.sendSuccess(() -> Component.literal("§aGrade " + g.nom + " donné à " + p.getGameProfile().getName() + " (" + until(expiry) + ")."), true);
        return 1;
    }

    private static int info(CommandSourceStack src, ServerPlayer p) {
        VipData.Entry e = VipData.get(p.server).active(p.getUUID());
        VipConfig.Grade g = e == null ? null : VipConfig.get().grade(e.grade());
        src.sendSuccess(() -> Component.literal(g == null ? "§7" + p.getGameProfile().getName() + " : aucun grade."
                : "§6" + p.getGameProfile().getName() + " : §e" + g.nom + " §7(" + until(e.expiry()) + ") avantages " + g.avantages), false);
        return 1;
    }
}
