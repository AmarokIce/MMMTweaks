package club.someoneice.mmmtweaks.crystal_crafting;

import club.someoneice.json.Pair;
import club.someoneice.json.PairList;
import club.someoneice.togocup.tags.Ingredient;
import com.google.common.collect.ImmutableList;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import java.util.Objects;


@SuppressWarnings("unused")
public final class DataCraftCrystal {
    private ItemStack itemStack;
    private int itemId;
    private int meta;
    private PairList<ImmutableList<Ingredient>, ItemStack> caches;

    public DataCraftCrystal(int itemId, int meta, PairList<ImmutableList<Ingredient>, ItemStack> caches) {
        this.itemStack = new ItemStack(Item.getItemById(itemId), 1, meta);
        this.itemId = itemId;
        this.meta = meta;
        this.caches = caches;
    }

    public DataCraftCrystal(ItemStack item) {
        this.itemStack = item;
        this.itemId = Item.getIdFromItem(item.getItem());
        this.meta = item.getItemDamage();
        PairList<ImmutableList<Ingredient>, ItemStack> caches = new PairList<>();
        CraftingHelper.findCacheOrRegister(item).entrySet().stream()
                .map(enter -> new Pair<>(enter.getKey(), enter.getValue()))
                .forEach(caches::add);
        this.caches = caches;
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

    public PairList<ImmutableList<Ingredient>, ItemStack> getCaches() {
        return caches;
    }

    public void setCaches(PairList<ImmutableList<Ingredient>, ItemStack> caches) {
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
