package org.bernshtam.weather.datasources

import org.apache.http.HttpHost
import org.apache.http.auth.AuthScope
import org.apache.http.auth.NTCredentials
import org.apache.http.client.methods.CloseableHttpResponse
import org.apache.http.client.methods.HttpGet
import org.apache.http.client.protocol.HttpClientContext
import org.apache.http.impl.client.BasicCredentialsProvider
import org.apache.http.impl.client.HttpClients
import org.bernshtam.weather.datasources.IMSConstants.dateTimeFormatter
import org.bernshtam.weather.datasources.IMSConstants.downloadDir
import org.bernshtam.weather.utils.TokenManager
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStream
import java.time.LocalDate

object IMSStreamProvider {
    private val httpclient = HttpClients.createDefault()

    private val credsProvider = BasicCredentialsProvider()
    private val target = HttpHost("data.israel-meteo-service.org", 8080, "http")


    private val user = TokenManager.get("ims.user")
    private val password = TokenManager.get("ims.password")

    init {
        credsProvider.setCredentials(
                AuthScope.ANY,
                NTCredentials(user, password, "", "data.israel-meteo-service.org")
        )

        // Make sure the same context is used to execute logically related requests

        IMSConstants.downloadDir.mkdirs()
    }

    fun redownload() {
        val fileDate = LocalDate.now()
        val yesterday = fileDate.minusDays(1)
        val dateStr = fileDate.format(dateTimeFormatter)
        val yesterdayStr = yesterday.format(dateTimeFormatter)

        IMSConstants.params.forEach { param ->
            val paramFileName = param.first

            val patterns = listOf(
                    "C3_${dateStr}12_$paramFileName.grb",
                    "C3_${dateStr}00_$paramFileName.grb",
                    "C3_${yesterdayStr}12_$paramFileName.grb",
                    "C3_${yesterdayStr}00_$paramFileName.grb"
            )
            for (f in patterns) {
                val file = File(downloadDir, f)
                if (file.exists()) break
                val response = getResponse("/ims/IMS_COSMO/$f")
                response.use { r ->
                    if (r.statusLine.statusCode == 200) {
                        val inputStream = r.entity.content
                        inputStream.use {
                            file.copyInputStreamToFile(inputStream)
                        }
                    }
                }

            }
        }

        removeOldFiles()
        IMSConnector.reopenGribFiles()
    }


    private fun getResponse(uri: String): CloseableHttpResponse {
        val context = HttpClientContext.create()
        context.credentialsProvider = credsProvider
        val httpget = HttpGet(uri)
        return httpclient.execute(target, httpget, context)
    }


    private fun removeOldFiles() {
        try {
            val files: List<File> = downloadDir.listFiles().toList().sortedBy { f -> f.lastModified() }
            val toRemoveNum = files.size - 9
            val toRemove = files.take(if (toRemoveNum > 0) toRemoveNum else 0)
            toRemove.forEach { it.delete() }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun File.copyInputStreamToFile(inputStream: InputStream) {
        inputStream.use { input ->
            this.outputStream().use { fileOut ->
                input.copyTo(fileOut)
            }
        }
    }
}