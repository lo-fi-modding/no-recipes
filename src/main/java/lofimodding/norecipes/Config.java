package lofimodding.norecipes;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;

public final class Config {
  private Config() { }

  private static final ForgeConfigSpec SERVER_CONFIG;

  private static final String GENERAL_CATEGORY = "general";
  public static final ForgeConfigSpec.BooleanValue ENABLED;

  static {
    final ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();

    builder.comment("General configuration").push(GENERAL_CATEGORY);

    ENABLED = builder
      .comment("Enables or disables the entire mod")
      .define("enabled", true);

    builder.pop();

    SERVER_CONFIG = builder.build();
  }

  static void registerConfig() {
    ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, SERVER_CONFIG);
  }
}
