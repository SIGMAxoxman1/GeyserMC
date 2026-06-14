package dev.geyserfix;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.logging.Logger;

public class GeyserFix extends JavaPlugin {

    private final Logger log = getLogger();

    @Override
    public void onEnable() {
        saveDefaultConfig();

        int delayTicks = getConfig().getInt("delay-ticks", 20);

        // Run after server fully starts so Panel overrides are already done
        Bukkit.getScheduler().runTaskLater(this, () -> {
            fixServerProperties();
            reloadGeyser();
        }, delayTicks);

        log.info("GeyserFix enabled! Will fix config in " + delayTicks + " ticks.");
    }

    private void fixServerProperties() {
        File serverProps = new File(getServer().getWorldContainer(), "server.properties");

        if (!serverProps.exists()) {
            log.warning("server.properties not found!");
            return;
        }

        try {
            List<String> lines = Files.readAllLines(serverProps.toPath());
            List<String> newLines = new ArrayList<>();

            boolean foundEnableQuery = false;
            boolean foundQueryPort = false;

            String enableQuery = getConfig().getString("enable-query", "false");
            String queryPort   = getConfig().getString("query-port", "25565");

            for (String line : lines) {
                if (line.startsWith("enable-query=")) {
                    newLines.add("enable-query=" + enableQuery);
                    foundEnableQuery = true;
                    log.info("Fixed enable-query=" + enableQuery);
                } else if (line.startsWith("query.port=")) {
                    newLines.add("query.port=" + queryPort);
                    foundQueryPort = true;
                    log.info("Fixed query.port=" + queryPort);
                } else {
                    newLines.add(line);
                }
            }

            if (!foundEnableQuery) newLines.add("enable-query=" + enableQuery);
            if (!foundQueryPort)   newLines.add("query.port=" + queryPort);

            Files.write(serverProps.toPath(), newLines);
            log.info("server.properties fixed successfully!");

        } catch (IOException e) {
            log.severe("Failed to fix server.properties: " + e.getMessage());
        }
    }

    private void reloadGeyser() {
        if (Bukkit.getPluginManager().getPlugin("Geyser-Spigot") != null) {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "geyser reload");
            log.info("Geyser reloaded!");
        } else {
            log.warning("Geyser-Spigot not found, skipping reload.");
        }
    }
}
