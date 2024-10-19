import kotlinx.serialization.Serializable
import java.io.File
import java.lang.IllegalArgumentException
import java.lang.IndexOutOfBoundsException

@Serializable
data class Word(
    val questionWord: String,
    val translate: String,
    var correctAnswersCount: Int = 0
)

data class Statistics(
    val learned: Int,
    val total: Int,
    val percent: Int,
)

data class Question(
    val variants: List<Word>,
    val correctAnswer: Word,
)

class LearnWordsTrainer(
    private val rightAnswerGoal: Int = 3,
    private val fileName: String = "words.txt",
    private val numberOfTranslateVariants: Int = 4,
) {
    var question: Question? = null
    private val dictionary = loadDictionary()

    fun getStatistics(): Statistics {
        val learned = dictionary.filter { it.correctAnswersCount >= rightAnswerGoal }.size
        val total = dictionary.size
        val percent = learned * 100 / total
        return Statistics(learned, total, percent)
    }

    fun getNextQuestion(): Question? {
        val notLearnedList = dictionary.filter { it.correctAnswersCount < rightAnswerGoal }
        if (notLearnedList.isEmpty()) return null
        val questionWords = if (notLearnedList.size < numberOfTranslateVariants) {
            val learnedList = dictionary.filter { it.correctAnswersCount >= rightAnswerGoal }.shuffled()
            notLearnedList.shuffled()
                .take(numberOfTranslateVariants) + learnedList.take(numberOfTranslateVariants - notLearnedList.size)
        } else {
            notLearnedList.shuffled().take(numberOfTranslateVariants)
        }.shuffled()

        val correctAnswer = questionWords.random()
        question = Question(
            variants = questionWords,
            correctAnswer = correctAnswer
        )
        return question
    }

    fun checkAnswer(userAnswerIndex: Int?): Boolean {
        return question?.let {
            val correctAnswerId = it.variants.indexOf(it.correctAnswer)
            if (correctAnswerId == userAnswerIndex) {
                it.correctAnswer.correctAnswersCount++
                saveDictionary()
                true
            } else {
                false
            }
        } ?: false
    }

    private fun loadDictionary(): List<Word> {
        try {
            val wordsFile = File(fileName)
            if (!wordsFile.exists()) {
                File("words.txt").copyTo(wordsFile)
            }
            val dictionary = mutableListOf<Word>()
            wordsFile.readLines().forEach {
                val splitLine = it.split("|")
                dictionary.add(Word(splitLine[0], splitLine[1], splitLine[2].toIntOrNull() ?: 0))
            }
            return dictionary
        } catch (e: IndexOutOfBoundsException) {
            throw IllegalArgumentException("некорректный файл")
        }
    }

    private fun saveDictionary() {
        val wordsFile = File(fileName)
        wordsFile.writeText("")
        for (word in dictionary) {
            wordsFile.appendText("${word.questionWord}|${word.translate}|${word.correctAnswersCount}\n")
        }
    }

    fun resetProgress(){
        dictionary.forEach { it.correctAnswersCount = 0 }
        saveDictionary()
    }

}