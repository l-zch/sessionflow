package com.sessionflow.repository;

import com.sessionflow.model.Tag;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@Transactional
@DisplayName("TagRepository 整合測試")
class TagRepositoryIntegrationTest {

    @Autowired
    private TagRepository tagRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    @DisplayName("成功建立 Tag")
    void shouldCreateTagSuccessfully() {
        // Given
        Tag tag = new Tag("工作", "#FF5733");

        // When
        Tag savedTag = tagRepository.save(tag);
        entityManager.flush();

        // Then
        assertThat(savedTag.getId()).isNotNull();
        assertThat(savedTag.getName()).isEqualTo("工作");
        assertThat(savedTag.getColor()).isEqualTo("#FF5733");
        assertThat(savedTag.getCreatedAt()).isNotNull();
        assertThat(savedTag.getUpdatedAt()).isNotNull();
    }

    @Test
    @DisplayName("查詢單筆 Tag")
    void shouldFindTagById() {
        // Given
        Tag tag = new Tag("測試標籤", "#00FF00");
        Tag savedTag = tagRepository.save(tag);
        entityManager.flush();
        entityManager.clear();

        // When
        Optional<Tag> foundTag = tagRepository.findById(savedTag.getId());

        // Then
        assertThat(foundTag).isPresent();
        assertThat(foundTag.get().getName()).isEqualTo("測試標籤");
        assertThat(foundTag.get().getColor()).isEqualTo("#00FF00");
    }

    @Test
    @DisplayName("查詢全部 Tag")
    void shouldFindAllTags() {
        // Given
        Tag tag1 = new Tag("工作", "#FF0000");
        Tag tag2 = new Tag("個人", "#00FF00");
        Tag tag3 = new Tag("緊急", "#0000FF");
        
        tagRepository.saveAll(List.of(tag1, tag2, tag3));
        entityManager.flush();

        // When
        List<Tag> allTags = tagRepository.findAll();

        // Then
        assertThat(allTags).hasSize(3);
        assertThat(allTags).extracting(Tag::getName)
                .containsExactlyInAnyOrder("工作", "個人", "緊急");
    }

    @Test
    @DisplayName("根據名稱查詢 Tag")
    void shouldFindTagByName() {
        // Given
        Tag tag = new Tag("特殊標籤", "#ABCDEF");
        tagRepository.save(tag);
        entityManager.flush();

        // When
        Optional<Tag> foundTag = tagRepository.findByName("特殊標籤");

        // Then
        assertThat(foundTag).isPresent();
        assertThat(foundTag.get().getColor()).isEqualTo("#ABCDEF");
    }

    @Test
    @DisplayName("檢查標籤名稱是否存在")
    void shouldCheckIfTagNameExists() {
        // Given
        Tag tag = new Tag("存在的標籤", "#123456");
        tagRepository.save(tag);
        entityManager.flush();

        // When & Then
        assertThat(tagRepository.existsByName("存在的標籤")).isTrue();
        assertThat(tagRepository.existsByName("不存在的標籤")).isFalse();
    }

    @Test
    @DisplayName("檢查除指定 ID 外是否存在相同名稱標籤")
    void shouldCheckIfTagNameExistsExcludingId() {
        // Given
        Tag tag1 = new Tag("測試標籤", "#111111");
        Tag tag2 = new Tag("其他標籤", "#222222");
        tagRepository.saveAll(List.of(tag1, tag2));
        entityManager.flush();

        // When & Then
        assertThat(tagRepository.existsByNameAndIdNot("測試標籤", tag2.getId())).isTrue();
        assertThat(tagRepository.existsByNameAndIdNot("測試標籤", tag1.getId())).isFalse();
        assertThat(tagRepository.existsByNameAndIdNot("不存在標籤", tag1.getId())).isFalse();
    }

    @Test
    @DisplayName("更新 Tag 欄位")
    void shouldUpdateTagFields() {
        // Given
        Tag tag = new Tag("原始名稱", "#000000");
        Tag savedTag = tagRepository.save(tag);
        entityManager.flush();
        
        LocalDateTime originalUpdatedAt = savedTag.getUpdatedAt();
        
        // 等待一毫秒確保時間戳不同
        try {
            Thread.sleep(1);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // When
        savedTag.setName("更新後名稱");
        savedTag.setColor("#FFFFFF");
        
        Tag updatedTag = tagRepository.save(savedTag);
        entityManager.flush();

        // Then
        assertThat(updatedTag.getName()).isEqualTo("更新後名稱");
        assertThat(updatedTag.getColor()).isEqualTo("#FFFFFF");
        assertThat(updatedTag.getUpdatedAt()).isAfter(originalUpdatedAt);
        assertThat(updatedTag.getCreatedAt()).isEqualTo(savedTag.getCreatedAt());
    }

    @Test
    @DisplayName("刪除 Tag 後資料庫應為空")
    void shouldDeleteTagAndDatabaseShouldBeEmpty() {
        // Given
        Tag tag1 = new Tag("標籤1", "#111111");
        Tag tag2 = new Tag("標籤2", "#222222");
        tagRepository.saveAll(List.of(tag1, tag2));
        entityManager.flush();

        // When
        tagRepository.deleteAll();
        entityManager.flush();

        // Then
        List<Tag> allTags = tagRepository.findAll();
        assertThat(allTags).isEmpty();
    }

    @Test
    @DisplayName("驗證標籤名稱唯一性約束")
    void shouldEnforceUniqueNameConstraint() {
        // Given
        Tag tag1 = new Tag("重複名稱", "#111111");
        tagRepository.save(tag1);
        entityManager.flush();

        Tag tag2 = new Tag("重複名稱", "#222222");

        // When & Then
        assertThatThrownBy(() -> {
            tagRepository.save(tag2);
            entityManager.flush();
        }).isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("驗證 not-null 欄位 - 名稱")
    void shouldValidateNotNullName() {
        // Given
        Tag tag = new Tag();
        tag.setColor("#FF0000");
        // name 為 null

        // When & Then
        assertThatThrownBy(() -> {
            tagRepository.save(tag);
            entityManager.flush();
        }).isInstanceOf(Exception.class);
    }

    @Test
    @DisplayName("驗證 not-null 欄位 - 顏色")
    void shouldValidateNotNullColor() {
        // Given
        Tag tag = new Tag();
        tag.setName("測試標籤");
        // color 為 null

        // When & Then
        assertThatThrownBy(() -> {
            tagRepository.save(tag);
            entityManager.flush();
        }).isInstanceOf(Exception.class);
    }

    @Test
    @DisplayName("驗證顏色格式 - 有效的十六進制顏色")
    void shouldAcceptValidHexColors() {
        // Given & When & Then
        String[] validColors = {"#000000", "#FFFFFF", "#FF5733", "#123ABC", "#abcdef"};
        
        for (String color : validColors) {
            Tag tag = new Tag("測試" + color, color);
            assertThatNoException().isThrownBy(() -> {
                tagRepository.save(tag);
                entityManager.flush();
            });
            tagRepository.delete(tag);
            entityManager.flush();
        }
    }

    @Test
    @DisplayName("驗證顏色格式 - 無效的顏色格式應拋出例外")
    void shouldRejectInvalidColorFormats() {
        // Given
        String[] invalidColors = {"FF5733", "#FF57", "#GGGGGG", "red", "#FF5733X", ""};
        
        for (String color : invalidColors) {
            Tag tag = new Tag("測試" + color, color);
            
            // When & Then
            assertThatThrownBy(() -> {
                tagRepository.save(tag);
                entityManager.flush();
            }).isInstanceOf(Exception.class);
        }
    }

    @Test
    @DisplayName("時間欄位檢查 - createdAt 和 updatedAt 自動設定")
    void shouldAutoSetTimestamps() {
        // Given
        LocalDateTime beforeCreate = LocalDateTime.now().minusSeconds(1);
        
        // When
        Tag tag = new Tag("時間測試標籤", "#123456");
        Tag savedTag = tagRepository.save(tag);
        entityManager.flush();
        
        LocalDateTime afterCreate = LocalDateTime.now().plusSeconds(1);

        // Then
        assertThat(savedTag.getCreatedAt()).isNotNull();
        assertThat(savedTag.getUpdatedAt()).isNotNull();
        assertThat(savedTag.getCreatedAt()).isAfter(beforeCreate);
        assertThat(savedTag.getCreatedAt()).isBefore(afterCreate);
        assertThat(savedTag.getUpdatedAt()).isAfter(beforeCreate);
        assertThat(savedTag.getUpdatedAt()).isBefore(afterCreate);
    }

    @Test
    @DisplayName("刪除單一標籤")
    void shouldDeleteSingleTag() {
        // Given
        Tag tag1 = new Tag("保留標籤", "#111111");
        Tag tag2 = new Tag("刪除標籤", "#222222");
        tagRepository.saveAll(List.of(tag1, tag2));
        entityManager.flush();

        // When
        tagRepository.delete(tag2);
        entityManager.flush();

        // Then
        List<Tag> remainingTags = tagRepository.findAll();
        assertThat(remainingTags).hasSize(1);
        assertThat(remainingTags.get(0).getName()).isEqualTo("保留標籤");
    }
} 