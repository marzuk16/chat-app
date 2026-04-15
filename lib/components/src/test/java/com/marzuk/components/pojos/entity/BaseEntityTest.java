package com.marzuk.components.pojos.entity;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

@DataJpaTest
@EntityScan(basePackageClasses = BaseEntityTest.class)
class BaseEntityTest {

    @Autowired private TestEntityManager entityManager;

    @Test
    void idAndTimestampsArePopulatedOnPersist() {
        UUID userId = UUID.randomUUID();
        SampleEntity sampleEntity = new SampleEntity();
        sampleEntity.setTitle("sample");
        sampleEntity.setCreatedBy(userId);
        sampleEntity.setUpdatedBy(userId);

        SampleEntity persistedEntity = entityManager.persistFlushFind(sampleEntity);

        assertThat(persistedEntity.getId()).isNotNull();
        assertThat(persistedEntity.getCreatedAt()).isNotNull();
        assertThat(persistedEntity.getUpdatedAt()).isNotNull();
    }

    @Test
    void createdByIsImmutableAfterPersist() {
        UUID originalUserId = UUID.randomUUID();
        UUID newUpdaterId = UUID.randomUUID();
        SampleEntity sampleEntity = new SampleEntity();
        sampleEntity.setTitle("sample");
        sampleEntity.setCreatedBy(originalUserId);
        sampleEntity.setUpdatedBy(originalUserId);

        SampleEntity persistedEntity = entityManager.persistFlushFind(sampleEntity);
        assertThat(persistedEntity.getCreatedBy()).isEqualTo(originalUserId);

        persistedEntity.setUpdatedBy(newUpdaterId);
        SampleEntity updatedEntity = entityManager.persistFlushFind(persistedEntity);

        assertThat(updatedEntity.getCreatedBy()).isEqualTo(originalUserId);
        assertThat(updatedEntity.getUpdatedBy()).isEqualTo(newUpdaterId);
    }

    @Entity
    @Table(name = "sample_entity")
    static class SampleEntity extends BaseEntity {
        private String title;

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }
    }
}
