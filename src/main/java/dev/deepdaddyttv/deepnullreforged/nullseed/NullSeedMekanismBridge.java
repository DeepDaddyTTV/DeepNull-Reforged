package dev.deepdaddyttv.deepnullreforged.nullseed;

import dev.deepdaddyttv.deepnullreforged.integration.mekanism.MekanismCompat;
import dev.deepdaddyttv.deepnullreforged.inventory.StoredChemical;
import mekanism.api.MekanismAPI;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

final class NullSeedMekanismBridge {
    private NullSeedMekanismBridge() {
    }

    static List<NullSeedEntry> chemicalEntries() {
        HolderLookup.Provider registries = ServerLifecycleHooks.getCurrentServer() == null
                ? null
                : ServerLifecycleHooks.getCurrentServer().registryAccess();
        if (registries == null) {
            return List.of();
        }
        HolderLookup.RegistryLookup<Chemical> chemicals = registries.lookup(MekanismAPI.CHEMICAL_REGISTRY_NAME).orElse(null);
        if (chemicals == null) {
            return List.of();
        }
        List<NullSeedEntry> entries = new ArrayList<>();
        chemicals.listElements()
                .filter(holder -> !holder.is(MekanismAPI.EMPTY_CHEMICAL_KEY))
                .sorted(Comparator.comparing(holder -> holder.key().location().toString()))
                .forEach(holder -> addChemical(entries, holder));
        return entries;
    }

    private static void addChemical(List<NullSeedEntry> entries, Holder.Reference<Chemical> holder) {
        ResourceKey<Chemical> key = holder.key();
        ResourceLocation id = key.location();
        StoredChemical stored = MekanismCompat.fromChemicalStack(new ChemicalStack(holder, 1000L));
        if (!stored.isEmpty()) {
            entries.add(NullSeedEntry.chemical(id, entries.size(), (int) Math.min(Integer.MAX_VALUE, stored.amount())));
        }
    }
}
