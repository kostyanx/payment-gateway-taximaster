package ru.catcab.taximaster.paymentgateway.util.ktor.feature

import io.ktor.client.*
import io.ktor.client.features.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.util.*
import ru.catcab.taximaster.paymentgateway.util.common.Helpers.md5

class TaxiMasterAuth(val secret: String) {
    class Config(var secret: String = "secret")

    companion object Feature : HttpClientFeature<Config, TaxiMasterAuth> {
        override val key: AttributeKey<TaxiMasterAuth> = AttributeKey("TaxiMasterAuth")

        override fun prepare(block: Config.() -> Unit): TaxiMasterAuth = TaxiMasterAuth(Config().apply(block).secret)

        override fun install(feature: TaxiMasterAuth, scope: HttpClient) {
            scope.requestPipeline.intercept(HttpRequestPipeline.Render) { content ->
                if (context.method == HttpMethod.Post) {
                    if (content is TextContent) {
                        context.headers["Signature"] = md5(content.text + feature.secret)
                    }
                }
                if (context.method == HttpMethod.Get) {
                    val queryParametersString = context.url.parameters.build().formUrlEncode()
                    context.headers["Signature"] = md5(queryParametersString + feature.secret)
                }
            }
        }
    }
}