package com.example.alhaja.data.local

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
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "joyas_favoritas")
data class JoyaFavoritaEntity(
    @PrimaryKey val id: Int,
    val nombre: String,
    val categoria: String,
    val descripcion: String,
    val precio: Double,
    val material: String,
    val imagenUrl: String
)

@Entity(tableName = "lugares_joyeria")
data class LugarEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val nombre: String,
    val latitud: Double,
    val longitud: Double,
    val fechaMillis: Long,
    val fotoUri: String? = null
)

@Dao
interface JoyaFavoritaDao {
    @Query("SELECT * FROM joyas_favoritas ORDER BY nombre")
    fun observarFavoritas(): Flow<List<JoyaFavoritaEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun guardar(joya: JoyaFavoritaEntity)

    @Query("DELETE FROM joyas_favoritas WHERE id = :id")
    suspend fun eliminar(id: Int)

    @Query("SELECT EXISTS(SELECT 1 FROM joyas_favoritas WHERE id = :id)")
    suspend fun esFavorita(id: Int): Boolean
}

@Dao
interface LugarDao {
    @Query("SELECT * FROM lugares_joyeria ORDER BY fechaMillis DESC")
    fun observarLugares(): Flow<List<LugarEntity>>

    @Insert
    suspend fun guardar(lugar: LugarEntity): Long

    @Query("DELETE FROM lugares_joyeria WHERE id = :id")
    suspend fun eliminar(id: Long)
}

@Database(
    entities = [JoyaFavoritaEntity::class, LugarEntity::class],
    version = 2,
    exportSchema = false
)
abstract class AlhajaDatabase : RoomDatabase() {
    abstract fun favoritaDao(): JoyaFavoritaDao
    abstract fun lugarDao(): LugarDao

    companion object {
        @Volatile
        private var instance: AlhajaDatabase? = null

        fun obtener(context: Context): AlhajaDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AlhajaDatabase::class.java,
                    "alhaja.db"
                ).fallbackToDestructiveMigration(dropAllTables = true)
                    .build()
                    .also { instance = it }
            }
    }
}
