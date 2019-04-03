package com.instructure.canvasapi.model;

import android.os.Parcel;

import java.util.Date;


public class ModuleItemWrapper extends CanvasModel<ModuleItemWrapper> {

    private ModuleItem prev;
    private ModuleItem current;
    private ModuleItem next;

    public ModuleItem getPrev() {
        return prev;
    }

    public void setPrev(ModuleItem prev) {
        this.prev = prev;
    }

    public ModuleItem getCurrent() {
        return current;
    }

    public void setCurrent(ModuleItem current) {
        this.current = current;
    }

    public ModuleItem getNext() {
        return next;
    }

    public void setNext(ModuleItem next) {
        this.next = next;
    }

    @Override
    public String getComparisonString() {
        return null;
    }

    @Override
    public Date getComparisonDate() {
        return null;
    }

    @Override
    public long getId() {
        return 0;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(this.prev, flags);
        dest.writeParcelable(this.current, flags);
        dest.writeParcelable(this.next, flags);
    }

    public ModuleItemWrapper() {
    }

    protected ModuleItemWrapper(Parcel in) {
        this.prev = in.readParcelable(ModuleItem.class.getClassLoader());
        this.current = in.readParcelable(ModuleItem.class.getClassLoader());
        this.next = in.readParcelable(ModuleItem.class.getClassLoader());
    }

    public static final Creator<ModuleItemWrapper> CREATOR = new Creator<ModuleItemWrapper>() {
        @Override
        public ModuleItemWrapper createFromParcel(Parcel source) {
            return new ModuleItemWrapper(source);
        }

        @Override
        public ModuleItemWrapper[] newArray(int size) {
            return new ModuleItemWrapper[size];
        }
    };
}
