package dev.deepdaddyttv.deepnullreforged.client;

import dev.deepdaddyttv.deepnullreforged.network.DeepNullPayloads;

public interface StorageActionResultListener {
    void deepNullReforged$handleStorageActionResult(DeepNullPayloads.StorageActionResultPayload payload);
}
