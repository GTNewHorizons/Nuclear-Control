package shedar.mods.ic2.nuclearcontrol.crossmod.bigreactors.recipes;

import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

import ic2.api.item.IC2Items;
import ic2.api.recipe.Recipes;
import shedar.mods.ic2.nuclearcontrol.crossmod.bigreactors.CrossBigReactors;

public class BigReactorsRecipesNew {

    public static void addRecipes() {
        Recipes.advRecipes.addRecipe(
                new ItemStack(CrossBigReactors.kitRFsensor),
                "IT", "PD", 'I', "ingotYellorium", 'T', IC2Items.getItem("frequencyTransmitter"), 'P',
                Items.paper, 'D', "dyeRed");
        Recipes.advRecipes.addRecipe(
                new ItemStack(CrossBigReactors.ReactorInfoFetch),
                "BRB", "YCY", "BRB", 'B', "reactorCasing", 'R', "dustRedstone", 'Y', "ingotYellorium",
                'C', Items.comparator);
    }
}
