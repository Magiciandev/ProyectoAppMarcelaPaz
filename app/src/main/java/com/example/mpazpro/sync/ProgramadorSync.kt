package com.example.mpazpro.sync

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

/** Pide al sistema que envíe lo pendiente cuando haya conexión. */
object ProgramadorSync {
    private const val ENVIO_PENDIENTE = "mpaz_sync_pendiente"
    private const val ENVIO_PERIODICO = "mpaz_sync_periodico"

    private val conRed = Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()

    /** Un envío en cuanto haya conexión. Si ya hay uno esperando, no se agrega otro. */
    fun programar(contexto: Context) {
        val pedido = OneTimeWorkRequestBuilder<SincronizacionWorker>()
            .setConstraints(conRed)
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.SECONDS)
            .build()
        WorkManager.getInstance(contexto).enqueueUniqueWork(ENVIO_PENDIENTE, ExistingWorkPolicy.KEEP, pedido)
    }

    /** Red de seguridad: cada 15 minutos (el mínimo de Android) con conexión, por si un envío quedó sin pedirse. */
    fun programarPeriodico(contexto: Context) {
        val pedido = PeriodicWorkRequestBuilder<SincronizacionWorker>(15, TimeUnit.MINUTES)
            .setConstraints(conRed)
            .build()
        WorkManager.getInstance(contexto)
            .enqueueUniquePeriodicWork(ENVIO_PERIODICO, ExistingPeriodicWorkPolicy.KEEP, pedido)
    }
}
