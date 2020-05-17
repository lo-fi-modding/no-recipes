package lofimodding.norecipes;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.item.crafting.RecipeManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.InterModProcessEvent;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.function.Supplier;

@Mod(NoRecipesMod.MOD_ID)
public class NoRecipesMod {
  public static final String MOD_ID = "no-recipes";
  public static final Logger LOGGER = LogManager.getLogger();

  private final List<Predicate<IRecipe<?>>> recipesToRemove = new ArrayList<>();

  public NoRecipesMod() {
    Config.registerConfig();

    final IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
    modBus.addListener(this::processIMC);

    final IEventBus forgeBus = MinecraftForge.EVENT_BUS;
    forgeBus.addListener(this::serverStarting);
  }

  private void processIMC(final InterModProcessEvent event) {
    final Object2IntMap<String> removers = new Object2IntOpenHashMap<>();

    event.getIMCStream("remove_recipe"::equals).forEach(message -> {
      final Supplier<Predicate<IRecipe<?>>> predicate = message.getMessageSupplier();
      this.recipesToRemove.add(predicate.get());
      removers.mergeInt(message.getSenderModId(), 1, Integer::sum);
    });

    for(final Object2IntMap.Entry<String> entry : removers.object2IntEntrySet()) {
      LOGGER.info("{} registered {} recipe removers", entry.getKey(), entry.getIntValue());
    }
  }

  private void serverStarting(final FMLServerStartingEvent event) {
    if(!Config.ENABLED.get()) {
      LOGGER.info("No Recipes! is disabled");
      return;
    }

    LOGGER.info("No Recipes! is taking over the world");

    final RecipeManager recipeManager = event.getServer().getRecipeManager();

    final Map<IRecipeType<?>, Map<ResourceLocation, IRecipe<?>>> recipes = new HashMap<>();

    for(final IRecipe<?> recipe : recipeManager.getRecipes()) {
      if(this.recipesToRemove.stream().anyMatch(predicate -> predicate.test(recipe))) {
        continue;
      }

      recipes.computeIfAbsent(recipe.getType(), key -> new HashMap<>()).put(recipe.getId(), recipe);
    }

    recipeManager.recipes = recipes;
  }
}
