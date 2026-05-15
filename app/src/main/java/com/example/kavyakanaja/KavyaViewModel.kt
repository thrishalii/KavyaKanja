package com.example.kavyakanaja

import android.app.Application
import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import androidx.compose.runtime.*
import androidx.lifecycle.AndroidViewModel
import com.example.kavyakanaja.data.Poem
import com.example.kavyakanaja.data.PoemLine
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import org.json.JSONArray
import java.util.Locale

class KavyaViewModel(application: Application) : AndroidViewModel(application) {
    private val auth: FirebaseAuth = Firebase.auth
    private var tts: TextToSpeech? = null
    private val sharedPrefs = application.getSharedPreferences("kavya_prefs", Context.MODE_PRIVATE)
    
    var poems by mutableStateOf(listOf<Poem>())
        private set
        
    var searchQuery by mutableStateOf("")
    val favoritePoemTitles = mutableStateListOf<String>()
    
    var currentSpokenLineIndex by mutableIntStateOf(-1)
        private set
        
    var isPlaying by mutableStateOf(false)
        private set
    
    var currentUser by mutableStateOf(auth.currentUser)
        private set
        
    var isDarkTheme by mutableStateOf(sharedPrefs.getBoolean("is_dark_theme", false))

    init {
        loadPoems()
        loadFavorites()
        initTTS(application)
    }

    private fun initTTS(context: Context) {
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val result = tts?.setLanguage(Locale("kn", "IN"))
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    tts?.language = Locale.US
                }
            }
        }
    }

    private fun loadFavorites() {
        val favs = sharedPrefs.getStringSet("favorites", emptySet()) ?: emptySet()
        favoritePoemTitles.addAll(favs)
    }

    private fun saveFavorites() {
        sharedPrefs.edit().putStringSet("favorites", favoritePoemTitles.toSet()).apply()
    }

    private fun loadPoems() {
        try {
            val json = getApplication<Application>().assets.open("poems.json")
                .bufferedReader().use { it.readText() }
            val array = JSONArray(json)
            val temp = mutableListOf<Poem>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val linesArr = obj.getJSONArray("lines")
                val linesList = mutableListOf<PoemLine>()
                for (j in 0 until linesArr.length()) {
                    val l = linesArr.getJSONObject(j)
                    linesList.add(PoemLine(l.getString("text"), l.getString("meaning")))
                }
                temp.add(Poem(
                    obj.getString("title"), 
                    obj.getString("author"), 
                    obj.optString("category", "General"),
                    obj.getString("audio"), 
                    linesList
                ))
            }
            poems = temp
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    val filteredPoems: List<Poem>
        get() = if (searchQuery.isEmpty()) {
            poems
        } else {
            poems.filter { 
                it.title.contains(searchQuery, ignoreCase = true) || 
                it.author.contains(searchQuery, ignoreCase = true) ||
                it.category.contains(searchQuery, ignoreCase = true)
            }
        }

    val favoritePoems: List<Poem>
        get() = poems.filter { favoritePoemTitles.contains(it.title) }

    fun toggleFavorite(poem: Poem) {
        if (favoritePoemTitles.contains(poem.title)) {
            favoritePoemTitles.remove(poem.title)
        } else {
            favoritePoemTitles.add(poem.title)
        }
        saveFavorites()
    }

    fun isFavorite(poem: Poem): Boolean {
        return favoritePoemTitles.contains(poem.title)
    }

    private var lineToSpeak = 0
    private var currentLines: List<PoemLine> = emptyList()
    private var speakMeanings = false

    fun speakPoem(poem: Poem, showMeaning: Boolean) {
        stopSpeaking()
        isPlaying = true
        currentLines = poem.lines
        speakMeanings = showMeaning
        lineToSpeak = 0
        
        tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {
                utteranceId?.removePrefix("line_")?.toIntOrNull()?.let {
                    currentSpokenLineIndex = it
                }
            }
            override fun onDone(utteranceId: String?) {
                lineToSpeak++
                if (lineToSpeak < currentLines.size) {
                    speakNext()
                } else {
                    stopSpeaking()
                }
            }
            @Deprecated("Deprecated in Java")
            override fun onError(utteranceId: String?) {
                stopSpeaking()
            }
        })
        
        speakNext()
    }

    private fun speakNext() {
        if (lineToSpeak < currentLines.size) {
            val line = currentLines[lineToSpeak]
            val text = if (speakMeanings) "${line.text}. Meaning: ${line.meaning}" else line.text
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "line_$lineToSpeak")
        }
    }

    fun stopSpeaking() {
        tts?.stop()
        isPlaying = false
        currentSpokenLineIndex = -1
    }

    fun signOut() {
        auth.signOut()
        currentUser = null
    }

    fun updateCurrentUser() {
        currentUser = auth.currentUser
    }
    
    fun toggleTheme() {
        isDarkTheme = !isDarkTheme
        sharedPrefs.edit().putBoolean("is_dark_theme", isDarkTheme).apply()
    }

    override fun onCleared() {
        tts?.stop()
        tts?.shutdown()
        super.onCleared()
    }
}
