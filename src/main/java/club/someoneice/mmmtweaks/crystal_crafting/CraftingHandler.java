package club.someoneice.mmmtweaks.crystal_crafting;

import club.someoneice.json.Pair;
import club.someoneice.mmmtweaks.Config;
import club.someoneice.mmmtweaks.ModMain;
import club.someoneice.pineapplepsychic.util.MatchUtil;
import club.someoneice.pineapplepsychic.util.Util;
import club.someoneice.togocup.tags.Ingredient;
import com.google.common.collect.*;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.world.ChunkPosition;
import project.studio.manametalmod.MMM;
import project.studio.manametalmod.tileentity.TileEntityCrystalZ;

import java.util.*;

@SuppressWarnings("unused")
public final class CraftingHandler {
    private CraftingHandler() {
    }

    public static void checkRecipe(TileEntityCrystalZ tile, int time) {
        if (!tile.open || time % 20 != 0 || tile.getWorldObj().isRemote) {
            return;
        }

        if (tile.target == null || tile.energy.getEnergy() < 3) {
            return;
        }

        IInventory targetBox = tile.getTarget();
        if (targetBox == null) {
            return;
        }

        ChunkPosition pos = new ChunkPosition(tile.xCoord, tile.yCoord, tile.zCoord);
        DataCraftCrystal data = CraftingHelper.findCrystalData(pos, tile.target);
        for (Pair<ImmutableList<Ingredient>, ItemStack> dataCach : data.getCaches()) {
            if (scanInventory(tile, targetBox, dataCach)) {
                return;
            }
        }
    }

    private static boolean scanInventory(TileEntityCrystalZ tile, IInventory inventory, Pair<ImmutableList<Ingredient>, ItemStack> data) {
        Set<Integer> caches = Sets.newHashSet();
        Multiset<String> itemInputCaches = HashMultiset.create();
        Multiset<String> itemUsedCounter = HashMultiset.create();

        for (int i = 0; i < data.getKey().size(); i++) {
            caches.add(i);
        }

        for (int slot = 0; slot < inventory.getSizeInventory(); slot++) {
            ItemStack stack = inventory.getStackInSlot(slot);
            if (stack == null) {
                continue;
            }

            itemInputCaches.add(ModMain.codecItemStack(stack.copy()), stack.stackSize);
        }

        Iterator<Integer> iterator = caches.iterator();
        while (iterator.hasNext()) {
            int index = iterator.next();
            Ingredient ingredient = data.getKey().get(index);
            for (String stack : itemInputCaches.elementSet()) {
                if (MatchUtil.matchItemStackInIngredient(ingredient, ModMain.decodecItemStack(stack))) {
                    itemUsedCounter.add(stack);
                    iterator.remove();
                }
            }
        }

        if (caches.isEmpty()) {
            crafting(tile, inventory, itemUsedCounter, HashMultiset.create(), data.getValue().copy());
            return true;
        }

        if (!Config.SHOULD_DEPTH_SCAN_RECIPE) {
            return false;
        }

        Multiset<String> craftOut = HashMultiset.create();
        for (Integer slotIn : caches) {
            Ingredient rp = data.getKey().get(slotIn);
            if (!depthScan(itemInputCaches, itemUsedCounter, rp, Sets.newHashSet(), 0, craftOut)) {
                return false;
            }
        }

        crafting(tile, inventory, itemUsedCounter, craftOut, data.getValue().copy());
        return true;
    }

    private static void crafting(TileEntityCrystalZ tile, IInventory inventory, Multiset<String> use, Multiset<String> craftOut, ItemStack output) {
        List<ItemStack> itemReturn = Lists.newArrayList();

        for (int slot = 0; slot < inventory.getSizeInventory(); slot++) {
            ItemStack stack = inventory.getStackInSlot(slot);
            if (stack == null) {
                continue;
            }
            String codecStack = ModMain.codecItemStack(stack);
            if (!use.contains(codecStack)) {
                continue;
            }

            int count = use.count(codecStack);
            if (count > stack.stackSize) {
                int size = stack.stackSize;
                inventory.setInventorySlotContents(slot, null);
                use.remove(stack, size);
                if (stack.getItem().hasContainerItem(stack)) {
                    ItemStack item = stack.getItem().getContainerItem(stack).copy();
                    item.stackSize *= size;
                    itemReturn.add(item.copy());
                }
            } else {
                stack.stackSize -= count;
                if (stack.stackSize == 0) {
                    inventory.setInventorySlotContents(slot, null);
                }
                use.remove(codecStack);
                if (stack.getItem().hasContainerItem(stack)) {
                    ItemStack item = stack.getItem().getContainerItem(stack).copy();
                    item.stackSize *= count;
                    itemReturn.add(item);
                }
            }
        }

        for (String stack : craftOut) {
            if (!use.contains(stack)) {
                continue;
            }

            int count = use.count(stack);
            int craftCount = craftOut.count(stack);
            ItemStack deStack = ModMain.decodecItemStack(stack);
            if (count > craftCount) {
                craftOut.remove(stack);
                use.remove(stack, craftCount);

                if (deStack.getItem().hasContainerItem(deStack)) {
                    ItemStack item = deStack.getItem().getContainerItem(deStack).copy();
                    item.stackSize *= craftCount;
                    itemReturn.add(item);
                }
            } else {
                craftOut.remove(stack, count);
                use.remove(stack);
                if (deStack.getItem().hasContainerItem(deStack)) {
                    ItemStack item = deStack.getItem().getContainerItem(deStack).copy();
                    item.stackSize *= count;
                    itemReturn.add(item);
                }
            }
        }

        ChunkPosition chunkPosition = new ChunkPosition(tile.xCoord, tile.yCoord + 1, tile.zCoord);
        Util.itemThrowOut(tile.getWorldObj(), chunkPosition, output);

        itemReturn.forEach(it -> {
            if (!MMM.tryInsertStack(inventory, it))
                Util.itemThrowOut(tile.getWorldObj(), chunkPosition, it);
        });
        craftOut.forEach(it -> {
            ItemStack out = ModMain.decodecItemStack(it);
            if (!MMM.tryInsertStack(inventory, out))
                Util.itemThrowOut(tile.getWorldObj(), chunkPosition, out);
        });
    }

    private static boolean depthScan(Multiset<String> input, Multiset<String> output, Ingredient recipe, Set<String> parents, int depth, Multiset<String> craftOut) {
        if (depth > 3) {
            return false;
        }

        Iterator<ItemStack> iterator = recipe.getObj().iterator();
        while (iterator.hasNext()) {
            ItemStack stack = iterator.next();
            if (parents.contains(ModMain.codecItemStack(stack))) {
                continue;
            }
            HashMap<ImmutableList<Ingredient>, ItemStack> cache = CraftingHelper.findCacheOrRegister(stack);
            for (Map.Entry<ImmutableList<Ingredient>, ItemStack> entry : cache.entrySet()) {
                List<Ingredient> ingredients = Lists.newArrayList(entry.getKey());
                Iterator<Ingredient> ingredientIterator = ingredients.iterator();
                Multiset<String> itemUsedCounter = HashMultiset.create();

                while (ingredientIterator.hasNext()) {
                    Ingredient ingredient = ingredientIterator.next();
                    for (String itemStack : input.elementSet()) {
                        if (!MatchUtil.matchItemStackInIngredient(ingredient, ModMain.decodecItemStack(itemStack))) {
                            continue;
                        }

                        parents.add(itemStack);
                        itemUsedCounter.add(itemStack);
                        iterator.remove();
                        break;
                    }
                }

                if (ingredients.isEmpty()) {
                    String codecOutput = ModMain.codecItemStack(entry.getValue().copy());
                    input.add(codecOutput, entry.getValue().stackSize);
                    craftOut.add(codecOutput, entry.getValue().stackSize);
                    output.addAll(itemUsedCounter);
                    return true;
                }

                ingredientIterator = ingredients.iterator();
                while (ingredientIterator.hasNext()) {
                    Ingredient ingredient = ingredientIterator.next();
                    if (!depthScan(input, output, ingredient, parents, depth + 1, craftOut)) {
                        return false;
                    }
                }

                String codecOutput = ModMain.codecItemStack(entry.getValue().copy());
                input.add(codecOutput, entry.getValue().stackSize);
                craftOut.add(codecOutput, entry.getValue().stackSize);
                output.addAll(itemUsedCounter);
                return true;
            }
        }

        return false;
    }

    public static boolean hasItem(Collection<ItemStack> collection, ItemStack item) {
        return collection.stream().anyMatch(it -> Util.itemStackEquals(it, item));
    }
}
