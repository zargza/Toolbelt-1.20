package dev.gigaherz.toolbelt.customslots;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandlerModifiable;

import javax.annotation.Nonnull;

public class ExtensionSlotItemHandler implements IExtensionSlot
{
    protected final IExtensionContainer owner;
    protected final ResourceLocation slotType;
    protected final int slot;
    protected final IItemHandlerModifiable inventory;

    public ExtensionSlotItemHandler(IExtensionContainer owner, ResourceLocation slotType, IItemHandlerModifiable inventory, int slot)
    {
        this.owner = owner;
        this.slotType = slotType;
        this.slot = slot;
        this.inventory = inventory;
    }

    @Nonnull
    @Override
    public IExtensionContainer getContainer()
    {
        return owner;
    }

    @Nonnull
    @Override
    public ResourceLocation getType()
    {
        return slotType;
    }

    /**
     * @return The contents of the slot. The stack is *NOT* required to be of an IExtensionSlotItem!
     */
    @Nonnull
    @Override
    public ItemStack getContents()
    {
        return inventory.getStackInSlot(slot);
    }

    @Override
    public void setContents(@Nonnull ItemStack stack)
    {
        ItemStack oldStack = getContents();
        if (oldStack == stack) return;
        if (!oldStack.isEmpty())
            notifyUnequip(oldStack);
        inventory.setStackInSlot(slot, stack);
        if (!stack.isEmpty())
            notifyEquip(stack);
    }

    @Override
    public void onContentsChanged()
    {
        owner.onContentsChanged(this);
    }

    private void notifyEquip(ItemStack stack)
    {
        stack.getCapability(ExtensionSlotItemCapability.INSTANCE, null).ifPresent((extItem) -> {
            extItem.onEquipped(stack, this);
        });
    }

    private void notifyUnequip(ItemStack stack)
    {
        stack.getCapability(ExtensionSlotItemCapability.INSTANCE, null).ifPresent((extItem) -> {
            extItem.onUnequipped(stack, this);
        });
    }

    public void onWornTick()
    {
        ItemStack stack = getContents();
        if (stack.isEmpty())
            return;
        stack.getCapability(ExtensionSlotItemCapability.INSTANCE, null).ifPresent((extItem) -> {
            extItem.onWornTick(stack, this);
        });
    }
}
