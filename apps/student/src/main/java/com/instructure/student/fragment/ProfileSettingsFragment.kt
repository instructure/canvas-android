/*
 * Copyright (C) 2017 - present Instructure, Inc.
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, version 3 of the License.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package com.instructure.student.fragment

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.FileProvider
import androidx.loader.app.LoaderManager
import androidx.loader.content.AsyncTaskLoader
import androidx.loader.content.Loader
import com.instructure.canvasapi2.managers.FileFolderManager
import com.instructure.canvasapi2.managers.FileUploadManager
import com.instructure.canvasapi2.managers.UserManager
import com.instructure.canvasapi2.models.AvatarWrapper
import com.instructure.canvasapi2.models.FileFolder
import com.instructure.canvasapi2.models.User
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.pageview.PageView
import com.instructure.canvasapi2.utils.weave.*
import com.instructure.pandautils.analytics.SCREEN_VIEW_PROFILE_SETTINGS
import com.instructure.pandautils.analytics.ScreenView
import com.instructure.pandautils.binding.viewBinding
import com.instructure.pandautils.utils.*
import com.instructure.student.R
import com.instructure.student.activity.PandaAvatarActivity
import com.instructure.student.databinding.DialogPhotoSourceBinding
import com.instructure.student.databinding.FragmentProfileSettingsBinding
import com.instructure.student.dialog.EditTextDialog
import com.instructure.student.events.UserUpdatedEvent
import com.instructure.student.util.StudentPrefs
import org.greenrobot.eventbus.EventBus
import java.io.File

@ScreenView(SCREEN_VIEW_PROFILE_SETTINGS)
@PageView(url = "profile")
class ProfileSettingsFragment : ParentFragment(), LoaderManager.LoaderCallbacks<AvatarWrapper> {

    private val binding by viewBinding(FragmentProfileSettingsBinding::bind)

    private var loaderBundle: Bundle? = null
    private var mCapturedImageUri: Uri? = null
    private var mPermissionCall: WeaveJob? = null
    private var mUpdateNameCall: WeaveJob? = null
    private var mUpdateAvatarCall: WeaveJob? = null

    override fun title(): String = getString(R.string.profileSettings)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
            inflater.inflate(R.layout.fragment_profile_settings, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        applyTheme()
        setupViews()
        getUserPermissions()
        binding.scrollView.applyBottomSystemBarInsets()
    }

    override fun applyTheme() {
        binding.toolbar.setupAsBackButton(this)
        binding.toolbar.applyTopSystemBarInsets()
        ViewStyler.themeToolbarColored(requireActivity(), binding.toolbar, ThemePrefs.primaryColor, ThemePrefs.primaryTextColor)
    }

    private fun setupViews() = with(binding) {
        editPhoto.onClickWithRequireNetwork { pickAvatar() }
        editUsername.onClickWithRequireNetwork {
            EditTextDialog.show(requireFragmentManager(), getString(R.string.editUserName), ApiPrefs.user?.shortName.orEmpty()) { name ->
                updateUserName(name)
            }
        }
        createPandaAvatar.onClickWithRequireNetwork {
            startActivityForResult(Intent(requireActivity(), PandaAvatarActivity::class.java), Const.PANDA_AVATAR_RESULT_CODE)
        }
    }

    private fun setEnabled(view: View, isEnabled: Boolean) {
        view.isEnabled = isEnabled
        view.alpha = if (isEnabled) 1f else 0.35f
    }

    private fun getUserPermissions() = with(binding) {
        mPermissionCall = tryWeave {
            val user = awaitApi<User> { UserManager.getSelfWithPermissions(true, it) }
            editUsername.setVisible()
            editPhoto.setVisible()
            createPandaAvatar.setVisible()
            loadingFrameLayout.setGone()
            setEnabled(editUsername, user.canUpdateName())
            setEnabled(editPhoto, user.canUpdateAvatar())
            setEnabled(createPandaAvatar, user.canUpdateAvatar())
        } catch {
            toast(R.string.errorOccurred)
            requireActivity().onBackPressed()
        }
    }

    private fun updateUserName(newName: String) {
        if (newName.isBlank()) {
            toast(R.string.invalidUsername)
            return
        }
        mUpdateNameCall = tryWeave {
            val user = awaitApi<User> { UserManager.updateUserShortName(newName, it) }
            ApiPrefs.user = ApiPrefs.user?.copy(shortName = user.shortName)
            EventBus.getDefault().postSticky(UserUpdatedEvent(ApiPrefs.user!!))
            toast(R.string.usernameChangeSuccess)
        } catch {
            toast(R.string.errorOccurred)
        }
    }

    private fun updateAvatarUrl(newUrl: String) {
        mUpdateAvatarCall = weave {
            try {
                val newAvatarUrl = if (!ApiPrefs.domain.contains("instructure")) {
                    // We have a vanity URL for the domain - has to be handled slightly differently
                    val fileNumber = newUrl.substringAfter("files/").substringBefore(("/download"))
                    val token = awaitApi<FileFolder> { FileFolderManager.getAvatarFileToken(fileNumber, it) }.avatar?.token
                    val user = awaitApi<User> { UserManager.updateUsersAvatarWithToken(token!!, it) }
                    user.avatarUrl
                } else {
                    awaitApi<User> { UserManager.updateUsersAvatar(newUrl, it) }.avatarUrl
                }
                ApiPrefs.user = ApiPrefs.user?.apply { avatarUrl = newAvatarUrl }
                EventBus.getDefault().postSticky(UserUpdatedEvent(ApiPrefs.user!!))
                toast(R.string.regularAvatarSuccessfullySaved)
                loaderBundle = null
            } catch (e: Throwable) {
                toast(R.string.uploadAvatarFailMsg)
            } finally {
                if(this@ProfileSettingsFragment.isAdded) {
                    binding.photoProgressBar.setGone()
                    binding.createPandaProgressBar.setGone()
                    setEnabled(binding.editPhoto, true)
                    setEnabled(binding.createPandaAvatar, true)
                }
            }
        }
    }

    @SuppressLint("InflateParams")
    private fun pickAvatar() {
        val dialogBinding = DialogPhotoSourceBinding.inflate(layoutInflater)
        AlertDialog.Builder(requireContext())
                .setView(dialogBinding.root)
                .show()
                .apply {
                    dialogBinding.takePhotoItem.onClick {
                        newPhoto()
                        dismiss()
                    }
                    dialogBinding.chooseFromGalleryItem.onClick {
                        chooseFromGallery()
                        dismiss()
                    }
                }
    }

    private fun newPhoto() {
        if (!Utils.hasCameraAvailable(activity)) {
            showToast(R.string.noCameraOnDevice)
            return
        }

        if (PermissionUtils.hasPermissions(requireActivity(), PermissionUtils.WRITE_EXTERNAL_STORAGE, PermissionUtils.CAMERA)) {
            takeNewPhotoBecausePermissionsAlreadyGranted()
        } else {
            requestPermissions(PermissionUtils.makeArray(PermissionUtils.WRITE_EXTERNAL_STORAGE, PermissionUtils.CAMERA), REQUEST_CODE_PERMISSIONS_TAKE_PHOTO)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (PermissionUtils.allPermissionsGrantedResultSummary(grantResults)) {
            if (requestCode == REQUEST_CODE_PERMISSIONS_TAKE_PHOTO) {
                takeNewPhotoBecausePermissionsAlreadyGranted()
            } else if (requestCode == REQUEST_CODE_PERMISSIONS_GALLERY) {
                chooseFromGallery()
            }
        } else {
            Toast.makeText(requireActivity(), R.string.permissionDenied, Toast.LENGTH_LONG).show()
        }
    }

    private fun takeNewPhotoBecausePermissionsAlreadyGranted() {
        // Get the location of the saved picture
        val fileName = "profilePic_${System.currentTimeMillis()}.jpg"
        val values = ContentValues()
        values.put(MediaStore.Images.Media.TITLE, fileName)

        mCapturedImageUri = requireContext().contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)

        if (mCapturedImageUri != null) {
            // Save the intent information in case we get booted from memory.
            StudentPrefs.tempCaptureUri = mCapturedImageUri.toString()
        }

        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, mCapturedImageUri)
        cameraIntent.putExtra(Const.IS_OVERRIDDEN, true)
        startActivityForResult(cameraIntent, RequestCodes.CAMERA_PIC_REQUEST)
    }

    private fun chooseFromGallery() {
        if (!PermissionUtils.hasPermissions(requireActivity(), PermissionUtils.WRITE_EXTERNAL_STORAGE)) {
            requestPermissions(PermissionUtils.makeArray(PermissionUtils.WRITE_EXTERNAL_STORAGE), REQUEST_CODE_PERMISSIONS_GALLERY)
            return
        }
        val intent = Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        val file = File(requireContext().filesDir, "/image/*")
        intent.setDataAndType(FileProvider.getUriForFile(requireContext(), requireContext().applicationContext.packageName + Const.FILE_PROVIDER_AUTHORITY, file), "image/*")
        startActivityForResult(intent, RequestCodes.PICK_IMAGE_GALLERY)
    }

    override fun onCreateLoader(id: Int, args: Bundle?): Loader<AvatarWrapper> {
        return PostAvatarLoader(
                requireActivity(),
                args?.getString(Const.NAME) ?: "",
                args?.getString(Const.CONTENT_TYPE) ?: "",
                args?.getString(Const.PATH) ?: "",
                args?.getLong(Const.SIZE) ?: 0L,
                args?.getBoolean(Const.DELETE) ?: false
        )
    }

    override fun onLoadFinished(loader: Loader<AvatarWrapper>, data: AvatarWrapper?) {
        if (data?.avatar != null) {
            updateAvatarUrl(data.avatar!!.url!!)
        } else if (data != null) {
            if (data.error == AvatarWrapper.ERROR_QUOTA_EXCEEDED) {
                showToast(R.string.fileQuotaExceeded)
            } else if (data.error == AvatarWrapper.ERROR_UNKNOWN) {
                showToast(R.string.errorUploadingFile)
            }
        }
    }

    override fun onLoaderReset(loader: Loader<AvatarWrapper>) {}

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        // Restore loader if necessary
        LoaderUtils.restoreLoaderFromBundle<ProfileSettingsFragment>(loaderManager, savedInstanceState, this, R.id.avatarLoaderID)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        LoaderUtils.saveLoaderBundle(outState, loaderBundle)
        super.onSaveInstanceState(outState)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RequestCodes.CROP_IMAGE && resultCode == Activity.RESULT_OK) {
            val croppedPath = data!!.data!!.path!!
            val croppedFile = File(croppedPath)
            loaderBundle = createLoaderBundle("profilePic.jpg", "image/jpeg", croppedPath, croppedFile.length(), true)
            LoaderUtils.restartLoaderWithBundle(loaderManager, loaderBundle, this, R.id.avatarLoaderID)
            binding.photoProgressBar.setVisible()
            setEnabled(binding.editPhoto, false)
            setEnabled(binding.createPandaAvatar, false)

        } else if (requestCode == RequestCodes.CAMERA_PIC_REQUEST && resultCode != Activity.RESULT_CANCELED) {
            if (mCapturedImageUri == null) {
                // Recover Uri from prefs in case we were booted from memory.
                mCapturedImageUri = Uri.parse(StudentPrefs.tempCaptureUri)
            }

            // If it's still null, tell the user there is an error and return.
            if (mCapturedImageUri == null) {
                showToast(R.string.errorGettingPhoto)
                return
            }

            // Open image for cropping
            val config = AvatarCropConfig(mCapturedImageUri!!)
            val cropIntent = AvatarCropActivity.createIntent(requireContext(), config)
            startActivityForResult(cropIntent, RequestCodes.CROP_IMAGE)

        } else if (requestCode == RequestCodes.PICK_IMAGE_GALLERY && resultCode != Activity.RESULT_CANCELED) {

            data?.data?.let { uri ->
                // Open image for cropping
                val config = AvatarCropConfig(uri)
                val cropIntent = AvatarCropActivity.createIntent(requireContext(), config)
                startActivityForResult(cropIntent, RequestCodes.CROP_IMAGE)
            }

        } else if (requestCode == Const.PANDA_AVATAR_RESULT_CODE && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                val pandaPath = data.getStringExtra(Const.PATH)
                val size = data.getLongExtra(Const.SIZE, 0)
                // The api will rename the avatar automatically for us
                loaderBundle = createLoaderBundle("pandaAvatar.png", "image/png", pandaPath, size, false)
                LoaderUtils.restartLoaderWithBundle(loaderManager, loaderBundle, this, R.id.avatarLoaderID)
                binding.createPandaProgressBar.setVisible()
                setEnabled(binding.editPhoto, false)
                setEnabled(binding.createPandaAvatar, false)
            }
        }
    }

    private fun createLoaderBundle(
            name: String,
            contentType: String,
            path: String?,
            size: Long,
            deleteOnCompletion: Boolean
    ) = Bundle().apply {
        putString(Const.NAME, name)
        putString(Const.CONTENT_TYPE, contentType)
        putString(Const.PATH, path)
        putLong(Const.SIZE, size)
        putBoolean(Const.DELETE, deleteOnCompletion)
    }

    internal class PostAvatarLoader(
            context: Context,
            private val name: String,
            private val contentType: String,
            private val path: String,
            private val size: Long,
            private val deleteOnCompletion: Boolean
    ) : AsyncTaskLoader<AvatarWrapper>(context) {
        override fun loadInBackground(): AvatarWrapper {
            val wrapper = FileUploadManager.uploadAvatarSynchronous(name, size, contentType, path)
            if (deleteOnCompletion) File(path).delete()
            return wrapper
        }

        override fun onStopLoading() {
            cancelLoad()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mPermissionCall?.cancel()
        mUpdateNameCall?.cancel()
        mUpdateAvatarCall?.cancel()
    }

    companion object {
        private const val REQUEST_CODE_PERMISSIONS_TAKE_PHOTO = 223
        private const val REQUEST_CODE_PERMISSIONS_GALLERY = 332

        fun newInstance(): ProfileSettingsFragment {
            return ProfileSettingsFragment()
        }
    }
}
