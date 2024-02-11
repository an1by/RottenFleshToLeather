package net.aniby.rottenfleshtoleather;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Furnace;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.*;
import org.bukkit.inventory.*;
import org.bukkit.plugin.java.JavaPlugin;

public final class RottenFleshToLeather extends JavaPlugin implements Listener {
    NamespacedKey recipeKey;
    int fleshAmount;
    int cookingTime;
    FurnaceRecipe pluginRecipe;

    @Override
    public void onEnable() {
        // Config init
        saveDefaultConfig();
        FileConfiguration config = getConfig();

        // Event listener
        getServer().getPluginManager().registerEvents(this, this);

        // Getting values
        fleshAmount = config.getInt("flesh_amount", 64);
        float experience = (float) config.getDouble("experience", 0.35);
        cookingTime = config.getInt("cooking_time", 218);

        // Creating recipe
        recipeKey = new NamespacedKey(this, "leather");
        ItemStack result = new ItemStack(Material.LEATHER);
        RecipeChoice input = new RecipeChoice.ExactChoice(
                new ItemStack(Material.ROTTEN_FLESH, fleshAmount)
        );
        pluginRecipe = new FurnaceRecipe(recipeKey, result, input, experience, cookingTime);

        // Adding recipe
        getServer().addRecipe(pluginRecipe);

        // Ad
        Bukkit.getLogger().info(ChatColor.YELLOW + "Plugin written by " + ChatColor.GOLD + "An1by");
        Bukkit.getLogger().info(ChatColor.RED + "Website " + ChatColor.WHITE + " - " + ChatColor.AQUA + "aniby.net");
        Bukkit.getLogger().info(ChatColor.RED + "Discord " + ChatColor.WHITE + " - " + ChatColor.AQUA + "@an1by");
    }

    @EventHandler
    void onMove(InventoryMoveItemEvent event) {
        if (event.getDestination() instanceof FurnaceInventory inventory) {
            Furnace furnace = inventory.getHolder();
            if (furnace != null) {

                ItemStack source = inventory.getSmelting();
                ItemStack input = event.getItem();
                if (source != null) {
                    if (source.getType() == Material.ROTTEN_FLESH && source.isSimilar(input)) {
                        if (source.getAmount() < Material.ROTTEN_FLESH.getMaxStackSize()) {
                            int amount = source.getAmount() + input.getAmount();

                            if (amount == fleshAmount) {
                                furnace.setRecipeUsedCount(pluginRecipe, 0);
                                furnace.setCookTimeTotal(cookingTime);
                                furnace.setCookTime((short) 0);
                                furnace.update();
                            }

                            source.setAmount(amount);
                            input.setAmount(0);
                            furnace.getInventory().setSmelting(source);
                        }
                    }
                } else if (input.getType() == Material.ROTTEN_FLESH) {
                    inventory.setSmelting(input);
                }
            }
        }
    }

    @EventHandler
    void onClick(InventoryClickEvent event) {
        InventoryView view = event.getView();
        FurnaceInventory inventory = null;
        for (int i = 0; i < view.countSlots(); i++) {
            if (view.getInventory(i) instanceof FurnaceInventory fi) {
                inventory = fi;
                break;
            }
        }
        if (inventory != null) {
            Furnace furnace = inventory.getHolder();
            if (furnace != null) {
                ItemStack source = inventory.getSmelting();
                if (source != null && source.getType() == Material.ROTTEN_FLESH) {
                    if (source.getAmount() < fleshAmount) {
                        furnace.setRecipeUsedCount(pluginRecipe, 0);
                        furnace.setCookTimeTotal(Integer.MAX_VALUE);
                        furnace.setCookTime((short) 0);
                        furnace.update();
                    } else {
                        if (furnace.getCookTimeTotal() == Integer.MAX_VALUE) {
                            furnace.setRecipeUsedCount(pluginRecipe, 0);
                            furnace.setCookTimeTotal(cookingTime);
                            furnace.setCookTime((short) 0);
                            furnace.update();
                        }
                    }
                }
            }
        }
    }

    @EventHandler
    void onBurn(FurnaceBurnEvent event) {
        Furnace furnace = (Furnace) event.getBlock().getState();
        FurnaceInventory inventory = furnace.getInventory();

        ItemStack source = inventory.getSmelting();
        if (source != null && source.getType() == Material.ROTTEN_FLESH) {
            if (source.getAmount() != fleshAmount) {
                event.setBurning(false);
                event.setBurnTime(0);
            }
        }
    }

    @EventHandler
    void onStartSmelt(FurnaceStartSmeltEvent event) {
        if (event.getRecipe() instanceof FurnaceRecipe recipe) {
            if (recipe.getKey().equals(pluginRecipe.getKey())) {
                Furnace furnace = (Furnace) event.getBlock().getState();
                FurnaceInventory inventory = furnace.getSnapshotInventory();
                ItemStack source = inventory.getSmelting();
                if (source != null && source.getAmount() != fleshAmount) {
                    event.setTotalCookTime(Integer.MAX_VALUE);
                }
            }
        }
    }

    @EventHandler
    void onSmelt(FurnaceSmeltEvent event) {
        Furnace furnace = (Furnace) event.getBlock().getState();

        ItemStack source = event.getSource();
        ItemStack result = event.getResult();
        if (source.getType() == Material.ROTTEN_FLESH && result.getType() == Material.LEATHER) {
            if (source.getAmount() != fleshAmount) {
                event.setCancelled(true);
            } else {
                source.setAmount(Math.max(source.getAmount() - fleshAmount, 0));
                furnace.getInventory().setSmelting(source);
            }
        }
    }

    @Override
    public void onDisable() {
        getServer().removeRecipe(recipeKey);
    }
}
