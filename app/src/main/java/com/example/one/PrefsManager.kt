import android.content.Context
import androidx.core.content.edit

// Создаем класс-менеджер для удобного доступа
class PrefsManager(context: Context) {
    private val sharedPrefs = context.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)

    // Для сохранения данных
    fun saveAuthData(token: String, login: String) {
        sharedPrefs.edit {
            putString("ACCESS_TOKEN", token)
            putString("USER_LOGIN", login)
            apply() // или commit() для синхронного сохранения
        }
    }

    // Для получения данных
    fun getToken(): String? = sharedPrefs.getString("ACCESS_TOKEN", null)
    fun getLogin(): String? = sharedPrefs.getString("USER_LOGIN", null)

    // Для очистки данных при выходе
    fun clearAuthData() {
        sharedPrefs.edit {
            remove("ACCESS_TOKEN")
            remove("USER_LOGIN")
            apply()
        }
    }

    fun saveProfileLocally(context: Context, fullName: String, workPlace: String, location: String, bio: String) {
        val prefs = context.getSharedPreferences("ProfileCache", Context.MODE_PRIVATE)
        prefs.edit().apply {
            putString("fullName", fullName)
            putString("workPlace", workPlace)
            putString("location", location)
            putString("bio", bio)
            apply()
        }
    }

    fun loadProfileLocally(context: Context): Map<String, String> {
        val prefs = context.getSharedPreferences("ProfileCache", Context.MODE_PRIVATE)
        return mapOf(
            "fullName" to (prefs.getString("fullName", "") ?: ""),
            "workPlace" to (prefs.getString("workPlace", "") ?: ""),
            "location" to (prefs.getString("location", "") ?: ""),
            "bio" to (prefs.getString("bio", "") ?: "")
        )
    }

}