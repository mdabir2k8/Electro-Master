package com.example.data

import android.content.Context
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.io.InputStreamReader

class ComponentRepository(
    private val componentDao: ComponentDao,
    private val context: Context
) {
    val allComponents: Flow<List<ComponentEntity>> = componentDao.getAllComponents()

    fun searchComponents(query: String): Flow<List<ComponentEntity>> {
        return componentDao.searchComponents(query)
    }

    suspend fun populateDatabaseIfEmpty() {
        withContext(Dispatchers.IO) {
            try {
                val currentCount = componentDao.getCount()
                context.assets.open("components.json").use { inputStream ->
                    val jsonStr = inputStream.bufferedReader().use { it.readText() }
                    val jsonArray = org.json.JSONArray(jsonStr)
                    if (currentCount < jsonArray.length()) {
                    val components = ArrayList<ComponentEntity>(jsonArray.length())
                    for (i in 0 until jsonArray.length()) {
                        val obj = jsonArray.getJSONObject(i)
                        components.add(
                            ComponentEntity(
                                id = obj.optString("id", (i + 1).toString()),
                                name = obj.optString("name"),
                                category = obj.optString("category"),
                                icon = obj.optString("icon"),
                                pinout = obj.optString("pinout"),
                                primaryFunction = obj.optString("primaryFunction"),
                                keySpecs = obj.optString("keySpecs"),
                                useCase = obj.optString("useCase")
                            )
                        )
                    }
                    if (components.isNotEmpty()) {
                        componentDao.insertAll(components)
                    }
                }
            }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
