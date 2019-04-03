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
 */

package com.instructure.teacheraid.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.instructure.canvasapi.model.User;
import com.instructure.teacheraid.R;
import com.instructure.teacheraid.animation.FlipAnimation;
import com.instructure.teacheraid.util.Const;
import com.squareup.picasso.Picasso;


public class GuessWhoStudentFragment extends ParentFragment {

    private ImageView avatar;
    private TextView name;
    private RelativeLayout rootView;
    private RelativeLayout cardFront;
    private RelativeLayout cardBack;
    private boolean hasFlipped = false;

    public static Fragment newInstance(FragmentActivity context, User user) {

        Bundle b = new Bundle();
        b.putParcelable(Const.USER, user);
        return Fragment.instantiate(context, GuessWhoStudentFragment.class.getName(), b);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (container == null) {
            return null;
        }

        View view = inflater.inflate(R.layout.guess_who_student_fragment, container, false);
        rootView = (RelativeLayout) view.findViewById(R.id.rootView);
        rootView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FlipAnimation flipAnimation = new FlipAnimation(cardFront, cardBack);

                if (cardFront.getVisibility() == View.GONE)
                {
                    flipAnimation.reverse();
                    hasFlipped = false;

                }
                else {
                    hasFlipped = true;
                }
                rootView.startAnimation(flipAnimation);
            }
        });

        cardFront = (RelativeLayout) view.findViewById(R.id.cardFront);
        cardBack = (RelativeLayout) view.findViewById(R.id.cardBack);

        User user = this.getArguments().getParcelable(Const.USER);
        name = (TextView) view.findViewById(R.id.studentName);
        name.setText(user.getShortName());

        avatar = (ImageView) view.findViewById(R.id.avatar);
        Picasso.with(getActivity()).load(user.getAvatarURL()).into(avatar);

        //show the back view
        if(savedInstanceState != null && savedInstanceState.getBoolean(Const.GUESS_WHO_CARD_FLIPPED)) {
            //we want the card to flip, but we don't want the animation. So use the same logic but set the
            //duration to 0 so it happens instantly
            FlipAnimation flipAnimation = new FlipAnimation(cardFront, cardBack);
            flipAnimation.setDuration(0);
            rootView.startAnimation(flipAnimation);
            hasFlipped = true;
        }
        return view;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putBoolean(Const.GUESS_WHO_CARD_FLIPPED, hasFlipped);
        super.onSaveInstanceState(outState);
    }
}
