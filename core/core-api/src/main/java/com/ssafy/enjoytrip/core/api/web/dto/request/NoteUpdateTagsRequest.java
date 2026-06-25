package com.ssafy.enjoytrip.core.api.web.dto.request;

import com.ssafy.enjoytrip.core.domain.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record NoteUpdateTagsRequest(
        @NotNull @Valid List<TagInput> tags
) {
    public record TagInput(Long id, String name) {}

    public List<Tag> toTags() {
        return tags.stream()
                .map(input -> new Tag(input.id(), input.name()))
                .toList();
    }
}
