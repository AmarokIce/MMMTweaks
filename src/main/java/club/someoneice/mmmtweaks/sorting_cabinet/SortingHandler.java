package club.someoneice.mmmtweaks.sorting_cabinet;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.ChunkPosition;
import net.minecraft.world.World;
import project.studio.manametalmod.MMM;
import project.studio.manametalmod.api.ILogisticsBox;
import project.studio.manametalmod.core.ItemType;

import java.util.List;

public class SortingHandler {
    public static void scanInventory(IInventory inventory, World world, ChunkPosition tilePos) {
        Multimap<ItemType, ChunkPosition> cache = findAndCache(world, tilePos);

        for (int i = 0; i < inventory.getSizeInventory(); i++) {
            ItemStack stack = inventory.getStackInSlot(i);
            if (stack == null) {
                continue;
            }

            for (ChunkPosition pos : cache.get(ItemType.Other)) {
                IInventory inv = (IInventory) world.getTileEntity(pos.chunkPosX, pos.chunkPosY, pos.chunkPosZ);
                if (!MMM.hasItemStack(stack, inv)) {
                    break;
                }

                if (MMM.tryInsertStack(inventory, stack)) {
                    break;
                }
            }

            if (stack.stackSize <= 0) {
                continue;
            }

            List<ItemType> itemTypes = ItemType.getTypeFromItem(stack);
            for (ItemType type : itemTypes) {
                if (!cache.containsKey(type)) {
                    continue;
                }

                for (ChunkPosition position : cache.get(type)) {
                    IInventory inv = (IInventory) world.getTileEntity(position.chunkPosX, position.chunkPosY, position.chunkPosZ);

                    if (MMM.tryInsertStack(inventory, stack)) {
                        break;
                    }
                }

                if (stack.stackSize <= 0) {
                    break;
                }
            }

            if (stack.stackSize <= 0) {
                break;
            }

            if (!cache.containsKey(ItemType.Any)) {
                break;
            }

            for (ChunkPosition position : cache.get(ItemType.Any)) {
                IInventory inv = (IInventory) world.getTileEntity(position.chunkPosX, position.chunkPosY, position.chunkPosZ);

                if (MMM.tryInsertStack(inventory, stack)) {
                    break;
                }
            }
        }
    }

    private static Multimap<ItemType, ChunkPosition> findAndCache(World world, ChunkPosition pos) {
        Multimap<ItemType, ChunkPosition> table = HashMultimap.create();

        for (int y = 0; y <= 3; y++) for (int x = -4; x <= 4; x++) for (int z = -4; z <= 4; z++) {
            int posX = pos.chunkPosX + x;
            int posY = pos.chunkPosY + y;
            int posZ = pos.chunkPosZ + z;

            TileEntity tile = world.getTileEntity(posX, posY, posZ);
            if (!(tile instanceof ILogisticsBox)) {
                continue;
            }

            ILogisticsBox box = (ILogisticsBox) tile;
            if (!box.canInput()) {
                continue;
            }

            table.put(box.getBoxType(), pos);
        }

        return table;
    }
}
