package shedar.mods.ic2.nuclearcontrol.crossmod.bigreactors.recipes;

import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

import cpw.mods.fml.common.Loader;
import ic2.api.item.IC2Items;
import ic2.api.recipe.Recipes;
import shedar.mods.ic2.nuclearcontrol.crossmod.bigreactors.CrossBigReactors;

public class BigReactorsRecipesGregtech {

    public static void addRecipes() {
        if (Loader.isModLoaded("dreamcraft")) {
            return;
        }
        // TODO BR doesn't have any GT recipes
        Recipes.advRecipes.addRecipe(
                new ItemStack(CrossBigReactors.kitRFsensor),
                new Object[] { "IT", "PD", 'I', "ingotYellorium", 'T', IC2Items.getItem("frequencyTransmitter"), 'P',
                        Items.paper, 'D', "dyeRed" });
        Recipes.advRecipes.addRecipe(
                new ItemStack(CrossBigReactors.ReactorInfoFetch),
                new Object[] { "BRB", "YCY", "BRB", 'B', "reactorCasing", 'R', "dustRedstone", 'Y', "ingotYellorium",
                        'C', Items.comparator });
    }
}
