/*
 * Copyright (C) 2016 - present Instructure, Inc.
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

package com.instructure.student.model;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.drawable.Drawable;
import android.text.style.ImageSpan;
import android.widget.ImageView;

public class ChipsItem extends ImageSpan implements Comparable<ChipsItem>{

    private String title;
    private String id;
    private ImageView imageView;
    private String image_url;
	public ChipsItem(){
        super(new Drawable() {
            @Override
            public void draw(Canvas canvas) {

            }

            @Override
            public void setAlpha(int i) {

            }

            @Override
            public void setColorFilter(ColorFilter colorFilter) {

            }

            @Override
            public int getOpacity() {
                return 0;
            }
        });

    }

    public ChipsItem(Drawable drawable, String title, String id, String url, ImageView image){
        super(drawable);
        this.title = title;
        this.id = id;
        this.image_url = url;
        this.imageView = image;
    }

	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
    public String getImageUrl() {
        return image_url;
    }
    public void setImageUrl(String image_url) {
        this.image_url = image_url;
    }
	public ImageView getImageView() {
		return this.imageView;
	}
	public void setImageid(ImageView imgview) {
		this.imageView = imgview;
	}
    public void setId( String id ){this.id = id;}
    public String getId(){ return id; }
	@Override
	public String toString() {
		return getTitle();
	}

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }

        ChipsItem other = (ChipsItem) obj;
        if(other.getId().equals(this.getId())){
            return true;
        }
        return false;
    }

    @Override
    public int compareTo(ChipsItem chipsItem) {
        return id.compareTo(chipsItem.getId());
    }
}
