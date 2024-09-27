package club.someoneice.mmmtweaks.crystal_crafting;

import club.someoneice.cookie.util.ObjectUtil;
import club.someoneice.json.Pair;
import club.someoneice.pineapplepsychic.util.Util;
import club.someoneice.togocup.tags.Ingredient;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@SuppressWarnings({"unchecked", "all"})
public final class CraftingHelper {
    static final Cache<Integer, HashMap<Integer, ImmutableMap<ImmutableList<Ingredient>, ItemStack>>> RECIPE_CACHE_POOL = CacheBuilder.newBuilder()
            .maximumSize(50)
            .expireAfterWrite(10, TimeUnit.MINUTES)
            // .refreshAfterWrite(15, TimeUnit.MINUTES)
            .softValues()
            .build();

    // static final Table<Integer, Integer, HashMap<ImmutableList<Ingredient>, ItemStack>> RECIPE_CACHE_POOL = HashBasedTable.create();
    static final HashMap<ChunkPosition, DataCraftCrystal> RECIPE_CRYSTAL_CACHE = Maps.newHashMap();

    private CraftingHelper() {}

    public static DataCraftCrystal findCrystalData(final ChunkPosition pos, final ItemStack item) {
        DataCraftCrystal dataCache = RECIPE_CRYSTAL_CACHE.get(pos);

        if (Objects.nonNull(dataCache) && Util.itemStackEquals(dataCache.getItemStack(), item)) {
            return dataCache;
        }

        return ObjectUtil.objectDo(new DataCraftCrystal(item), it -> RECIPE_CRYSTAL_CACHE.put(pos, it));
    }

    public static ImmutableMap<ImmutableList<Ingredient>, ItemStack> findCacheOrRegister(final ItemStack item) {
        int id = Item.getIdFromItem(item.getItem());
        int meta = item.getItemDamage();

        HashMap<Integer, ImmutableMap<ImmutableList<Ingredient>, ItemStack>> cache = RECIPE_CACHE_POOL.getIfPresent(id);

        if (Objects.isNull(cache)) {
            return registerCache(item);
        }

        ImmutableMap<ImmutableList<Ingredient>, ItemStack> cached = cache.get(meta);
        if (Objects.nonNull(cached)) {
            return cached;
        }

        return registerCache(item);
    }

    public static Ingredient getIngredientByOreDictionary(final String oreDictionary) {
        return new Ingredient(OreDictionary.getOres(oreDictionary));
    }

    public static ImmutableMap<ImmutableList<Ingredient>, ItemStack> registerCache(ItemStack item) {
        int id = Item.getIdFromItem(item.getItem());
        int meta = item.getItemDamage();

        ImmutableMap<ImmutableList<Ingredient>, ItemStack> recipes = findRecipeByItemStack(item);

        RECIPE_CACHE_POOL.put(id, ObjectUtil.objectDo(RECIPE_CACHE_POOL.getIfPresent(id), it -> {
            if (Objects.isNull(it)) {
                it = Maps.newHashMap();
            }

            it.put(meta, recipes);
            return it;
        }));

        return recipes;
    }

    public static ImmutableMap<ImmutableList<Ingredient>, ItemStack> findRecipeByItemStack(final ItemStack recipeOut) {
        List<IRecipe> recipeList = ((List<IRecipe>) CraftingManager.getInstance().getRecipeList()).stream()
                .filter(it -> Util.itemStackEquals(it.getRecipeOutput(), recipeOut))
                .collect(Collectors.toList());

        if (recipeList.isEmpty()) {
            return ImmutableMap.of();
        }

        ImmutableMap.Builder<ImmutableList<Ingredient>, ItemStack> recipes = ImmutableMap.builder();
        recipeList.stream().map(CraftingHelper::getInputs).forEach(pair -> recipes.put(pair.getKey(), pair.getValue().copy()));
        return recipes.build();
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
