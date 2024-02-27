package com.nukateam.gunscore.common.data.datagen;

import com.nukateam.gunscore.GunMod;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.common.data.LanguageProvider;

public class LanguageGen extends LanguageProvider {
    public LanguageGen(DataGenerator gen) {
        super(gen, GunMod.MOD_ID, "en_us");
    }

    @Override
    protected void addTranslations() {
    }
}
