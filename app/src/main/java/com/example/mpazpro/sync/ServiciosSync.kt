package com.example.mpazpro.sync

import com.example.mpazpro.data.local.AppDatabase
import com.example.mpazpro.data.remote.ResultadoRemoto
import com.example.mpazpro.data.remote.api.ProgresoApi
import com.example.mpazpro.data.repository.ActividadesServidor
import com.example.mpazpro.data.repository.AvanceServidor

/** Arma el [AvanceServidor] con su base y su caché; lo usan la app y la tarea en segundo plano. */
fun crearAvanceServidor(
    api: ProgresoApi,
    actividades: ActividadesServidor,
    baseDeDatos: AppDatabase,
    alQuedarPendiente: () -> Unit = {}
) = AvanceServidor(
    api = api,
    dao = baseDeDatos.avanceLocalDao(),
    idsContenidos = { objetivoId ->
        baseDeDatos.catalogoCacheDao().contenidos().filter { it.objetivoId == objetivoId }.map { it.id }
    },
    dificultadesActividades = { objetivoId ->
        when (val carga = actividades.cargar(objetivoId)) {
            is ResultadoRemoto.Ok -> ResultadoRemoto.Ok(carga.valor.map { it.dificultad })
            is ResultadoRemoto.ErrorHttp -> carga
            is ResultadoRemoto.SinConexion -> carga
        }
    },
    alQuedarPendiente = alQuedarPendiente
)
