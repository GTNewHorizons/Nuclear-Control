package shedar.mods.ic2.nuclearcontrol.containers;

import java.util.UUID;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import shedar.mods.ic2.nuclearcontrol.IC2NuclearControl;
import shedar.mods.ic2.nuclearcontrol.InventoryItem;
import shedar.mods.ic2.nuclearcontrol.SlotFilter;
import shedar.mods.ic2.nuclearcontrol.api.IPanelDataSource;
import shedar.mods.ic2.nuclearcontrol.items.ItemRemoteMonitor;

public class ContainerRemoteMonitor extends Container {

    protected final ItemStack is;
    public final InventoryItem item;

    private static final String NBT_KEY_UID = "UID";
    private final ItemStack itemStack;
    private final int itemSlot;

    private static boolean isSameItemInventory(ItemStack base, ItemStack comparison) {
        if (base == null || comparison == null) return false;

        if (base.getItem() != comparison.getItem()) return false;

        if (!base.hasTagCompound() || !comparison.hasTagCompound()) return false;

        String baseUID = base.getTagCompound().getString(NBT_KEY_UID);
        String comparisonUID = comparison.getTagCompound().getString(NBT_KEY_UID);
        return baseUID != null && comparisonUID != null && baseUID.equals(comparisonUID);
    }

    public ContainerRemoteMonitor(InventoryPlayer inv, ItemStack stack, InventoryItem iItem) {
        this.is = stack;
        this.item = iItem;

        Slot slot = this.addSlotToContainer(new SlotFilter(this.item, 0, 177, 21));

        this.itemStack = stack;
        this.itemSlot = slot.slotNumber;
        if (stack != null && stack.getItem() instanceof ItemRemoteMonitor) {
            if (!stack.hasTagCompound()) stack.setTagCompound(new NBTTagCompound());
            NBTTagCompound nbt = stack.getTagCompound();
            if (!nbt.hasKey(NBT_KEY_UID)) nbt.setString(NBT_KEY_UID, UUID.randomUUID().toString());
        }

        this.bindPlayerInventory(inv);
    }

    protected void bindPlayerInventory(InventoryPlayer inventoryPlayer) {
        /*
         * for (int i = 0; i < 3; i++) { for (int j = 0; j < 9; j++) { addSlotToContainer(new Slot(inventoryPlayer, j +
         * i * 9 + 9, 8 + j * 18, 84 + i * 18)); } }
         */

        for (int i = 0; i < 9; i++) {
            addSlotToContainer(new Slot(inventoryPlayer, i, 8 + i * 18, 142));
        }
    }

    @Override
    public boolean canInteractWith(EntityPlayer player) {
        return isSameItemInventory(player.getHeldItem(), this.itemStack);
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer player, int slot) {
        if (slot == this.itemSlot) return null;
        if (!isSameItemInventory(player.getHeldItem(), this.itemStack)) return null;

        ItemStack stack = null;
        Slot slots = (Slot) this.inventorySlots.get(slot);

        if (slots.getStack() != null)
            if (slots.getStack().getItem() == IC2NuclearControl.itemRemoteMonitor) return null;

        if (slots != null && slots.getHasStack()) {
            ItemStack itemstackR = slots.getStack();
            stack = itemstackR.copy();

            if (slot == 0) {
                boolean fixed = false;
                for (int h = 1; h < 10; h++) {
                    Slot know = (Slot) this.inventorySlots.get(h);
                    if (!know.getHasStack()) {
                        know.putStack(slots.getStack());
                        slots.decrStackSize(1);
                        fixed = true;
                    }
                }
                if (!fixed) return null;
                slots.onSlotChange(itemstackR, stack);
            } else if (slots.getStack().getItem() instanceof IPanelDataSource
                    && !((Slot) this.inventorySlots.get(0)).getHasStack()) {
                        ((Slot) this.inventorySlots.get(0)).putStack(itemstackR);
                        slots.decrStackSize(1);
                        slots.onSlotChange(itemstackR, stack);
                        ((Slot) this.inventorySlots.get(0)).onSlotChanged();
                    } else
                return null;
        }
        return stack;
    }

    @Override
    public ItemStack slotClick(int slot, int button, int flag, EntityPlayer player) {
        if (slot == this.itemSlot) return null;
        if (flag == 2 && button == this.itemSlot) return null;
        if (!isSameItemInventory(player.getHeldItem(), this.itemStack)) return null;

        if (slot >= 0 && this.getSlot(slot) != null && this.getSlot(slot).getStack() == player.getHeldItem())
            return null;
        return super.slotClick(slot, button, flag, player);
    }
}
