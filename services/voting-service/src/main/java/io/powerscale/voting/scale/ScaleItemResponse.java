package io.powerscale.voting.scale;

import java.util.UUID;

public record ScaleItemResponse(UUID id, String name, String imageUrl) {

    public static ScaleItemResponse from(ScaleItem item) {
        return new ScaleItemResponse(item.getId(), item.getName(), item.getImageUrl());
    }
}
