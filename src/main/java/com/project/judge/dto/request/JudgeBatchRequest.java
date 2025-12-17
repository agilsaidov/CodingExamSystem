package com.project.judge.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JudgeBatchRequest {
    private List<JudgeSubmissionRequest> submissions;
}