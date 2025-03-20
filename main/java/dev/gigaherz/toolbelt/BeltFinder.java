package dev.gigaherz.toolbelt;

import dev.gigaherz.toolbelt.belt.ToolBeltItem;
import net.minecraft.core.NonNullList;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;

import java.util.Optional;
import java.util.function.IntFunction;

public abstract class BeltFinder
{
    private static NonNullList<BeltFinder> instances = NonNullList.create();

    public static synchronized void addFinder(BeltFinder finder)
    {
        instances.add(0, finder);
    }

    public static Optional<? extends BeltGetter> findBelt(LivingEntity player)
    {
        return findBelt(player, false);
    }

    public static Optional<? extends BeltGetter> findBelt(LivingEntity player, boolean allowCosmetic)
    {
        return instances.stream()
                .map(f -> f.findStack(player, allowCosmetic))
                .filter(Optional::isPresent)
                .findFirst()
                .orElseGet(Optional::empty);
    }

    public static void sendSync(Player player)
    {
        findBelt(player).ifPresent(BeltGetter::syncToClients);
    }

    public static void setBeltFromPacket(Player player, String where, int slot, ItemStack stack)
    {
        for (BeltFinder finder : instances)
        {
            if (finder.getName().equals(where))
            {
                finder.getSlotFromId(player, slot).ifPresent(getter -> getter.setBelt(stack));
            }
        }
    }

    public abstract String getName();

    public abstract Optional<? extends BeltGetter> findStack(LivingEntity player, boolean allowCosmetic);

    protected Optional<BeltGetter> getSlotFromId(Player player, int slotId)
    {
        return Optional.empty();
    }

    protected final Optional<? extends BeltGetter> findBeltInInventory(IItemHandler inventory, IntFunction<? extends BeltGetter> getterFactory)
    {
        for (int i = 0; i < inventory.getSlots(); i++)
        {
            ItemStack inSlot = inventory.getStackInSlot(i);
            if (inSlot.getCount() > 0)
            {
                if (inSlot.getItem() instanceof ToolBeltItem)
                {
                    return Optional.of(getterFactory.apply(i));
                }
            }
        }
        return Optional.empty();
    }

    public interface BeltGetter
    {
        ItemStack getBelt();

        default void setBelt(ItemStack stack)
        {
            // Defaults to "do nothing"
        }

        default boolean isHidden()
        {
            return false;
        }

        void syncToClients();
    }
}
