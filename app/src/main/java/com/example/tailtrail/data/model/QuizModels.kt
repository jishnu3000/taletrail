package com.example.tailtrail.data.model

data class QuizAnswer(
    val questionId: Int,
    val question: String,
    val selectedOption: String
)

data class QuizSubmissionRequest(
    val userId: Int,
    val answers: List<QuizAnswer>
)

data class QuizSubmissionResponse(
    val message: String
)

data class QuizErrorResponse(
    val errorDescription: String,
    val errorShortDescription: String,
    val errorCode: String
)
