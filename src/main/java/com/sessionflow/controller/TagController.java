package com.sessionflow.controller;

import com.sessionflow.dto.TagRequest;
import com.sessionflow.dto.TagResponse;
import com.sessionflow.exception.ErrorResponse;
import com.sessionflow.service.TagService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tags")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Tag Management", description = "標籤管理相關 API")
public class TagController {
    
    private final TagService tagService;
    
    @PostMapping
    @Operation(summary = "建立標籤", description = "建立新的標籤")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "標籤建立成功",
                content = @Content(mediaType = "application/json", 
                schema = @Schema(implementation = TagResponse.class))),
        @ApiResponse(responseCode = "400", description = "請求參數錯誤",
                content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = """
                        {
                          "code": "VALIDATION_ERROR",
                          "message": "請求參數驗證失敗",
                          "details": "{name=標籤名稱不能為空}",
                          "timestamp": "2024-01-15T10:30:00"
                        }
                        """))),
        @ApiResponse(responseCode = "409", description = "標籤名稱重複",
                content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = """
                        {
                          "code": "TAG_NAME_CONFLICT",
                          "message": "標籤名稱重複",
                          "details": "Tag name 'work' already exists",
                          "timestamp": "2024-01-15T10:30:00"
                        }
                        """)))
    })
    public ResponseEntity<TagResponse> createTag(
            @Parameter(description = "標籤建立請求", required = true)
            @Valid @RequestBody TagRequest request) {
        
        log.info("POST /api/tags - Creating tag with name: {}", request.getName());
        TagResponse response = tagService.createTag(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @GetMapping
    @Operation(summary = "查詢所有標籤", description = "取得所有標籤列表")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "查詢成功",
                content = @Content(mediaType = "application/json", 
                array = @ArraySchema(schema = @Schema(implementation = TagResponse.class))))
    })
    public ResponseEntity<List<TagResponse>> getAllTags() {
        log.info("GET /api/tags - Fetching all tags");
        List<TagResponse> response = tagService.getAllTags();
        return ResponseEntity.ok(response);
    }
    
    @PutMapping("/{id}")
    @Operation(summary = "更新標籤", description = "更新指定 ID 的標籤")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "標籤更新成功",
                content = @Content(mediaType = "application/json", 
                schema = @Schema(implementation = TagResponse.class))),
        @ApiResponse(responseCode = "400", description = "請求參數錯誤",
                content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = """
                        {
                          "code": "VALIDATION_ERROR",
                          "message": "請求參數驗證失敗",
                          "details": "{name=標籤名稱不能為空}",
                          "timestamp": "2024-01-15T10:30:00"
                        }
                        """))),
        @ApiResponse(responseCode = "404", description = "標籤不存在",
                content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = """
                        {
                          "code": "TAG_NOT_FOUND",
                          "message": "標籤不存在",
                          "details": "Tag with id 1 not found",
                          "timestamp": "2024-01-15T10:30:00"
                        }
                        """))),
        @ApiResponse(responseCode = "409", description = "標籤名稱重複",
                content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = """
                        {
                          "code": "TAG_NAME_CONFLICT",
                          "message": "標籤名稱重複",
                          "details": "Tag name 'work' already exists",
                          "timestamp": "2024-01-15T10:30:00"
                        }
                        """)))
    })
    public ResponseEntity<TagResponse> updateTag(
            @Parameter(description = "標籤 ID", required = true, example = "1")
            @PathVariable Long id,
            @Parameter(description = "標籤更新請求", required = true)
            @Valid @RequestBody TagRequest request) {
        
        log.info("PUT /api/tags/{} - Updating tag with name: {}", id, request.getName());
        TagResponse response = tagService.updateTag(id, request);
        return ResponseEntity.ok(response);
    }
    
    @DeleteMapping("/{id}")
    @Operation(summary = "刪除標籤", description = "刪除指定 ID 的標籤")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "標籤刪除成功"),
        @ApiResponse(responseCode = "404", description = "標籤不存在",
                content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = """
                        {
                          "code": "TAG_NOT_FOUND",
                          "message": "標籤不存在",
                          "details": "Tag with id 1 not found",
                          "timestamp": "2024-01-15T10:30:00"
                        }
                        """)))
    })
    public ResponseEntity<Void> deleteTag(
            @Parameter(description = "標籤 ID", required = true, example = "1")
            @PathVariable Long id) {
        
        log.info("DELETE /api/tags/{} - Deleting tag", id);
        tagService.deleteTag(id);
        return ResponseEntity.noContent().build();
    }
} 