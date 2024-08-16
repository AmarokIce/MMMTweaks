package club.someoneice.mmmtweaks.crystal_crafting;

import club.someoneice.json.Pair;
import club.someoneice.json.PairList;
import club.someoneice.pineapplepsychic.util.Util;
import club.someoneice.togocup.tags.Ingredient;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.google.common.collect.Table;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.ShapedRecipes;
import net.minecraft.item.crafting.ShapelessRecipes;
import net.minecraft.world.ChunkPosition;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.oredict.ShapedOreRecipe;
import net.minecraftforge.oredict.ShapelessOreRecipe;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

@SuppressWarnings("unchecked")
public final class CraftingHelper {
    static final Table<Integer, Integer, HashMap<ImmutableList<Ingredient>, ItemStack>> RECIPE_CACHE_POOL = HashBasedTable.create();
    static final PairList<ChunkPosition, DataCraftCrystal> RECIPE_CRYSTAL_CACHE = new PairList<>();

    private CraftingHelper() {
    }

    public static DataCraftCrystal findCrystalData(final ChunkPosition pos, final ItemStack item) {
        final Optional<Pair<ChunkPosition, DataCraftCrystal>> data = RECIPE_CRYSTAL_CACHE
                .stream()
                .filter(pair -> pair.getKey().equals(pos))
                .findFirst();
        if (data.isPresent()) {
            DataCraftCrystal dat = data.get().getValue();
            if (Util.itemStackEquals(dat.getItemStack(), item)) {
                return dat;
            }
        }

        DataCraftCrystal dataCraftCrystal = new DataCraftCrystal(item);
        RECIPE_CRYSTAL_CACHE.put(pos, dataCraftCrystal);
        return dataCraftCrystal;
    }

    public static HashMap<ImmutableList<Ingredient>, ItemStack> findCacheOrRegister(final ItemStack item) {
        int id = Item.getIdFromItem(item.getItem());
        int meta = item.getItemDamage();

        if (RECIPE_CACHE_POOL.contains(id, meta)) {
            return RECIPE_CACHE_POOL.get(id, meta);
        }

        return registerCache(item);
    }

    public static Ingredient getIngredientByOreDictionary(final String oreDictionary) {
        return new Ingredient(OreDictionary.getOres(oreDictionary));
    }

    public static HashMap<ImmutableList<Ingredient>, ItemStack> registerCache(ItemStack item) {
        int id = Item.getIdFromItem(item.getItem());
        int meta = item.getItemDamage();
        HashMap<ImmutableList<Ingredient>, ItemStack> recipes = findRecipeByItemStack(item);

        RECIPE_CACHE_POOL.put(id, meta, recipes);
        return recipes;
    }

    public static HashMap<ImmutableList<Ingredient>, ItemStack> findRecipeByItemStack(final ItemStack recipeOut) {
        List<IRecipe> recipeList = ((List<IRecipe>) CraftingManager.getInstance().getRecipeList()).stream()
                .filter(it -> Util.itemStackEquals(it.getRecipeOutput(), recipeOut))
                .collect(Collectors.toList());
        if (recipeList.isEmpty()) {
            return Maps.newHashMap();
        }

        HashMap<ImmutableList<Ingredient>, ItemStack> recipes = Maps.newHashMap();
        recipeList.stream().map(CraftingHelper::getInputs).forEach(pair -> recipes.put(pair.getKey(), pair.getValue().copy()));
        return recipes;
    }

    @Nullable
    private static Ingredient getIngredientByOreDictionaryObject(final Object item) {
        if (item instanceof ItemStack) {
            return new Ingredient((ItemStack) item);
        } else if (item instanceof List) {
            // Will fix
            List<ItemStack> list = (List<ItemStack>) item;
            list.removeIf(Objects::isNull);
            ItemStack[] items = new ItemStack[list.size()];
            for (int i = 0; i < list.size(); i++) {
                items[i] = list.get(i);
            }
            return new Ingredient(items);
        }

        return null;
    }

    private static Pair<ImmutableList<Ingredient>, ItemStack> getInputs(final IRecipe recipeInput) {
        final ImmutableList.Builder<Ingredient> builder = ImmutableList.builder();

        if (recipeInput instanceof ShapedRecipes) {
            ShapedRecipes subRecipe = ((ShapedRecipes) recipeInput);
            Arrays.stream(subRecipe.recipeItems).forEach(item -> builder.add(new Ingredient(item)));

            return new Pair<>(builder.build(), recipeInput.getRecipeOutput().copy());
        }

        if (recipeInput instanceof ShapelessRecipes) {
            ShapelessRecipes subRecipe = ((ShapelessRecipes) recipeInput);
            ((List<ItemStack>) subRecipe.recipeItems).forEach(item -> builder.add(new Ingredient(item)));

            return new Pair<>(builder.build(), recipeInput.getRecipeOutput().copy());
        }

        if (recipeInput instanceof ShapedOreRecipe) {
            ShapedOreRecipe subRecipe = ((ShapedOreRecipe) recipeInput);
            Arrays.stream(subRecipe.getInput()).forEach(obj -> {
                Ingredient ingredient = getIngredientByOreDictionaryObject(obj);
                if (ingredient != null) {
                    builder.add(ingredient);
                }
            });

            return new Pair<>(builder.build(), recipeInput.getRecipeOutput().copy());
        }

        if (recipeInput instanceof ShapelessOreRecipe) {
            ShapelessOreRecipe subRecipe = ((ShapelessOreRecipe) recipeInput);
            subRecipe.getInput().forEach(obj -> {
                Ingredient ingredient = getIngredientByOreDictionaryObject(obj);
                if (ingredient != null) {
                    builder.add(ingredient);
                }
            });

            return new Pair<>(builder.build(), recipeInput.getRecipeOutput().copy());
        }

        return new Pair<>(builder.build(), recipeInput.getRecipeOutput().copy());
    }
}
