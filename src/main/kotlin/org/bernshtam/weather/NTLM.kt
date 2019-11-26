package org.bernshtam.weather

import org.apache.http.HttpHost
import org.apache.http.auth.AuthScope
import org.apache.http.auth.NTCredentials
import org.apache.http.client.methods.HttpGet
import org.apache.http.client.protocol.HttpClientContext
import org.apache.http.impl.client.BasicCredentialsProvider
import org.apache.http.impl.client.HttpClients
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStream

object NTLM {
    private val httpclient = HttpClients.createDefault()

    private val credsProvider = BasicCredentialsProvider()
    private val target = HttpHost("data.israel-meteo-service.org", 8080, "http")
    private val context = HttpClientContext.create()

    private val downloadDir = File(System.getProperty("java.io.tmpdir"),"ims")

    private val user = TokenManager.get("ims.user")
    private val password = TokenManager.get("ims.password")

    init {
        credsProvider.setCredentials(
            AuthScope.ANY,
            NTCredentials(user, password, "", "data.israel-meteo-service.org")
        );


        // Make sure the same context is used to execute logically related requests
        context.credentialsProvider = credsProvider
        downloadDir.mkdirs()
    }

    @JvmStatic
    fun main(args: Array<String>) {
        val s = ""

        // Execute a cheap method first. This will trigger NTLM authentication
        get(s)

    }

    fun get(s: String): InputStream {
        val file = File(downloadDir, s)
        if (!file.exists()) {
            val httpget = HttpGet("/ims/IMS_COSMO/$s")
            val response = httpclient.execute(target, httpget, context)
            response.use { r ->
                if (r.statusLine.statusCode != 200) {
                    val message = r.statusLine.toString()
                    throw IOException(message)
                }
                val inputStream = r.entity.content
                inputStream.use {
                    file.copyInputStreamToFile(inputStream)
                }
            }
        }
        return FileInputStream(file)

    }

    fun File.copyInputStreamToFile(inputStream: InputStream) {
        inputStream.use { input ->
            this.outputStream().use { fileOut ->
                input.copyTo(fileOut)
            }
        }
    }
}