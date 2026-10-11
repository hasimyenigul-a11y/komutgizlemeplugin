package me.komutgizle;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerCommandSendEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public final class KomutGizle extends JavaPlugin implements Listener {

    private final Set<String> izinli = new HashSet<>();
    private boolean engelle;
    private String engelMesaji;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        yukle();
        getServer().getPluginManager().registerEvents(this, this);
    }

    private void yukle() {
        reloadConfig();
        izinli.clear();
        for (String s : getConfig().getStringList("izinli-komutlar")) {
            izinli.add(s.toLowerCase(Locale.ROOT).replace("/", ""));
        }
        engelle = getConfig().getBoolean("komut-engelle", true);
        engelMesaji = ChatColor.translateAlternateColorCodes('&',
                getConfig().getString("engel-mesaji", "&cBu komutu kullanamazsin."));
    }

    private boolean muaf(Player p) {
        return p.isOp() || p.hasPermission("komutgizle.bypass");
    }

    private boolean izinliMi(String ad) {
        ad = ad.toLowerCase(Locale.ROOT);
        int i = ad.indexOf(':');
        String kisa = i >= 0 ? ad.substring(i + 1) : ad;
        return izinli.contains(ad) || izinli.contains(kisa);
    }

    // Tab-complete / komut listesinde gorunenleri filtreler
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onCommandSend(PlayerCommandSendEvent e) {
        if (muaf(e.getPlayer())) return;
        e.getCommands().removeIf(c -> !izinliMi(c));
    }

    // Yazilarak kullanilmasini engeller
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPreprocess(PlayerCommandPreprocessEvent e) {
        if (!engelle || muaf(e.getPlayer())) return;
        String msg = e.getMessage();
        if (msg.length() < 2) return;
        String ad = msg.substring(1).split(" ", 2)[0];
        if (!izinliMi(ad)) {
            e.setCancelled(true);
            e.getPlayer().sendMessage(engelMesaji);
        }
    }

    @Override
    public boolean onCommand(CommandSender s, Command cmd, String label, String[] args) {
        if (!s.hasPermission("komutgizle.admin")) {
            s.sendMessage(ChatColor.RED + "Yetkin yok.");
            return true;
        }
        if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
            yukle();
            for (Player p : getServer().getOnlinePlayers()) p.updateCommands();
            s.sendMessage(ChatColor.GREEN + "KomutGizle yenilendi.");
            return true;
        }
        s.sendMessage(ChatColor.YELLOW + "Kullanim: /komutgizle reload");
        return true;
    }
}
