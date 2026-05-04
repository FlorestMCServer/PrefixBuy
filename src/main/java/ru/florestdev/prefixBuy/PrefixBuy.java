package ru.florestdev.prefixBuy;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.NodeType;
import net.luckperms.api.node.types.PrefixNode;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import net.milkbowl.vault.economy.Economy;
public final class PrefixBuy extends JavaPlugin implements CommandExecutor {

    LuckPerms api_luck = null;
    Economy economy = null;
    @Override
    public void onEnable() {
        getLogger().info("Starting...");
        RegisteredServiceProvider<LuckPerms> provider = Bukkit.getServicesManager().getRegistration(LuckPerms.class);
        api_luck = provider.getProvider();
        economy = setupEconomy();
        if (economy == null) {
            getServer().getPluginManager().disablePlugin(this);
        }

        if (api_luck == null) {
            getServer().getPluginManager().disablePlugin(this);
        }
        getCommand("prefix").setExecutor(this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Формат: /prefix buy <префикс (можно указывать HEX и цветные коды Minecraft>");
            return true;
        }

        if (!args[0].equalsIgnoreCase("buy")) {
            sender.sendMessage(ChatColor.RED + "Формат: /prefix buy <префикс (можно указывать HEX и цветные коды Minecraft>");
            return true;
        }

        if (!economy.has(sender.getName(), 200000)) {
            sender.sendMessage(ChatColor.RED + "У вас нет 200 тысяч FMC рублей для покупки!");
            return true;
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 1; i < args.length; i++) {
            sb.append(args[i]).append(" ");
        }
        String prefix = sb.toString().trim();
        RegisteredServiceProvider<LuckPerms> provider = Bukkit.getServicesManager().getRegistration(LuckPerms.class);
        if (provider != null) {
            LuckPerms lpApi = provider.getProvider();
            User user = lpApi.getUserManager().getUser(getServer().getPlayer(sender.getName()).getUniqueId());

            if (user != null) {
                // Списываем деньги
                economy.withdrawPlayer(sender.getName(), 200000);

                // Устанавливаем префикс (Node)
                // Приоритет ставим высокий (например, 100), чтобы он перекрывал дефолтные
                PrefixNode node = PrefixNode.builder(prefix, 100).build();

                // Очищаем старые префиксы (опционально) и добавляем новый
                user.data().clear(NodeType.PREFIX::matches);
                user.data().add(node);

                // Сохраняем изменения в БД LuckPerms
                lpApi.getUserManager().saveUser(user);

                sender.sendMessage(ChatColor.GREEN + "Вы успешно купили префикс: " + ChatColor.translateAlternateColorCodes('&', prefix));
            }
        } else {
            sender.sendMessage(ChatColor.RED + "Ошибка: LuckPerms не найден!");
        }

        return true;
    }
    public static Economy setupEconomy() {
        if (Bukkit.getPluginManager().getPlugin("Vault") == null) {
            return null;
        } else {
            RegisteredServiceProvider<Economy> rsp = Bukkit.getServicesManager().getRegistration(Economy.class);
            if (rsp == null) {
                return null;
            } else {
                Economy econ = (Economy)rsp.getProvider();
                return econ;
            }
        }
    }

    @Override
    public void onDisable() {
        getLogger().info("Disabling..");
    }
}
