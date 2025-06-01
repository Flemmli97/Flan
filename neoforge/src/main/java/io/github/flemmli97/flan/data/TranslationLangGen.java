package io.github.flemmli97.flan.data;

import io.github.flemmli97.flan.Flan;
import io.github.flemmli97.linguabib.api.ServerLangGen;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.data.event.GatherDataEvent;

import java.util.HashSet;
import java.util.Set;

/**
 * Lang gen for other language using en as verification
 * To use
 * 1. Extend this class and implement addTranslationsFor
 * 2. Add all translations
 * 3. Add the instance to {@link DataEvent#data(GatherDataEvent.Server)}
 */
public abstract class TranslationLangGen extends ServerLangGen {

    private final Set<String> keys = new HashSet<>();

    private final ENLangGen enLang;

    public TranslationLangGen(PackOutput output, String locale, ENLangGen enLang) {
        super(output, Flan.MODID, locale);
        this.enLang = enLang;
    }

    protected abstract void addTranslationsFor();

    @Override
    protected final void addTranslations() {
        this.addTranslationsFor();
        this.verify();
    }

    @Override
    public void add(String key, String value) {
        super.add(key, value);
        this.keys.add(key);
    }

    @Override
    public void add(String key, String... lines) {
        super.add(key, lines);
        this.keys.add(key);
    }

    protected void verify() {
        Set<String> missing = new HashSet<>();
        this.enLang.allKeys().forEach(key -> {
            if (!this.keys.contains(key))
                missing.add(key);
        });
        if (!missing.isEmpty()) {
            Flan.LOGGER.error("Missing translation for {}", missing);
        }
    }
}
