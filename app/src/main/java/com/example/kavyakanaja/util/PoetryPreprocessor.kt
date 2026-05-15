package com.example.kavyakanaja.util

import com.example.kavyakanaja.data.Poem
import com.example.kavyakanaja.data.PoemLine
import org.json.JSONArray
import org.json.JSONObject

/**
 * Utility to process raw poetry text and generate structured JSON.
 * This can be used to clean text extracted from PDFs or AI-generated translations.
 */
object PoetryPreprocessor {

    fun processRawText(
        title: String,
        author: String,
        category: String,
        rawLines: List<String>,
        meanings: List<String> = emptyList()
    ): String {
        val linesArray = JSONArray()
        rawLines.forEachIndexed { index, text ->
            val obj = JSONObject()
            obj.put("text", text.trim())
            obj.put("meaning", meanings.getOrNull(index)?.trim() ?: "Meaning coming soon.")
            linesArray.put(obj)
        }

        val poemJson = JSONObject()
        poemJson.put("title", title.trim())
        poemJson.put("author", author.trim())
        poemJson.put("category", category.trim())
        poemJson.put("audio", "${title.lowercase().replace(" ", "_")}.mp3")
        poemJson.put("lines", linesArray)

        return poemJson.toString(2)
    }

    /**
     * Placeholder for AI-assisted cleaning logic.
     * In a real implementation, this would interface with an LLM API.
     */
    fun cleanAndStructure(rawPdfContent: String): String {
        // Logic to strip page numbers, headers, and unwanted artifacts
        return rawPdfContent.replace(Regex("\\d+"), "").trim()
    }
}
