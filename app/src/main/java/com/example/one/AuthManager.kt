package com.yourpackage.diaryschool.network

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import java.io.File
import java.util.concurrent.TimeUnit
import androidx.core.content.edit
import okhttp3.ResponseBody
import android.graphics.BitmapFactory
import com.example.one.ConversationPreview

// -------------------- DATA MODELS --------------------
data class LoginRequest(val username: String, val password: String)
data class TokenResponse(val access_token: String, val token_type: String)
data class ProtectedResponse(val message: String, val status: String)
data class Grade(val id: Int, val studentId: Int, val subject: String, val value: Int, val date: String)
data class Homework(val id: Int, val classId: Int, val subject: String, val dueDate: String, val description: String)
data class Student(val id: Int, val name: String)
data class Subject(val id: Int, val name: String)
data class Class(val id: Int, val name: String)

data class Student2(
    val id: Int,
    val full_name: String,  // Измените name на full_name
    val class_name: String? = null,
    val photo_url: String? = null,
)

data class Profile(
    val id: Int,
    val userId: Int,
    val fullName: String,
    val bio: String,
    val photoUrl: String?,
    val createdAt: String
)

data class ProfileRequest(
    val fullName: String,
    val bio: String
)

data class ProfileResponse(
    val profile: Profile,
    val photoUrl: String?
)

data class TokenVerificationResponse(
    val status: String,
    val username: String,
    val role: String,
    val access_token: String,
    val token_type: String,
    val user_id: Int
)

data class ProfileData(
    val full_name: String,
    val work_place: String,
    val location: String,
    val bio: String
)

data class StudentScoreResponse(
    val student_id: Int,
    val student_name: String,
    val grades: List<GradeItem>
)

data class GradeItem(
    val subject: String,
    val value: Int,
    val date: String
)


data class Teacher2(
    val id: Int,
    val full_name: String,
    val work_place: String?,
    val location: String?,
    val subject: String?,
    val classes: String?,
    val username: String?,
    val password: String?
)

data class SendMessageRequest(
    val receiver_id: Int,
    val content: String
)

data class MessageResponse(
    val sender_id: Int,
    val content: String,
    val created_at: String
)

data class UserPublicInfo(
    val id: Int,
    val full_name: String,
    val role: String,
    val photo_url: String?,
    val class_name: String?,
    val work_place: String?
)

// -------------------- API INTERFACE --------------------
interface DiaryApiService {
    @POST("register")
    suspend fun register(@Body user: LoginRequest): Response<Unit>

    @FormUrlEncoded
    @POST("token")
    suspend fun login(
        @Field("username") username: String,
        @Field("password") password: String,
        @Field("grant_type") grantType: String = "password"
    ): Response<TokenResponse>

    @GET("protected")
    suspend fun getProtected(): Response<ProtectedResponse>

    @GET("grades/{studentId}")
    suspend fun getGrades(@Path("studentId") studentId: Int): Response<List<Grade>>

    @POST("grades")
    suspend fun addGrade(@Body grade: Grade): Response<Grade>

    @GET("homeworks/{classId}")
    suspend fun getHomeworks(@Path("classId") classId: Int): Response<List<Homework>>

    @POST("homeworks")
    suspend fun addHomework(@Body homework: Homework): Response<Homework>

    @GET("students/{classId}")
    suspend fun getClassStudents(@Path("classId") classId: Int): Response<List<Student>>

    @GET("subjects")
    suspend fun getSubjects(): Response<List<Subject>>

    @GET("classes")
    suspend fun getClasses(): Response<List<Class>>


    @GET("/profile/photo")
    suspend fun getPhoto(
        @Header("Authorization") authHeader: String
    ): Response<ResponseBody>

    @GET("verify-token")
    suspend fun verifyToken(
        @Header("Authorization") authHeader: String
    ): Response<TokenVerificationResponse>


    @GET("profile/info")
    suspend fun getProfileData(
        @Header("Authorization") authHeader: String
    ): Response<ProfileData>

    @Multipart
    @POST("profile/full-update")
    suspend fun updateFullProfile(
        @Header("Authorization") authHeader: String,
        @Part("full_name") fullName: String,
        @Part("work_place") workPlace: String,
        @Part("location") location: String,
        @Part("bio") bio: String,
        @Part photo: MultipartBody.Part? = null
    ): Response<Unit>

    @GET("student-scores-full")
    suspend fun getAllStudentScores(): Response<List<StudentScoreResponse>>


    @GET("users/all")
    suspend fun getAllUsersByRole(
        @Query("role") role: String,
        @Query("page") page: Int = 1,
        @Query("per_page") perPage: Int = 20
    ): Response<List<Map<String, Any>>>


    @POST("messages/send")
    suspend fun sendMessage(
        @Body message: SendMessageRequest
    ): Response<Unit>

    @GET("messages/{userId}")
    suspend fun getMessagesWithUser(
        @Path("userId") userId: Int
    ): Response<List<MessageResponse>>

    @GET("conversations")
    suspend fun getStartedConversations(
        @Header("Authorization") authHeader: String
    ): Response<List<ConversationPreview>>

    @GET("users/{userId}")
    suspend fun getUserPublicInfo(
        @Path("userId") userId: Int
    ): Response<UserPublicInfo>

}

// -------------------- API MANAGER --------------------
class ApiManager(context: Context) {

    private val baseUrl = "http://10.0.2.2:8001/"
    private val timeout = 30L

    private val prefs = context.getSharedPreferences("DiaryPrefs", Context.MODE_PRIVATE)

    private val retrofit: Retrofit by lazy {
        val logging = HttpLoggingInterceptor { message -> Log.d("API_LOG", message) }
        logging.level = HttpLoggingInterceptor.Level.BODY

        val client = OkHttpClient.Builder()
            .connectTimeout(timeout, TimeUnit.SECONDS)
            .readTimeout(timeout, TimeUnit.SECONDS)
            .writeTimeout(timeout, TimeUnit.SECONDS)
            .addInterceptor(logging)
            .addInterceptor { chain ->
                val request = chain.request()
                val token = getToken()
                val newRequest = if (!token.isNullOrEmpty() && !request.url.encodedPath.contains("token")) {
                    request.newBuilder()
                        .addHeader("Authorization", "Bearer $token")
                        .build()
                } else request
                chain.proceed(newRequest)
            }
            .build()

        Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    private val api = retrofit.create(DiaryApiService::class.java)


    fun saveUserId(id: Int) {
        prefs.edit().putInt("user_id", id).apply()
    }

    fun getUserId(): Int = prefs.getInt("user_id", -1)
    fun saveToken(token: String) {
        prefs.edit { putString("access_token", token) }
    }

    fun getToken(): String? = prefs.getString("access_token", null)

    fun clearData() {
        prefs.edit {
            remove("access_token")
                .remove("user_id")
                .remove("full_name")
                .remove("work_place")
                .remove("location")
                .remove("bio")
        }
    }

    suspend fun register(username: String, password: String): Boolean = withContext(Dispatchers.IO) {
        runCatching {
            api.register(LoginRequest(username, password)).isSuccessful
        }.getOrElse {
            Log.e("API_REGISTER", it.localizedMessage ?: "Unknown error")
            false
        }
    }

    suspend fun login(username: String, password: String): Boolean = withContext(Dispatchers.IO) {
        runCatching {
            val response = api.login(username, password)
            if (response.isSuccessful) {
                response.body()?.access_token?.let { saveToken(it) }

                val isValid = isTokenValid() 
                isValid
            } else false
        }.getOrElse {
            Log.e("API_LOGIN", it.localizedMessage ?: "Unknown error")
            false
        }
    }


    suspend fun getProtectedMessage(): String? = withContext(Dispatchers.IO) {
        runCatching {
            val response = api.getProtected()
            if (response.isSuccessful) response.body()?.message else null
        }.getOrNull()
    }

    suspend fun getGrades(studentId: Int): List<Grade> = withContext(Dispatchers.IO) {
        runCatching {
            val response = api.getGrades(studentId)
            if (response.isSuccessful) response.body() ?: emptyList() else emptyList()
        }.getOrDefault(emptyList())
    }

    suspend fun addGrade(grade: Grade): Boolean = withContext(Dispatchers.IO) {
        runCatching {
            api.addGrade(grade).isSuccessful
        }.getOrElse { false }
    }

    suspend fun getHomeworks(classId: Int): List<Homework> = withContext(Dispatchers.IO) {
        runCatching {
            val response = api.getHomeworks(classId)
            if (response.isSuccessful) response.body() ?: emptyList() else emptyList()
        }.getOrDefault(emptyList())
    }

    suspend fun addHomework(homework: Homework): Boolean = withContext(Dispatchers.IO) {
        runCatching {
            api.addHomework(homework).isSuccessful
        }.getOrElse { false }
    }

    suspend fun getClassStudents(classId: Int): List<Student> = withContext(Dispatchers.IO) {
        runCatching {
            val response = api.getClassStudents(classId)
            if (response.isSuccessful) response.body() ?: emptyList() else emptyList()
        }.getOrDefault(emptyList())
    }

    suspend fun getSubjects(): List<Subject> = withContext(Dispatchers.IO) {
        runCatching {
            val response = api.getSubjects()
            if (response.isSuccessful) response.body() ?: emptyList() else emptyList()
        }.getOrDefault(emptyList())
    }

    suspend fun getClasses(): List<Class> = withContext(Dispatchers.IO) {
        runCatching {
            val response = api.getClasses()
            if (response.isSuccessful) response.body() ?: emptyList() else emptyList()
        }.getOrDefault(emptyList())
    }


    private fun createTempFileFromUri(context: Context, uri: Uri): File? {
        return try {
            val file = File.createTempFile("upload_${System.currentTimeMillis()}", ".jpg", context.cacheDir)
            context.contentResolver.openInputStream(uri)?.use { input ->
                file.outputStream().use { output -> input.copyTo(output) }
            }
            file
        } catch (e: Exception) {
            Log.e("FileUtils", "Error creating temp file", e)
            null
        }
    }

    suspend fun getProfile(): Bitmap? = withContext(Dispatchers.IO) {
        runCatching {
            val token = getToken() ?: return@withContext null
            val response: Response<ResponseBody> = api.getPhoto("Bearer $token")
            if (response.isSuccessful) {
                response.body()?.byteStream()?.use { inputStream ->
                    BitmapFactory.decodeStream(inputStream)
                }
            } else {
                null
            }
        }.getOrElse {
            Log.e("API_PROFILE_PHOTO", it.localizedMessage ?: "Photo fetch error")
            null
        }
    }

    suspend fun getProfilePhoto(): Bitmap? = withContext(Dispatchers.IO) {
        runCatching {
            val token = getToken() ?: return@withContext null
            val response = api.getPhoto("Bearer $token")
            if (response.isSuccessful) {
                response.body()?.byteStream()?.use { inputStream ->
                    BitmapFactory.decodeStream(inputStream)
                }
            } else {
                null
            }
        }.getOrElse {
            Log.e("ApiManager", it.localizedMessage ?: "Error loading photo")
            null
        }
    }

    suspend fun isTokenValid(): Boolean = withContext(Dispatchers.IO) {
        val token = getToken() ?: return@withContext false

        runCatching {
            val response = api.verifyToken("Bearer $token")
            if (response.isSuccessful) {
                val body = response.body()
                body?.access_token?.let { saveToken(it) }
                body?.user_id?.let { saveUserId(it) }
                true
            }
            else {
                false
            }
        }.getOrElse {
            Log.e("API_TOKEN_VERIFY", it.localizedMessage ?: "Token verification error")
            false
        }
    }


    suspend fun getProfileData(): ProfileData? = withContext(Dispatchers.IO) {
        runCatching {
            val token = getToken() ?: return@withContext null
            val response = api.getProfileData("Bearer $token")
            println(response)
            if (response.isSuccessful) response.body() else null
        }.getOrElse {
            Log.e("API_GET_PROFILE_DATA", it.localizedMessage ?: "Fetch profile error")
            null
        }
    }

    suspend fun updateFullProfile(
        context: Context,
        imageUri: Uri?,
        fullName: String,
        work: String,
        location: String,
        bio: String
    ): Boolean = withContext(Dispatchers.IO) {
        val token = getToken() ?: return@withContext false

        val photoPart = imageUri?.let {
            try {
                val file = createTempFileFromUri(context, it) ?: return@let null
                val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
                MultipartBody.Part.createFormData("file", file.name, requestFile)
            } catch (e: Exception) {
                Log.e("API_PHOTO_PART", e.localizedMessage ?: "Photo part creation error")
                null
            }
        }

        return@withContext runCatching {
            val response = api.updateFullProfile(
                authHeader = "Bearer $token",
                fullName = fullName,
                workPlace = work,
                location = location,
                bio = bio,
                photo = photoPart
            )
            response.isSuccessful
        }.getOrElse {
            Log.e("API_FULL_UPDATE", it.localizedMessage ?: "Full update error")
            false
        }
    }

    suspend fun getAllStudentScores(): List<StudentScoreResponse> = withContext(Dispatchers.IO) {
        runCatching {
            val response = api.getAllStudentScores()
            if (response.isSuccessful) response.body() ?: emptyList() else emptyList()
        }.getOrDefault(emptyList())
    }


    suspend fun getAllStudents(page: Int = 1, perPage: Int = 20): List<Student2> = withContext(Dispatchers.IO) {
        runCatching {
            val response = api.getAllUsersByRole("student", page, perPage)
            if (response.isSuccessful) {
                val raw = response.body() ?: return@runCatching emptyList()
                raw.mapNotNull { item ->
                    try {
                        Student2(
                            id = (item["id"] as? Double)?.toInt() ?: return@mapNotNull null,
                            full_name = item["full_name"] as? String ?: "Неизвестно",
                            class_name = item["class_name"] as? String,
                            photo_url = item["photo_url"] as? String
                        )
                    } catch (e: Exception) {
                        null
                    }
                }
            } else {
                emptyList()
            }
        }.getOrDefault(emptyList())
    }



    suspend fun getAllTeachers(page: Int = 1, perPage: Int = 20): List<Teacher2> = withContext(Dispatchers.IO) {
        runCatching {
            val response = api.getAllUsersByRole("teacher", page, perPage)
            if (response.isSuccessful) {
                val raw = response.body() ?: return@runCatching emptyList()
                raw.mapNotNull { item ->
                    try {
                        Teacher2(
                            id = (item["id"] as? Double)?.toInt() ?: return@mapNotNull null,
                            full_name = item["full_name"] as? String ?: "Неизвестно",
                            work_place = item["work_place"] as? String,
                            location = item["location"] as? String,
                            subject = item["subject"] as? String,
                            classes = item["classes"] as? String,
                            username = item["username"] as? String,
                            password = item["password"] as? String
                        )
                    } catch (e: Exception) {
                        null
                    }
                }
            } else {
                emptyList()
            }
        }.getOrDefault(emptyList())
    }



    suspend fun sendMessage(receiverId: Int, content: String): Boolean = withContext(Dispatchers.IO) {
        runCatching {
            val request = SendMessageRequest(receiverId, content)
            val response = api.sendMessage(request)
            response.isSuccessful
        }.getOrElse {
            Log.e("API_SEND_MESSAGE", it.localizedMessage ?: "Send message error")
            false
        }
    }

    suspend fun getMessagesWithUser(userId: Int): List<MessageResponse> = withContext(Dispatchers.IO) {
        runCatching {
            val response = api.getMessagesWithUser(userId)
            if (response.isSuccessful) response.body() ?: emptyList() else emptyList()
        }.getOrDefault(emptyList())
    }


    suspend fun getStartedConversations(): List<ConversationPreview> = withContext(Dispatchers.IO) {
        runCatching {
            val token = getToken() ?: return@withContext emptyList()
            val response = api.getStartedConversations("Bearer $token")
            if (response.isSuccessful) response.body() ?: emptyList() else emptyList()
        }.getOrDefault(emptyList())
    }


    suspend fun getUserPublicInfo(userId: Int): UserPublicInfo? = withContext(Dispatchers.IO) {
        runCatching {
            val response = api.getUserPublicInfo(userId)
            if (response.isSuccessful) response.body() else null
        }.getOrElse {
            Log.e("API_USER_INFO", it.localizedMessage ?: "User info error")
            null
        }
    }

}
