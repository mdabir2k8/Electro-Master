package com.example.data

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import androidx.compose.runtime.Immutable

@Entity(tableName = "components")
@Immutable
data class ComponentEntity(
    @PrimaryKey val id: String,
    val name: String,
    val category: String,
    val icon: String,
    val pinout: String,
    val primaryFunction: String,
    val keySpecs: String,
    val useCase: String
)

@Dao
interface ComponentDao {
    @Query("SELECT COUNT(*) FROM components")
    suspend fun getCount(): Int

    @Query("SELECT * FROM components ORDER BY name ASC")
    fun getAllComponents(): Flow<List<ComponentEntity>>

    @Query("SELECT * FROM components WHERE name LIKE '%' || :query || '%' OR category LIKE '%' || :query || '%' OR primaryFunction LIKE '%' || :query || '%' ORDER BY name ASC")
    fun searchComponents(query: String): Flow<List<ComponentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(components: List<ComponentEntity>)
}

@Database(entities = [ComponentEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun componentDao(): ComponentDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "electromaster_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
