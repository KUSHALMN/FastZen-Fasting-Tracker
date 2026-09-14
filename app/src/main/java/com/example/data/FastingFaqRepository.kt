package com.example.data

import com.example.model.FastingFaqItem

object FastingFaqRepository {
    val allFaqs: List<FastingFaqItem> = buildList {
        addAll(FaqDrinksData)
        addAll(FaqMetabolismData)
        addAll(FaqHungerSymptomsData)
        addAll(FaqBreakingFastData)
        addAll(FaqFitnessMuscleData)
        addAll(FaqWomenHealthData)
        addAll(FaqProtocolsHabitsData)
        addAll(FaqMythsMistakesData)
        addAll(FaqMindsetPsychologyData)
        addAll(FaqLongevityAntiAgingData)
    }

    val categories: List<String> = listOf(
        "All",
        "Drinks & Fasting",
        "Metabolism & Science",
        "Hunger & Symptoms",
        "Breaking a Fast",
        "Fitness & Muscle",
        "Women's Health",
        "Protocols & Habits",
        "Myths & Mistakes",
        "Mindset & Psychology",
        "Longevity & Anti-Aging"
    )
}
