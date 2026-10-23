package com.example.mpazpro.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.mpazpro.data.model.Usuario

@Dao
interface UsuarioDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertar(usuario: Usuario): Long

    // IGNORE: si el usuario ya existe (el nombre de usuario es único), se salta sin dar error.
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertarTodos(usuarios: List<Usuario>)

    @Query("SELECT * FROM usuarios WHERE usuario = :usuario LIMIT 1")
    suspend fun buscarPorUsuario(usuario: String): Usuario?

    @Query("SELECT * FROM usuarios WHERE id = :id")
    suspend fun buscarPorId(id: Long): Usuario?

    @Query("SELECT COUNT(*) FROM usuarios")
    suspend fun contar(): Int
}
