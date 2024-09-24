package com.example.diariodofiscal

import android.content.Context
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.bumptech.glide.Registry
import com.bumptech.glide.annotation.GlideModule
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.Options
import com.bumptech.glide.load.data.DataFetcher
import com.bumptech.glide.load.model.ModelLoader
import com.bumptech.glide.load.model.ModelLoaderFactory
import com.bumptech.glide.load.model.MultiModelLoaderFactory
import com.bumptech.glide.module.AppGlideModule
import com.bumptech.glide.signature.ObjectKey
import com.google.firebase.storage.StorageReference
import java.io.InputStream

// Criar um módulo Glide personalizado
@GlideModule
class AppGlideModule : AppGlideModule() {

    // Registrar o ModelLoader para StorageReference
    override fun registerComponents(context: Context, glide: Glide, registry: Registry) {
        registry.prepend(
            StorageReference::class.java,
            InputStream::class.java,
            FirebaseStorageModelLoader.Factory()
        )
    }
}

// Criar um ModelLoader personalizado para StorageReference
class FirebaseStorageModelLoader : ModelLoader<StorageReference, InputStream> {

    // Carregar a imagem do StorageReference e retornar um InputStream
    override fun buildLoadData(
        model: StorageReference,
        width: Int,
        height: Int,
        options: Options
    ): ModelLoader.LoadData<InputStream>? {
        return ModelLoader.LoadData(
            ObjectKey(model),
            FirebaseStorageFetcher(model)
        )
    }

    override fun handles(model: StorageReference): Boolean {
        // Lidar com o carregamento de imagens do Firebase Storage
        return true
    }

    // Factory para criar instâncias do FirebaseStorageModelLoader
    class Factory : ModelLoaderFactory<StorageReference, InputStream> {
        override fun build(multiFactory: MultiModelLoaderFactory): ModelLoader<StorageReference, InputStream> {
            return FirebaseStorageModelLoader()
        }

        override fun teardown() {
            // Nada a fazer aqui
        }
    }
}

// Implementar um Fetcher para buscar os dados do Firebase Storage
class FirebaseStorageFetcher(private val storageReference: StorageReference) : DataFetcher<InputStream> {
    private var stream: InputStream? = null

    override fun loadData(priority: Priority, callback: DataFetcher.DataCallback<in InputStream>) {
        // Carregar os dados do Firebase Storage
        storageReference.stream
            .addOnSuccessListener { taskSnapshot ->
                taskSnapshot.stream?.let { inputStream ->
                    stream = inputStream
                    callback.onDataReady(stream)
                }
            }
            .addOnFailureListener { exception ->
                callback.onLoadFailed(exception)
            }
    }

    override fun cleanup() {
        // Limpar recursos
        stream?.close()
    }

    override fun cancel() {
        // Cancelar operação se necessário
    }

    override fun getDataClass(): Class<InputStream> {
        return InputStream::class.java
    }

    override fun getDataSource(): DataSource {
        return DataSource.REMOTE
    }
}


