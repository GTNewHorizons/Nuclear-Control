package shedar.mods.ic2.nuclearcontrol.containers;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ICrafting;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

import shedar.mods.ic2.nuclearcontrol.SlotFilter;
import shedar.mods.ic2.nuclearcontrol.tileentities.TileEntityEnergyCounter;
import shedar.mods.ic2.nuclearcontrol.utils.NuclearNetworkHelper;

public class ContainerEnergyCounter extends Container {

    public final TileEntityEnergyCounter energyCounter;
    private final EntityPlayer player;
    private double lastCounter = -1;

    public ContainerEnergyCounter(EntityPlayer player, TileEntityEnergyCounter energyCounter) {
        super();

        this.energyCounter = energyCounter;
        this.player = player;

        // transformer upgrades
        addSlotToContainer(new SlotFilter(energyCounter, 0, 8, 18));

        // inventory
        for (int i = 0; i < 3; i++) {
            for (int k = 0; k < 9; k++) {
                addSlotToContainer(new Slot(player.inventory, k + i * 9 + 9, 8 + k * 18, 84 + i * 18));
            }
        }

        for (int j = 0; j < 9; j++) {
            addSlotToContainer(new Slot(player.inventory, j, 8 + j * 18, 142));
        }
    }

    public void addCraftingToCrafters(ICrafting crafting) {
        super.addCraftingToCrafters(crafting);
        NuclearNetworkHelper.sendEnergyCounterValue(energyCounter, crafting);
    }

    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();
        double counter = energyCounter.counter;
        for (Object crafter : crafters) {
            ICrafting crafting = (ICrafting) crafter;

            if (lastCounter != counter) {
                NuclearNetworkHelper.sendEnergyCounterValue(energyCounter, crafting);
            }
        }
        lastCounter = counter;
    }

    @Override
    public boolean canInteractWith(EntityPlayer var1) {
        return energyCounter.isUseableByPlayer(player);
    }

    @Override
    public ItemStack slotClick(int par1, int par2, int par3, EntityPlayer player) {
        ItemStack stack = super.slotClick(par1, par2, par3, player);
        energyCounter.markDirty();
        return stack;
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer p, int slotId) {
        Slot slot = (Slot) this.inventorySlots.get(slotId);
        if (slot != null) {
            ItemStack items = slot.getStack();
            if (items != null) {
                int initialCount = items.stackSize;
                if (slotId < energyCounter.getSizeInventory())// moving from counter to inventory
                {
                    mergeItemStack(items, energyCounter.getSizeInventory(), inventorySlots.size(), false);
                    if (items.stackSize == 0) {
                        slot.putStack(null);
                    } else {
                        slot.onSlotChanged();
                        if (initialCount != items.stackSize) return items;
                    }
                } else// moving from inventory to counter
                {
                    for (int i = 0; i < energyCounter.getSizeInventory(); i++) {
                        if (!energyCounter.isItemValid(i, items)) {
                            continue;
                        }
                        ItemStack targetStack = energyCounter.getStackInSlot(i);
                        if (targetStack == null) {
                            Slot targetSlot = (Slot) this.inventorySlots.get(i);
                            targetSlot.putStack(items);
                            slot.putStack(null);
                            break;
                        } else if (items.isStackable() && items.isItemEqual(targetStack)) {
                            mergeItemStack(items, i, i + 1, false);
                            if (items.stackSize == 0) {
                                slot.putStack(null);
                            } else {
                                slot.onSlotChanged();
                                if (initialCount != items.stackSize) return items;
                            }
                            break;
                        }

                    }
                }
            }
        }
        return null;
    }

}
