package com.example.one

import android.net.Uri
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import coil.load
import com.canhub.cropper.CropImageContract
import com.canhub.cropper.CropImageContractOptions
import com.canhub.cropper.CropImageOptions
import com.canhub.cropper.CropImageView
import com.yourpackage.diaryschool.network.ApiManager
import kotlinx.coroutines.*

class StaffEditProfileFragment : Fragment() {

    private lateinit var imageView: ImageView
    private lateinit var saveButton: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var viewModel: ApiManager
    private var selectedImageUri: Uri? = null

    private lateinit var fullNameEditText: EditText
    private lateinit var workPlaceEditText: EditText
    private lateinit var locationEditText: EditText
    private lateinit var descriptionEditText: EditText

    private val cropImageLauncher = registerForActivityResult(CropImageContract()) { result ->
        if (result.isSuccessful) {
            val uriContent = result.uriContent
            selectedImageUri = uriContent
            imageView.setImageURI(uriContent)
        } else {
            val error = result.error
            Toast.makeText(requireContext(), "Ошибка: ${error?.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { startCrop(it) }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.staff_profile_edit_fragment, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ApiManager(requireContext())

        imageView = view.findViewById(R.id.profileImage)


        saveButton = view.findViewById(R.id.saveButton)
        progressBar = view.findViewById(R.id.progressBar)

        fullNameEditText = view.findViewById(R.id.fullNameEditText)
        workPlaceEditText = view.findViewById(R.id.workPlaceEditText)
        locationEditText = view.findViewById(R.id.locationEditText)
        descriptionEditText = view.findViewById(R.id.descriptionEditText)

        view.findViewById<ImageButton>(R.id.exit_button).setOnClickListener {
            replaceFragment(ProfileFragment())
        }

        imageView.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        saveButton.setOnClickListener {
            saveProfile()
        }
        //TODO("Заполнение полей из кешп + сервера")
    }

    private fun startCrop(uri: Uri) {
        val cropOptions = CropImageContractOptions(
            uri,
            CropImageOptions().apply {
                cropShape = CropImageView.CropShape.OVAL
                aspectRatioX = 1
                aspectRatioY = 1
                fixAspectRatio = true
                guidelines = CropImageView.Guidelines.ON
                cropMenuCropButtonTitle = "Готово"
            }
        )
        cropImageLauncher.launch(cropOptions)
    }

    private fun saveProfile() {
        val fullName = fullNameEditText.text.toString().trim()
        val work = workPlaceEditText.text.toString().trim()
        val location = locationEditText.text.toString().trim()
        val description = descriptionEditText.text.toString().trim()

        if (fullName.isEmpty() || work.isEmpty() || location.isEmpty()) {
            Toast.makeText(requireContext(), "Заполните все поля", Toast.LENGTH_SHORT).show()
            return
        }

        progressBar.visibility = View.VISIBLE
        saveButton.isEnabled = false

        CoroutineScope(Dispatchers.IO).launch {
            val success = viewModel.updateFullProfile(
                requireContext(),
                selectedImageUri,
                fullName,
                work,
                location,
                description
            )



            withContext(Dispatchers.Main) {
                progressBar.visibility = View.GONE
                saveButton.isEnabled = true

                if (success) {
                    Toast.makeText(requireContext(), "Профиль обновлён", Toast.LENGTH_SHORT).show()
                    replaceFragment(ProfileFragment())
                } else {
                    Toast.makeText(requireContext(), "Ошибка при обновлении профиля", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun replaceFragment(newFragment: Fragment) {
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, newFragment)
            .addToBackStack(null)
            .commit()
    }
}