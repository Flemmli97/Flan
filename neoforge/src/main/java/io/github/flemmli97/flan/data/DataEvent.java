package io.github.flemmli97.flan.data;

import io.github.flemmli97.flan.Flan;
import net.minecraft.data.DataGenerator;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.data.event.GatherDataEvent;

@EventBusSubscriber(modid = Flan.MODID, bus = EventBusSubscriber.Bus.MOD)
public class DataEvent {

    @SubscribeEvent
    public static void data(GatherDataEvent.Server event) {
        DataGenerator data = event.getGenerator();
        PermissionGen permissionGen = new PermissionGen(data.getPackOutput(), event.getLookupProvider());
        data.addProvider(true, permissionGen);
        data.addProvider(true, new InteractionOverrideGen(data.getPackOutput()));
        ENLangGen enLang = new ENLangGen(data.getPackOutput(), permissionGen);
        data.addProvider(true, enLang);
    }

}
