/*
 * Copyright (C) 2016 - present  Instructure, Inc.
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

package com.instructure.speedgrader.fragments;

import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.instructure.annotations_library.FetchFileAsyncTask.FetchFileCallback;
import com.instructure.pandautils.utils.Const;
import com.instructure.speedgrader.R;
import com.instructure.speedgrader.util.App;
import com.instructure.speedgrader.util.CanvasPDFCache;
import com.pspdfkit.ui.PSPDFFragment;

import java.io.File;

public class PdfSubmissionFragment extends BaseSubmissionView {

    private static final String LOG_TAG = "SG.Submission";

    private Fragment mFragment;

    private Uri mTempFileUri;

    @Override
    public int getRootLayout() {
        return R.layout.fragment_pdf_container;
    }

    @Override
    public boolean retainInstanceState() {
        return false;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = super.onCreateView(inflater, container, savedInstanceState);

        mFragment = getChildFragmentManager().findFragmentById(R.id.pdfContainer);
        if (mFragment == null) {
            if(mTempFileUri == null){
                CanvasPDFCache.getInstance(getActivity()).getInputStream(getContext(), getAttachment().getUrl(), new FetchFileCallback() {
                    @Override
                    public void onFileLoaded(File file) {
                        if(getContext() == null){
                            return;
                        }
                        mTempFileUri = Uri.fromFile(file);
                        mFragment = createPDFFragment();
                        attachFragment();
                    }
                });
            }else{
                mFragment = createPDFFragment();
                attachFragment();
            }
        } else {
            //Rotation
            attachFragment();
        }

        return rootView;
    }

    @Nullable private Fragment createPDFFragment(){
        if(mTempFileUri == null){
            return null;
        }
        return PSPDFFragment.newInstance(mTempFileUri, ((App)getActivity().getApplication()).getConfig());
    }

    private void attachFragment() {
        getChildFragmentManager().beginTransaction()
                .replace(R.id.pdfContainer, mFragment)
                .commit();
        getChildFragmentManager().executePendingTransactions();
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void getBundleData(Bundle bundle) {
        super.getBundleData(bundle);
        setAttachment(currentSubmission.getAttachments().get(0));
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(Const.URI, mTempFileUri);
    }
}