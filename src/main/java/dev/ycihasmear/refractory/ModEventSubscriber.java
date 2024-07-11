package dev.ycihasmear.refractory;

import dev.ycihasmear.refractory.util.DebugCommands;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Refractory.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ModEventSubscriber {
    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        DebugCommands.register(event.getDispatcher());
    }
}
