package dev.geyserfix;

import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.logging.Logger;

public class GeyserFix extends JavaPlugin {

    private final Logger log = getLogger();

    @Override
    public void onLoad() {
        // onLoad runs BEFORE any other plugin loads, including Geyser
        saveDefaultConfig();
        fixGeyserConfig();
    }

    @Override
    public void onEnable() {
        log.info("GeyserFix active - Geyser config was patched before startup.");
    }

    private void fixGeyserConfig() {
        // Path to Geyser config
        File geyserConfig = new File(getDataFolder().getParentFile(), "Geyser-Spigot/config.yml");

        if (!geyserConfig.exists()) {
            log.warning("Geyser config not found at: " + geyserConfig.getPath());
            log.warning("Make sure Geyser-Spigot is installed and has run at least once.");
            return;
        }

        String bedrockAddress = getConfig().getString("bedrock-address", "0.0.0.0");
        String bedrockPort    = getConfig().getString("bedrock-port", "10160");
        String clonePort      = getConfig().getString("clone-remote-port", "false");

        try {
            List<String> lines = Files.readAllLines(geyserConfig.toPath());
            List<String> newLines = new ArrayList<>();

            boolean inBedrock = false;
            boolean fixedPort = false;
            boolean fixedAddress = false;
            boolean fixedClone = false;

            for (String line : lines) {
                String trimmed = line.trim();

                // Detect bedrock: section
                if (trimmed.equals("bedrock:")) {
                    inBedrock = true;
                    newLines.add(line);
                    continue;
                }

                // Detect leaving bedrock section (another top-level key)
                if (inBedrock && !line.startsWith(" ") && !line.startsWith("\t") && !trimmed.isEmpty()) {
                    inBedrock = false;
                }

                if (inBedrock) {
                    if (trimmed.startsWith("port:")) {
                        newLines.add("    port: " + bedrockPort);
                        fixedPort = true;
                        log.info("Patched Geyser bedrock.port -> " + bedrockPort);
                        continue;
                    }
                    if (trimmed.startsWith("address:")) {
                        newLines.add("    address: " + bedrockAddress);
                        fixedAddress = true;
                        log.info("Patched Geyser bedrock.address -> " + bedrockAddress);
                        continue;
                    }
                    if (trimmed.startsWith("clone-remote-port:")) {
                        newLines.add("    clone-remote-port: " + clonePort);
                        fixedClone = true;
                        log.info("Patched Geyser clone-remote-port -> " + clonePort);
                        continue;
                    }
                }

                newLines.add(line);
            }

            if (!fixedPort || !fixedAddress || !fixedClone) {
                log.warning("Some Geyser config fields were not found! fixed: port=" + fixedPort + " address=" + fixedAddress + " clone=" + fixedClone);
            }

            Files.write(geyserConfig.toPath(), newLines);
            log.info("Geyser config.yml patched successfully! Bedrock port set to " + bedrockPort);

        } catch (IOException e) {
            log.severe("Failed to patch Geyser config: " + e.getMessage());
        }
    }
}
