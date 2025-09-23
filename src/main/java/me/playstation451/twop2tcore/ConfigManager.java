package me.playstation451.twop2tcore;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class ConfigManager {

    private final JavaPlugin plugin;
    private FileConfiguration config; // Make config a class member

    private List<String> allowedCommands = new ArrayList<>();
    private List<String> allowedOPs = new ArrayList<>();
    private List<String> joinMessages = new ArrayList<>();
    private List<String> leaveMessages = new ArrayList<>();
    private Set<Material> illegalMaterials = new HashSet<>();
    private Map<Enchantment, Integer> enchantmentLimits = new HashMap<>();
    private int explosionBlocksPerTick = 20; // Default value

    public ConfigManager(JavaPlugin plugin) {
        this.plugin = plugin;
        loadConfig();
    }

    public void loadConfig() {
        File pluginsFolder = Bukkit.getServer().getPluginsFolder();
        File pluginFolder = new File(pluginsFolder, "2p2tCore");
        if (!pluginFolder.exists()) {
            pluginFolder.mkdir();
        }

        File configFile = new File(pluginFolder, "config.yml");
        if (!configFile.exists()) {
            plugin.getLogger().warning("config.yml not found, creating default.");
            try (FileWriter writer = new FileWriter(configFile)) {
                String initialConfig = "allowedCommands:\n  - \"/say\"\n  - \"/w\"\n  - \"/msg\"\n  - \"/message\"\n  - \"/register\"\n  - \"/login\"\n  - \"/ignore\"\nAllowedOPs:\n  - \"YourUsernameHere\"\nJoinMSG:\n  - \"Welcome %player%!\"\n  - \"Glad to see you, %player%!\"\nLeaveMSG:\n  - \"Goodbye %player%!\"\n  - \"See you next time, %player%!\"\nIllegalMaterials:\n  - \"BEDROCK\"\n  - \"BARRIER\"\n  - \"COMMAND_BLOCK\"\n  - \"STRUCTURE_BLOCK\"\n  - \"JIGSAW\"\n  - \"LIGHT\"\n  - \"DEBUG_STICK\"\nEnchantmentLimits:\n  PROTECTION: 4\n  FIRE_PROTECTION: 4\n  FEATHER_FALLING: 4\n  BLAST_PROTECTION: 4\n  PROJECTILE_PROTECTION: 4\n  RESPIRATION: 3\n  AQUA_AFFINITY: 1\n  THORNS: 3\n  DEPTH_STRIDER: 3\n  FROST_WALKER: 2\n  BINDING_CURSE: 1\n  SHARPNESS: 5\n  SMITE: 5\n  BANE_OF_ARTHROPODS: 5\n  KNOCKBACK: 2\n  FIRE_ASPECT: 2\n  LOOTING: 3\n  SWEEPING_EDGE: 3\n  EFFICIENCY: 5\n  SILK_TOUCH: 1\n  UNBREAKING: 3\n  FORTUNE: 3\n  POWER: 5\n  PUNCH: 2\n  FLAME: 1\n  INFINITY: 1\n  LUCK_OF_THE_SEA: 3\n  LURE: 3\n  MENDING: 1\n  VANISHING_CURSE: 1\nExplosionBlocksPerTick: 20";
                writer.write(initialConfig);
            } catch (IOException e) {
                plugin.getLogger().severe("Error creating config.yml: " + e.getMessage());
            }
        }

        try {
            this.config = YamlConfiguration.loadConfiguration(configFile); // Assign to class member
            List<String> commands = config.getStringList("allowedCommands");
            if (commands != null) {
                allowedCommands = new ArrayList<>(commands);
                plugin.getLogger().info("Allowed commands loaded: " + allowedCommands);
            } else {
                plugin.getLogger().warning("Allowed commands section is missing or invalid in config.yml, using default values.");
                allowedCommands = new ArrayList<>();
            }

            List<String> ops = config.getStringList("AllowedOPs");
            if (ops != null) {
                allowedOPs = new ArrayList<>(ops);
                plugin.getLogger().info("Allowed OPs loaded: " + allowedOPs);
            } else {
                plugin.getLogger().warning("AllowedOPs section is missing or invalid in config.yml, using default values.");
                allowedOPs = new ArrayList<>();
            }

            List<String> joins = config.getStringList("JoinMSG");
            if (joins != null) {
                joinMessages = new ArrayList<>(joins);
                plugin.getLogger().info("Join messages loaded: " + joinMessages);
            } else {
                plugin.getLogger().warning("JoinMSG section is missing or invalid in config.yml, using default values.");
                joinMessages = new ArrayList<>();
            }

            List<String> leaves = config.getStringList("LeaveMSG");
            if (leaves != null) {
                leaveMessages = new ArrayList<>(leaves);
                plugin.getLogger().info("Leave messages loaded: " + leaves);
            } else {
                plugin.getLogger().warning("LeaveMSG section is missing or invalid in config.yml, using default values.");
                leaveMessages = new ArrayList<>();
            }

            List<String> illegalMatNames = config.getStringList("IllegalMaterials");
            if (illegalMatNames != null) {
                illegalMaterials = illegalMatNames.stream()
                        .map(Material::matchMaterial)
                        .filter(java.util.Objects::nonNull)
                        .collect(Collectors.toSet());
                plugin.getLogger().info("Illegal materials loaded: " + illegalMaterials);
            } else {
                plugin.getLogger().warning("IllegalMaterials section is missing or invalid in config.yml, using default values.");
                illegalMaterials = new HashSet<>(Arrays.asList(
                        Material.BEDROCK,
                        Material.BARRIER,
                        Material.COMMAND_BLOCK,
                        Material.STRUCTURE_BLOCK,
                        Material.JIGSAW,
                        Material.LIGHT,
                        Material.DEBUG_STICK
                ));
            }

            Map<String, Object> enchantmentLimitMap = (Map<String, Object>) config.getConfigurationSection("EnchantmentLimits").getValues(false);
            if (enchantmentLimitMap != null) {
                enchantmentLimits = enchantmentLimitMap.entrySet().stream()
                        .collect(Collectors.toMap(
                                entry -> Enchantment.getByName(entry.getKey()),
                                entry -> (Integer) entry.getValue()
                        ));
                enchantmentLimits.entrySet().removeIf(entry -> entry.getKey() == null); 
                plugin.getLogger().info("Enchantment limits loaded: " + enchantmentLimits);
            } else {
                plugin.getLogger().warning("EnchantmentLimits section is missing or invalid in config.yml, using default values.");
                enchantmentLimits = new HashMap<>();
                enchantmentLimits.put(Enchantment.PROTECTION, 4);
                enchantmentLimits.put(Enchantment.FIRE_PROTECTION, 4);
                enchantmentLimits.put(Enchantment.FEATHER_FALLING, 4);
                enchantmentLimits.put(Enchantment.BLAST_PROTECTION, 4);
                enchantmentLimits.put(Enchantment.PROJECTILE_PROTECTION, 4);
                enchantmentLimits.put(Enchantment.RESPIRATION, 3);
                enchantmentLimits.put(Enchantment.AQUA_AFFINITY, 1);
                enchantmentLimits.put(Enchantment.THORNS, 3);
                enchantmentLimits.put(Enchantment.DEPTH_STRIDER, 3);
                enchantmentLimits.put(Enchantment.FROST_WALKER, 2);
                enchantmentLimits.put(Enchantment.BINDING_CURSE, 1);
                enchantmentLimits.put(Enchantment.SHARPNESS, 5);
                enchantmentLimits.put(Enchantment.SMITE, 5);
                enchantmentLimits.put(Enchantment.BANE_OF_ARTHROPODS, 5);
                enchantmentLimits.put(Enchantment.KNOCKBACK, 2);
                enchantmentLimits.put(Enchantment.FIRE_ASPECT, 2);
                enchantmentLimits.put(Enchantment.LOOTING, 3);
                enchantmentLimits.put(Enchantment.SWEEPING_EDGE, 3);
                enchantmentLimits.put(Enchantment.EFFICIENCY, 5);
                enchantmentLimits.put(Enchantment.SILK_TOUCH, 1);
                enchantmentLimits.put(Enchantment.UNBREAKING, 3);
                enchantmentLimits.put(Enchantment.FORTUNE, 3);
                enchantmentLimits.put(Enchantment.POWER, 5);
                enchantmentLimits.put(Enchantment.PUNCH, 2);
                enchantmentLimits.put(Enchantment.FLAME, 1);
                enchantmentLimits.put(Enchantment.INFINITY, 1);
                enchantmentLimits.put(Enchantment.LUCK_OF_THE_SEA, 3);
                enchantmentLimits.put(Enchantment.LURE, 3);
                enchantmentLimits.put(Enchantment.MENDING, 1);
                enchantmentLimits.put(Enchantment.VANISHING_CURSE, 1);
            }

        } catch (Exception e) { 
            plugin.getLogger().severe("Error loading config.yml: " + e.getMessage());
            allowedCommands = new ArrayList<>();
            allowedOPs = new ArrayList<>();
            joinMessages = new ArrayList<>();
            leaveMessages = new ArrayList<>();
            illegalMaterials = new HashSet<>(Arrays.asList(
                    Material.BEDROCK,
                    Material.BARRIER,
                    Material.COMMAND_BLOCK,
                    Material.STRUCTURE_BLOCK,
                    Material.JIGSAW,
                    Material.LIGHT,
                    Material.DEBUG_STICK
            ));
            enchantmentLimits = new HashMap<>();
            enchantmentLimits.put(Enchantment.PROTECTION, 4);
            enchantmentLimits.put(Enchantment.FIRE_PROTECTION, 4);
            enchantmentLimits.put(Enchantment.FEATHER_FALLING, 4);
            enchantmentLimits.put(Enchantment.BLAST_PROTECTION, 4);
            enchantmentLimits.put(Enchantment.PROJECTILE_PROTECTION, 4);
            enchantmentLimits.put(Enchantment.RESPIRATION, 3);
            enchantmentLimits.put(Enchantment.AQUA_AFFINITY, 1);
            enchantmentLimits.put(Enchantment.THORNS, 3);
            enchantmentLimits.put(Enchantment.DEPTH_STRIDER, 3);
            enchantmentLimits.put(Enchantment.FROST_WALKER, 2);
            enchantmentLimits.put(Enchantment.BINDING_CURSE, 1);
            enchantmentLimits.put(Enchantment.SHARPNESS, 5);
            enchantmentLimits.put(Enchantment.SMITE, 5);
            enchantmentLimits.put(Enchantment.BANE_OF_ARTHROPODS, 5);
            enchantmentLimits.put(Enchantment.KNOCKBACK, 2);
            enchantmentLimits.put(Enchantment.FIRE_ASPECT, 2);
            enchantmentLimits.put(Enchantment.LOOTING, 3);
            enchantmentLimits.put(Enchantment.SWEEPING_EDGE, 3);
            enchantmentLimits.put(Enchantment.EFFICIENCY, 5);
            enchantmentLimits.put(Enchantment.SILK_TOUCH, 1);
            enchantmentLimits.put(Enchantment.UNBREAKING, 3);
            enchantmentLimits.put(Enchantment.FORTUNE, 3);
            enchantmentLimits.put(Enchantment.POWER, 5);
            enchantmentLimits.put(Enchantment.PUNCH, 2);
            enchantmentLimits.put(Enchantment.FLAME, 1);
            enchantmentLimits.put(Enchantment.INFINITY, 1);
            enchantmentLimits.put(Enchantment.LUCK_OF_THE_SEA, 3);
            enchantmentLimits.put(Enchantment.LURE, 3);
            enchantmentLimits.put(Enchantment.MENDING, 1);
            enchantmentLimits.put(Enchantment.VANISHING_CURSE, 1);
        }
        explosionBlocksPerTick = config.getInt("ExplosionBlocksPerTick", 20);
    }

    public List<String> getAllowedCommands() {
        return Collections.unmodifiableList(allowedCommands);
    }

    public List<String> getAllowedOPs() {
        return Collections.unmodifiableList(allowedOPs);
    }

    public List<String> getJoinMessages() {
        return Collections.unmodifiableList(joinMessages);
    }

    public List<String> getLeaveMessages() {
        return Collections.unmodifiableList(leaveMessages);
    }

    public Set<Material> getIllegalMaterials() {
        return Collections.unmodifiableSet(illegalMaterials);
    }

    public Map<Enchantment, Integer> getEnchantmentLimits() {
        return Collections.unmodifiableMap(enchantmentLimits);
    }

    public int getExplosionBlocksPerTick() {
        return explosionBlocksPerTick;
    }
}