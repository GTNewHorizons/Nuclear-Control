package shedar.mods.ic2.nuclearcontrol.network.handler;

import net.minecraft.world.World;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import shedar.mods.ic2.nuclearcontrol.api.CardState;
import shedar.mods.ic2.nuclearcontrol.api.IPanelDataSource;
import shedar.mods.ic2.nuclearcontrol.api.IndexedItem;
import shedar.mods.ic2.nuclearcontrol.api.NBTCardLayout;
import shedar.mods.ic2.nuclearcontrol.network.message.PacketRemoteMonitor;

public class RemoteServerHandler implements IMessageHandler<PacketRemoteMonitor, IMessage> {

    @Override
    @SideOnly(Side.SERVER)
    public IMessage onMessage(PacketRemoteMonitor msg, MessageContext ctx) {
        World world = ctx.getServerHandler().playerEntity.worldObj;
        if (!(msg.stack.getItem() instanceof IPanelDataSource dataSource)) return null;

        IndexedItem<IPanelDataSource> indexedItem = new IndexedItem<>(0, msg.stack, dataSource);
        NBTCardLayout layout = dataSource.getLayout();
        layout.setItem(indexedItem);
        CardState state = dataSource.update(world, indexedItem, layout, Integer.MAX_VALUE);
        layout.setState(state);

        return new PacketRemoteMonitor(indexedItem.itemStack);
    }
}
