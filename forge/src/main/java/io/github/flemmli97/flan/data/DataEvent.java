package io.github.flemmli97.flan.data;

import io.github.flemmli97.flan.Flan;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Flan.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class DataEvent {

    @SubscribeEvent
    public static void data(GatherDataEvent event) {
        DataGenerator data = event.getGenerator();
        PermissionGen permissionGen = new PermissionGen(data.getPackOutput());
        data.addProvider(event.includeServer(), permissionGen);
        data.addProvider(event.includeServer(), new InteractionOverrideGen(data.getPackOutput()));
        ENLangGen enLang = new ENLangGen(data.getPackOutput(), permissionGen);
        data.addProvider(event.includeServer(), enLang);
    }

}
