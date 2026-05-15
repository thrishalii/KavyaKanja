package com.example.kavyakanaja.data

data class PoemLine(
    val text: String,
    val meaning: String
)

data class Poem(
    val title: String,
    val author: String,
    val category: String,
    val audio: String,
    val lines: List<PoemLine>
)

data class PoetBio(
    val name: String,
    val canonicalName: String,
    val lifespan: String,
    val awards: List<String>,
    val bio: String,
    val famousWorks: List<String>,
    val imageUrls: List<String> = emptyList()
)

val poetBios = mapOf(
    "ಕುವೆಂಪು (Kuvempu)" to PoetBio(
        name = "ಕುವೆಂಪು",
        canonicalName = "Kuppali Venkatappa Puttappa",
        lifespan = "1904 – 1994",
        awards = listOf("Jnanpith Award (1967)", "Padma Vibhushan (1988)", "Karnataka Ratna (1992)", "Rashtrakavi (1958)"),
        bio = "Kuvempu is widely regarded as the greatest Kannada poet of the 20th century. He was a strong advocate for universal humanism ('Vishwa Manava'). His epic 'Sri Ramayana Darshanam' is a masterpiece of modern Indian literature. He established the 'Pancha Mantra' for a reformed society.",
        famousWorks = listOf("Sri Ramayana Darshanam", "Malegalalli Madumagalu", "Kanooru Heggadithi", "Nenapina Doniyali")
    ),
    "ದ.ರಾ. ಬೇಂದ್ರೆ (D.R. Bendre)" to PoetBio(
        name = "ದ.ರಾ. ಬೇಂದ್ರೆ",
        canonicalName = "Dattatreya Ramachandra Bendre",
        lifespan = "1896 – 1981",
        awards = listOf("Jnanpith Award (1974)", "Padma Shri (1968)", "Sahitya Akademi Award (1958)"),
        bio = "Known as 'Ambikatanayadatta', Bendre is celebrated as the supreme lyric poet of Kannada. His poetry is known for its rhythmic flow, deep emotional resonance, and use of folk metaphors. He received the Jnanpith Award for his collection 'Naaku Tanti'.",
        famousWorks = listOf("Naku Tanthi", "Gari", "Aralu Maralu", "Gangavatarana")
    ),
    "ಬಸವಣ್ಣ (Basavanna)" to PoetBio(
        name = "ಬಸವಣ್ಣ",
        canonicalName = "Basaveshwara",
        lifespan = "1131 – 1167",
        awards = listOf("Vishwa Guru", "Bhakti Bhandari"),
        bio = "Basavanna was a 12th-century philosopher, statesman, Kannada poet and a social reformer. He pioneered the Vachana movement and advocated for a society free from caste and creed, emphasizing 'Kayaka' (work is worship) and social equality.",
        famousWorks = listOf("Vachanas (Kudalasangamadeva)")
    ),
    "ಡಿ.ವಿ. ಗುಂಡಪ್ಪ (D.V. Gundappa)" to PoetBio(
        name = "ಡಿ.ವಿ. ಗುಂಡಪ್ಪ",
        canonicalName = "D. V. Gundappa (DVG)",
        lifespan = "1887 – 1975",
        awards = listOf("Padma Bhushan (1974)", "Sahitya Akademi Award (1967)"),
        bio = "A prominent Kannada philosopher, poet, and writer. DVG is best known for 'Mankuthimmana Kagga', which offers profound reflections on life's mysteries through simple yet deep four-line stanzas. He founded the Gokhale Institute of Public Affairs.",
        famousWorks = listOf("Mankuthimmana Kagga", "Marula Muniyana Kagga", "Jnapaka Chitrashale")
    ),
    "ಕೆ.ಎಸ್. ನಿಸಾರ್ ಅಹಮದ್ (K.S. Nissar Ahmed)" to PoetBio(
        name = "ಕೆ.ಎಸ್. ನಿಸಾರ್ ಅಹಮದ್",
        canonicalName = "K. S. Nissar Ahmed",
        lifespan = "1936 – 2020",
        awards = listOf("Padma Shri (2008)", "Pampa Award (2017)", "Rajyotsava Award"),
        bio = "Famously known as the 'Nityotsava Kavi' for his iconic poem 'Nityotsava'. His poetry is characterized by a blend of nature, love, and social consciousness, and he was awarded several honors for his contribution to Kannada literature.",
        famousWorks = listOf("Nityotsava", "Manasu Gandhi Gandhi", "Sanje Aidara Male")
    ),
    "ಜಿ.ಎಸ್. ಶಿವರುದ್ರಪ್ಪ (G.S. Shivarudrappa)" to PoetBio(
        name = "ಜಿ.ಎಸ್. ಶಿವರುದ್ರಪ್ಪ",
        canonicalName = "G. S. Shivarudrappa",
        lifespan = "1926 – 2013",
        awards = listOf("Pampa Award (1997)", "Sahitya Akademi Award (1984)", "Rashtrakavi (2006)"),
        bio = "A prominent Kannada poet, writer and researcher who was awarded the title of Rashtrakavi by the Government of Karnataka in 2006. His poetry reflects deep philosophical insights and human values.",
        famousWorks = listOf("Saamanyana Hridayadaariya", "Gatotsava", "Deepada Hejje")
    ),
    "ಸಿದ್ಧಲಿಂಗಯ್ಯ (Siddalingaiah)" to PoetBio(
        name = "ಸಿದ್ಧಲಿಂಗಯ್ಯ",
        canonicalName = "Dr. Siddalingaiah",
        lifespan = "1954 – 2021",
        awards = listOf("Pampa Award", "Rajyotsava Award", "Nadoja Award"),
        bio = "Known as 'Dalita Kavi', he was one of the most prominent Kannada poets and activists. He was a co-founder of the Dalita Sangharsha Samiti. His poetry is known for its revolutionary themes and social criticism.",
        famousWorks = listOf("Holemadigara Haadu", "Saaviraaru Nadigalu", "Ooru Keri")
    )
)
