package comtest.ct.cd.purwo.rest

import android.os.SystemClock
import comtest.ct.cd.purwo.tools.Constant
import io.reactivex.Observable
import okhttp3.Dispatcher
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Query

/**
 * Created By Purwo on 12/09/20
 */
interface ApiServices {
    @GET("search/users")
    @Headers("Content-Type: application/json")
    fun search(
        @Query("q") q: String,
        @Query("page") page: Int,
        @Query("per_page") perPage: Int,
        @Query("order") orderBy: String
    ): Observable<UserResponse>

    companion object {
        fun create(): ApiServices {

            val builder = OkHttpClient.Builder()
            val dispatcher = Dispatcher()

            dispatcher.maxRequests = 1

            val interceptor = Interceptor { chain ->
                SystemClock.sleep(2550)
                chain.proceed(chain.request())
            }

            builder.addNetworkInterceptor(interceptor)
            builder.dispatcher(dispatcher)

            var client = builder.build()

            val retrofit = Retrofit.Builder()
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl(Constant.BASE_URL)
                .client(client)
                .build()

            return retrofit.create(ApiServices::class.java)
        }
    }
}