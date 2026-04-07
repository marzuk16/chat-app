package com.marzuk.components.pojos.response;

import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PageResponseTest {

    @Test
    void fromPageMapsAllPaginationFieldsCorrectly() {
        List<String> items = List.of("alpha", "beta", "gamma");
        PageRequest pageRequest = PageRequest.of(1, 3);
        Page<String> springPage = new PageImpl<>(items, pageRequest, 9);

        PageResponse<String> pageResponse = PageResponse.from(springPage);

        assertThat(pageResponse.getContent()).containsExactlyElementsOf(items);
        assertThat(pageResponse.getPage()).isEqualTo(1);
        assertThat(pageResponse.getSize()).isEqualTo(3);
        assertThat(pageResponse.getTotalElements()).isEqualTo(9);
        assertThat(pageResponse.getTotalPages()).isEqualTo(3);
        assertThat(pageResponse.isLast()).isFalse();
    }

    @Test
    void fromPageSetsLastTrueOnFinalPage() {
        List<String> items = List.of("alpha");
        PageRequest pageRequest = PageRequest.of(2, 3);
        Page<String> springPage = new PageImpl<>(items, pageRequest, 7);

        PageResponse<String> pageResponse = PageResponse.from(springPage);

        assertThat(pageResponse.isLast()).isTrue();
    }
}
