package com.example.tailtrail.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController

val quizQuestions = listOf(
    QuizQuestion(1, "You come across a fork in the road. What do you do?", listOf(
        "Take the path that's well-lit and frequently traveled.",
        "Pick the mysterious path that leads into the forest.",
        "Stop and analyze both paths before choosing.",
        "Ask someone nearby for advice and follow their lead."
    )),
    QuizQuestion(2, "In a team setting, you are usually the one who...", listOf(
        "Leads the group confidently.",
        "Suggests creative and out-of-the-box ideas.",
        "Ensures everyone is heard and tasks are clear.",
        "Waits to understand the situation before acting."
    )),
    QuizQuestion(3, "When faced with a sudden challenge, your first instinct is to...", listOf(
        "Jump in and solve it quickly.",
        "Look for a clever workaround.",
        "Break it down logically.",
        "Step back and consider emotions involved."
    )),
    QuizQuestion(4, "You value stories that are...", listOf(
        "Full of action and bold heroes.",
        "Imaginative and fantastical.",
        "Strategic with plot twists.",
        "Emotional and character-driven."
    )),
    QuizQuestion(5, "How do you usually make decisions?", listOf(
        "Quickly, by trusting your gut.",
        "Intuitively, guided by your feelings.",
        "After thorough analysis.",
        "By discussing with others first."
    )),
    QuizQuestion(6, "Your ideal adventure includes...", listOf(
        "Battling through tough terrain.",
        "Discovering magical artifacts.",
        "Solving ancient puzzles.",
        "Meeting diverse and interesting people."
    )),
    QuizQuestion(7, "If a friend is in trouble, you would...", listOf(
        "Charge in and help immediately.",
        "Devise a creative rescue plan.",
        "Assess the risks before acting.",
        "Get support and plan a group solution."
    )),
    QuizQuestion(8, "In stressful situations, you tend to...", listOf(
        "Stay calm and take action.",
        "Distract yourself with creative outlets.",
        "Focus on logic to find a solution.",
        "Talk it out with someone you trust."
    )),
    QuizQuestion(9, "Which quote resonates most with you?", listOf(
        "Fortune favors the bold.",
        "Imagination is the true magic carpet.",
        "Knowledge is power.",
        "Empathy is strength."
    )),
    QuizQuestion(10, "What role do you usually play in a group?", listOf(
        "The action-taker.",
        "The dreamer.",
        "The strategist.",
        "The supporter."
    )),
    QuizQuestion(11, "When starting a new journey, what excites you the most?", listOf(
        "The unknown challenges ahead.",
        "The chance to create unforgettable memories.",
        "The opportunity to learn something new.",
        "The people you'll meet along the way."
    )),
    QuizQuestion(12, "What would you do if you lost your way in a new place?", listOf(
        "Keep moving forward and trust your instincts.",
        "Find a creative landmark to guide you.",
        "Check a map and plan your next steps carefully.",
        "Ask a local or call a friend for help."
    )),
    QuizQuestion(13, "Which environment do you feel most drawn to?", listOf(
        "A rugged mountain trail.",
        "An enchanted forest.",
        "An ancient ruin full of secrets.",
        "A peaceful village full of stories."
    )),
    QuizQuestion(14, "If your story had a theme, it would be about...", listOf(
        "Courage and triumph.",
        "Wonder and magic.",
        "Discovery and logic.",
        "Connection and compassion."
    )),
    QuizQuestion(15, "How do you react when plans suddenly change?", listOf(
        "Adapt quickly and keep going.",
        "Go with the flow and find joy in the chaos.",
        "Analyze the new situation before deciding.",
        "Talk it through and consider everyone’s comfort."
    ))
)

data class QuizQuestion(val id: Int, val question: String, val options: List<String>)

@Composable
fun QuizScreen(navController: NavHostController) {
    var answers by remember { mutableStateOf(MutableList(quizQuestions.size) { -1 }) }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Quiz",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF673AB7),
            modifier = Modifier.padding(bottom = 16.dp)
        )
        LazyColumn(
            modifier = Modifier.weight(1f)
        ) {
            itemsIndexed(quizQuestions) { index, question ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(text = question.question, fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.height(8.dp))
                        question.options.forEachIndexed { optIdx, option ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                RadioButton(
                                    selected = answers[index] == optIdx,
                                    onClick = { answers = answers.toMutableList().also { it[index] = optIdx } }
                                )
                                Text(text = option)
                            }
                        }
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = { /* No functionality yet */ },
            modifier = Modifier.fillMaxWidth(0.7f),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9C27B0))
        ) {
            Text("Submit", color = Color.White, fontSize = 18.sp)
        }
    }
}
