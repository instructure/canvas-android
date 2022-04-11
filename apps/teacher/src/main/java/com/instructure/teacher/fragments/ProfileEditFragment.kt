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
package com.instructure.teacher.fragments

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.text.InputType
import android.util.TypedValue
import android.view.WindowManager
import android.widget.TextView
import android.widget.Toast
import androidx.loader.app.LoaderManager
import androidx.loader.content.AsyncTaskLoader
import androidx.loader.content.Loader
import com.bumptech.glide.Glide
import com.instructure.canvasapi2.StatusCallback
import com.instructure.canvasapi2.managers.FileFolderManager
import com.instructure.canvasapi2.managers.FileUploadManager
import com.instructure.canvasapi2.managers.UserManager
import com.instructure.canvasapi2.models.AvatarWrapper
import com.instructure.canvasapi2.models.FileFolder
import com.instructure.canvasapi2.models.User
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.ApiPrefs.user
import com.instructure.canvasapi2.utils.ApiType
import com.instructure.canvasapi2.utils.LinkHeaders
import com.instructure.canvasapi2.utils.validOrNull
import com.instructure.pandautils.analytics.SCREEN_VIEW_PROFILE_EDIT
import com.instructure.pandautils.analytics.ScreenView
import com.instructure.pandautils.fragments.BasePresenterFragment
import com.instructure.pandautils.utils.*
import com.instructure.pandautils.utils.MediaUploadUtils.chooseFromGalleryBecausePermissionsAlreadyGranted
import com.instructure.pandautils.utils.MediaUploadUtils.takeNewPhotoBecausePermissionsAlreadyGranted
import com.instructure.teacher.R
import com.instructure.teacher.factory.ProfileEditFragmentPresenterFactory
import com.instructure.teacher.presenters.ProfileEditFragmentPresenter
import com.instructure.teacher.utils.getColorCompat
import com.instructure.teacher.utils.setupCloseButton
import com.instructure.teacher.utils.setupMenu
import com.instructure.teacher.viewinterface.ProfileEditFragmentView
import kotlinx.android.synthetic.main.fragment_profile_edit.*
import retrofit2.Response
import java.io.File

@ScreenView(SCREEN_VIEW_PROFILE_EDIT)
class ProfileEditFragment : BasePresenterFragment<
        ProfileEditFragmentPresenter,
        ProfileEditFragmentView>(), ProfileEditFragmentView, LoaderManager.LoaderCallbacks<AvatarWrapper> {


    private var mLoaderBundle: Bundle? = null

    override fun getPresenterFactory() = ProfileEditFragmentPresenterFactory()

    override fun onReadySetGo(presenter: ProfileEditFragmentPresenter) {
        presenter.loadData(false)
    }

    private val saveButton: TextView? get() = view?.findViewById(R.id.menu_save)

    override fun layoutResId() = R.layout.fragment_profile_edit

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        profileBanner.setImageResource(
                if(isTablet) R.drawable.teacher_profile_banner_image_tablet
                else R.drawable.teacher_profile_banner_image_phone)

        val user = user

        if(ProfileUtils.shouldLoadAltAvatarImage(user?.avatarUrl)) {
            val initials = ProfileUtils.getUserInitials(user?.shortName ?: "")
            val color = requireContext().getColorCompat(R.color.defaultTextGray)
            val drawable = TextDrawable.builder()
                    .beginConfig()
                    .height(requireContext().resources.getDimensionPixelSize(R.dimen.profileAvatarSize))
                    .width(requireContext().resources.getDimensionPixelSize(R.dimen.profileAvatarSize))
                    .toUpperCase()
                    .useFont(Typeface.DEFAULT_BOLD)
                    .textColor(color)
                    .endConfig()
                    .buildRound(initials, Color.WHITE)
            usersAvatar.borderColor = requireContext().getColorCompat(R.color.defaultTextGray)
            usersAvatar.borderWidth = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 6F, requireContext().resources.displayMetrics).toInt()
            usersAvatar.setImageDrawable(drawable)
        } else {
            updateAvatarImage(user?.avatarUrl)
        }

        usersName.setText(user?.shortName)
        usersName.hint = user?.shortName

        ViewStyler.themeEditText(requireContext(), usersName, ThemePrefs.brandColor)
        ViewStyler.colorImageView(profileCameraIcon, ThemePrefs.buttonColor)
        ViewStyler.themeProgressBar(profileCameraLoadingIndicator, ThemePrefs.brandColor)

        //Restore loader if necessary
        LoaderUtils.restoreLoaderFromBundle(LoaderManager.getInstance(this), savedInstanceState, this, R.id.avatarLoaderId)
        requireActivity().window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        LoaderUtils.saveLoaderBundle(outState, mLoaderBundle)
        super.onSaveInstanceState(outState)
    }

    override fun onResume() {
        super.onResume()
        setupToolbar()
    }

    fun setupToolbar() {
        toolbar.setupCloseButton(this)
        toolbar.title = getString(R.string.editProfile)
        toolbar.setupMenu(R.menu.menu_save_generic) { saveProfile() }
        ViewStyler.themeToolbarBottomSheet(requireActivity(), isTablet, toolbar, Color.BLACK, false)
        ViewStyler.setToolbarElevationSmall(requireContext(), toolbar)
        saveButton?.setTextColor(ThemePrefs.buttonColor)
    }

    override fun readyToLoadUI(user: User?) {
        profileCameraIconWrapper.setVisible(user?.canUpdateAvatar() == true)
        profileCameraIconWrapper.onClickWithRequireNetwork {
            MediaUploadUtils.showPickImageDialog(this)
        }
        if(profileCameraLoadingIndicator.isShown) { profileCameraLoadingIndicator.announceForAccessibility(getString(R.string.loading))}

        if(user != null && !user.canUpdateName()) {
            usersName.isEnabled = false
            usersName.inputType = InputType.TYPE_NULL
        }
    }

    private fun saveProfile(){
        val name = usersName.text.toString().validOrNull() ?: user?.shortName ?: ""
        presenter.saveChanges(name, user?.bio ?: "")
    }

    private fun updateAvatarImage(url: String?) {
        usersAvatar.borderColor = Color.WHITE
        usersAvatar.borderWidth = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 6F, requireContext().resources.displayMetrics).toInt()
        Glide.with(requireContext()).load(url).into(usersAvatar)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == MediaUploadUtils.REQUEST_CODE_PERMISSIONS_TAKE_PHOTO) {
            if (PermissionUtils.allPermissionsGrantedResultSummary(grantResults)) {
                takeNewPhotoBecausePermissionsAlreadyGranted(this, requireActivity())
            } else {
                Toast.makeText(requireActivity(), R.string.permissionDenied, Toast.LENGTH_LONG).show()
            }
        } else if (requestCode == MediaUploadUtils.REQUEST_CODE_PERMISSIONS_GALLERY) {
            if (PermissionUtils.allPermissionsGrantedResultSummary(grantResults)) {
                chooseFromGalleryBecausePermissionsAlreadyGranted(requireActivity())
            } else {
                Toast.makeText(requireActivity(), R.string.permissionDenied, Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        setupToolbar()

        if (requestCode == RequestCodes.CROP_IMAGE && resultCode == Activity.RESULT_OK) {
            data?.data?.let {
                mLoaderBundle = createLoaderBundle("profilePic.jpg", "image/jpeg", it.path.orEmpty(), File(it.path).length())
                showProgressBar()
                LoaderUtils.restartLoaderWithBundle(LoaderManager.getInstance(this), mLoaderBundle, this, R.id.avatarLoaderId)
            }

        } else if (requestCode == RequestCodes.CAMERA_PIC_REQUEST && resultCode == Activity.RESULT_OK) {
            if (presenter.capturedImageUri == null) {
                presenter.capturedImageUri = Uri.parse(FilePrefs.tempCaptureUri)
            }

            if (presenter.capturedImageUri == null) {
                showToast(R.string.errorGettingPhoto)
                return
            }

            presenter.capturedImageUri?.let {
                val cropConfig = AvatarCropConfig(it)
                startActivityForResult(AvatarCropActivity.createIntent(requireContext(), cropConfig), RequestCodes.CROP_IMAGE)
            }

        } else if (requestCode == RequestCodes.PICK_IMAGE_GALLERY && resultCode == Activity.RESULT_OK && data?.data != null) {
            val uri = data.data!!
            var urlPath = uri.path.orEmpty()
            if (urlPath.contains("googleusercontent")) {
                urlPath = changeGoogleURL(urlPath)
                user?.avatarUrl = urlPath
                UserManager.updateUsersAvatar(urlPath, mAvatarPostedCallback)
                return
            }

            val cropConfig = AvatarCropConfig(uri)
            startActivityForResult(AvatarCropActivity.createIntent(requireContext(), cropConfig), RequestCodes.CROP_IMAGE)
        }

        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun successSavingProfile() {
        Toast.makeText(requireContext(), R.string.profileEditSuccess, Toast.LENGTH_SHORT).show()
        requireActivity().onBackPressed()
    }

    override fun errorSavingProfile() {
        Toast.makeText(requireContext(), R.string.profileEditFailure, Toast.LENGTH_SHORT).show()
    }

    companion object {
        fun newInstance(args: Bundle) = ProfileEditFragment().apply {
            arguments = args
        }
    }

    override fun onRefreshStarted() {}

    override fun onRefreshFinished() {}

    override fun onPresenterPrepared(presenter: ProfileEditFragmentPresenter) {}

    private fun hideProgressBar() {
        profileCameraLoadingIndicator.setGone()
        profileCameraIcon.setVisible()
        profileCameraIconWrapper.isClickable = true
    }

    private fun showProgressBar() {
        profileCameraLoadingIndicator.setVisible()
        profileCameraIcon.setGone()
        profileCameraIconWrapper.isClickable = false
    }

    //region Avatar Resizing, Conversion and Helpers

    private fun changeGoogleURL(url: String): String {
        val start = url.indexOf("http")
        val end = url.indexOf("-d")
        return url.substring(start, end + 1)
    }

    //endregion

    //region Loaders and Callbacks

    class PostAvatarLoader(context: Context, internal val name: String, internal val contentType: String, internal val path: String, internal val size: Long) :
            AsyncTaskLoader<AvatarWrapper>(context) {

        override fun loadInBackground() = FileUploadManager.uploadAvatarSynchronous(name, size, contentType, path)

        override fun onStopLoading() {
            // Attempt to cancel the current load task if possible.
            cancelLoad()
        }
    }

    /**
     * Used to instantiate the PostAvatarLoader
     * @param id - a unique ID
     * *
     * @param args - a bundle, containing:
     * *             -String name
     * *             -String content type
     * *             -String path
     * *             -int size
     * *
     * @return
     */
    override fun onCreateLoader(id: Int, args: Bundle?) = PostAvatarLoader(
            requireActivity(),
            args?.getString(Const.NAME) ?: "",
            args?.getString(Const.CONTENT_TYPE) ?: "",
            args?.getString(Const.PATH) ?: "",
            args?.getLong(Const.SIZE) ?: 0
    )

    override fun onLoadFinished(loader: Loader<AvatarWrapper>, data: AvatarWrapper?) {
        hideProgressBar()

        data?.avatar?.let {
            if (!ApiPrefs.domain.contains("instructure.com")) {
                // We have a vanity URL for the domain - has to be handled slightly differently
                val fileNumber = it.url?.substringAfter("files/")?.substringBefore(("/download")) ?: ""
                // Get the token and then update the user's avatar URL
                FileFolderManager.getAvatarFileToken(fileNumber, getTokenCallback)
            } else {
                user?.avatarUrl = it.url
                updateAvatarImage(it.url)
                // Notify canvas of the change in avatar URL
                UserManager.updateUsersAvatar(it.url!!, mAvatarPostedCallback)
            }
        } ?: data?.let {
            //check to see the error messages
            when(it.error) {
                AvatarWrapper.ERROR_QUOTA_EXCEEDED -> showToast(R.string.fileQuotaExceeded)
                AvatarWrapper.ERROR_UNKNOWN -> showToast(R.string.errorUploadingFile)
            }
        }
    }

    override fun onLoaderReset(loader: Loader<AvatarWrapper>) {}

    private fun createLoaderBundle(name: String, contentType: String, path: String, size: Long): Bundle {
        val bundle = Bundle()
        bundle.putString(Const.NAME, name)
        bundle.putString(Const.CONTENT_TYPE, contentType)
        bundle.putString(Const.PATH, path)
        bundle.putLong(Const.SIZE, size)
        return bundle
    }

    private val mAvatarPostedCallback = object: StatusCallback<User>() {
        override fun onResponse(response: Response<User>, linkHeaders: LinkHeaders, type: ApiType) {
            response.body()?.let { user ->
                ApiPrefs.user = ApiPrefs.user?.apply { avatarUrl = user.avatarUrl }
                updateAvatarImage(user.avatarUrl)
            }
        }
    }

    private val getTokenCallback = object: StatusCallback<FileFolder>() {
        override fun onResponse(response: Response<FileFolder>, linkHeaders: LinkHeaders, type: ApiType) {
            response.body()?.let { file ->
                val token = file.avatar?.token ?: ""
                UserManager.updateUsersAvatarWithToken(token, mAvatarPostedCallback)
                updateAvatarImage(user?.avatarUrl)
            }
        }
    }
    //endregion
}


