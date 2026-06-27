package com.ai.texttosql.controller;

import com.ai.texttosql.dto.TextToSqlRequest;
import com.ai.texttosql.dto.TextToSqlResponse;
import com.ai.texttosql.service.TextToSqlService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
class TextToSqlController {

    private final TextToSqlService textToSqlService;

    @PostMapping("/text-to-sql")
    public TextToSqlResponse textToSql(@RequestBody TextToSqlRequest request) {

        return textToSqlService.handle(request);
    }
}
