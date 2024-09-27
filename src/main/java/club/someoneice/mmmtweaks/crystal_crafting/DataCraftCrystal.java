package club.someoneice.mmmtweaks.crystal_crafting;

import club.someoneice.togocup.tags.Ingredient;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import java.util.Objects;


@SuppressWarnings("unused")
public final class DataCraftCrystal {
    private ItemStack itemStack;
    private int itemId;
    private int meta;
    private ImmutableMap<ImmutableList<Ingredient>, ItemStack> caches;

    public DataCraftCrystal(int itemId, int meta, ImmutableMap<ImmutableList<Ingredient>, ItemStack> caches) {
        this.itemStack = new ItemStack(Item.getItemById(itemId), 1, meta);
        this.itemId = itemId;
        this.meta = meta;
        this.caches = caches;
    }

    public DataCraftCrystal(ItemStack item) {
        this.itemStack = item;
        this.itemId = Item.getIdFromItem(item.getItem());
        this.meta = item.getItemDamage();
        this.caches = CraftingHelper.findCacheOrRegister(item);
    }

    public ItemStack getItemStack() {
        return itemStack;
    }

    public void setItemStack(ItemStack itemStack) {
        this.itemStack = itemStack;
    }

    public int getItemId() {
        return itemId;
    }

    public void setItemId(int itemId) {
        this.itemId = itemId;
    }

    public int getMeta() {
        return meta;
    }

    public void setMeta(int meta) {
        this.meta = meta;
    }

    public ImmutableMap<ImmutableList<Ingredient>, ItemStack> getCaches() {
        return caches;
    }

    public void setCaches(ImmutableMap<ImmutableList<Ingredient>, ItemStack> caches) {
        this.caches = caches;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DataCraftCrystal that = (DataCraftCrystal) o;
        return itemId == that.itemId && meta == that.meta && Objects.equals(caches, that.caches);
    }

    @Override
    public int hashCode() {
        return Objects.hash(itemId, meta, caches);
    }

    @Override
    public String toString() {
        return "DataCraftCrystal{" +
                "itemId=" + itemId +
                ", meta=" + meta +
                ", caches=" + caches +
                '}';
    }
}
