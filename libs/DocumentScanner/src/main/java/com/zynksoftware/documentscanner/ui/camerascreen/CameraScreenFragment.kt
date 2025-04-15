/**
    Copyright 2020 ZynkSoftware SRL

    Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
    associated documentation files (the "Software"), to deal in the Software without restriction,
    including without limitation the rights to use, copy, modify, merge, publish, distribute,
    sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
    furnished to do so, subject to the following conditions:

    The above copyright notice and this permission notice shall be included in all copies or
    substantial portions of the Software.

    THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
    INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
    NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
    DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
    OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.zynksoftware.documentscanner.ui.camerascreen

import android.Manifest
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.zynksoftware.documentscanner.R
import com.zynksoftware.documentscanner.common.extensions.hide
import com.zynksoftware.documentscanner.common.extensions.show
import com.zynksoftware.documentscanner.common.utils.FileUriUtils
import com.zynksoftware.documentscanner.databinding.FragmentCameraScreenBinding
import com.zynksoftware.documentscanner.model.DocumentScannerErrorModel
import com.zynksoftware.documentscanner.ui.base.BaseFragment
import com.zynksoftware.documentscanner.ui.components.scansurface.ScanSurfaceListener
import com.zynksoftware.documentscanner.ui.scan.InternalScanActivity
import java.io.File
import java.io.FileNotFoundException


internal class CameraScreenFragment: BaseFragment<FragmentCameraScreenBinding>(FragmentCameraScreenBinding::inflate), ScanSurfaceListener  {

    private val requestCameraPermission = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) {
            startCamera()
        } else {
            onError(DocumentScannerErrorModel(DocumentScannerErrorModel.ErrorMessage.CAMERA_PERMISSION_REFUSED_GO_TO_SETTINGS))
        }
    }

    private val filePickerContract = registerForActivityResult(ActivityResultContracts.GetContent()) {
        handleSelectedFile(it)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) = with(binding) {
        super.onViewCreated(view, savedInstanceState)

        scanSurfaceView.lifecycleOwner = this@CameraScreenFragment
        scanSurfaceView.listener = this@CameraScreenFragment
        scanSurfaceView.originalImageFile = getScanActivity().originalImageFile

        checkForCameraPermissions()
        initListeners()
    }

    override fun onDestroy() {
        super.onDestroy()
        if(getScanActivity().shouldCallOnClose) {
            getScanActivity().onClose()
        }
    }

    override fun onResume() {
        super.onResume()
        getScanActivity().reInitOriginalImageFile()
        binding.scanSurfaceView.originalImageFile = getScanActivity().originalImageFile
    }

    private fun initListeners() = with(binding) {
        cameraCaptureButton.setOnClickListener {
            takePhoto()
        }
        cancelButton.setOnClickListener {
            finishActivity()
        }
        flashButton.setOnClickListener {
            switchFlashState()
        }
        galleryButton.setOnClickListener {
            selectImageFromGallery()
        }
        autoButton.setOnClickListener {
            toggleAutoManualButton()
        }
    }

    private fun toggleAutoManualButton() = with(binding) {
        scanSurfaceView.isAutoCaptureOn = !scanSurfaceView.isAutoCaptureOn
        if (scanSurfaceView.isAutoCaptureOn) {
            autoButton.text = getString(R.string.zdc_auto)
        } else {
            autoButton.text = getString(R.string.zdc_manual)
        }
    }

    private fun checkForCameraPermissions() {
        ContextCompat.checkSelfPermission(requireActivity(), Manifest.permission.CAMERA).let {
            if (it == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                startCamera()
            } else {
                requestCameraPermission.launch(Manifest.permission.CAMERA)
            }
        }
    }

    private fun startCamera() {
        binding.scanSurfaceView.start()
    }

    private fun takePhoto() {
        binding.scanSurfaceView.takePicture()
    }

    private fun getScanActivity(): InternalScanActivity {
        return (requireActivity() as InternalScanActivity)
    }

    private fun finishActivity() {
        getScanActivity().finish()
    }

    private fun switchFlashState() {
        binding.scanSurfaceView.switchFlashState()
    }

    override fun showFlash() {
        binding.flashButton.show()
    }

    override fun hideFlash() {
        binding.flashButton.hide()
    }

    private fun selectImageFromGallery() {
        filePickerContract.launch("image/*")
    }

    private fun handleSelectedFile(imageUri: Uri?) {
        try {
            if (imageUri != null) {
                val realPath = FileUriUtils.getRealPath(getScanActivity(), imageUri)
                if (realPath != null) {
                    getScanActivity().reInitOriginalImageFile()
                    getScanActivity().originalImageFile = File(realPath)
                    startCroppingProcess()
                } else {
                    Log.e(TAG, DocumentScannerErrorModel.ErrorMessage.TAKE_IMAGE_FROM_GALLERY_ERROR.error)
                    onError(DocumentScannerErrorModel(
                        DocumentScannerErrorModel.ErrorMessage.TAKE_IMAGE_FROM_GALLERY_ERROR, null))
                }
            } else {
                Log.e(TAG, DocumentScannerErrorModel.ErrorMessage.TAKE_IMAGE_FROM_GALLERY_ERROR.error)
                onError(DocumentScannerErrorModel(
                    DocumentScannerErrorModel.ErrorMessage.TAKE_IMAGE_FROM_GALLERY_ERROR, null))
            }
        } catch (e: FileNotFoundException) {
            Log.e(TAG, "FileNotFoundException", e)
            onError(DocumentScannerErrorModel(
                DocumentScannerErrorModel.ErrorMessage.TAKE_IMAGE_FROM_GALLERY_ERROR, e))
        }
    }

    override fun scanSurfacePictureTaken() {
        startCroppingProcess()
    }

    private fun startCroppingProcess() {
        if (isAdded) {
            getScanActivity().showImageCropFragment()
        }
    }

    override fun scanSurfaceShowProgress() {
        showProgressBar()
    }

    override fun scanSurfaceHideProgress() {
        hideProgressBar()
    }

    override fun onError(error: DocumentScannerErrorModel) {
        if(isAdded) {
            getScanActivity().onError(error)
        }
    }

    override fun showFlashModeOn() {
        binding.flashButton.setImageResource(R.drawable.zdc_flash_on)
    }

    override fun showFlashModeOff() {
        binding.flashButton.setImageResource(R.drawable.zdc_flash_off)
    }

    companion object {
        private val TAG = CameraScreenFragment::class.simpleName

        fun newInstance(): CameraScreenFragment {
            return CameraScreenFragment()
        }
    }
}