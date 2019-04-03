/*
 * Copyright (C) 2017 - present Instructure, Inc.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *
 */

package com.instructure.pandarecycler;

public class Item {
    static int idCounter = 0;
    final int id;

    int cmpField;

    int data = (int) (Math.random() * 1000);//used for comparison

    public Item() {
        id = idCounter ++;;
        cmpField = (int) (Math.random() * 1000);
    }

    public Item(int cmpField) {
        id = idCounter ++;;
        this.cmpField = cmpField;
    }

    public Item(int id, int cmpField) {
        this.id = id;
        this.cmpField = cmpField;
    }

    public Item(int id, int cmpField, int data) {
        this.id = id;
        this.cmpField = cmpField;
        this.data = data;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Item item = (Item) o;

        if (cmpField != item.cmpField) {
            return false;
        }
        if (id != item.id) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + cmpField;
        return result;
    }

    @Override
    public String toString() {
        return "Item{" +
                "id=" + id +
                ", cmpField=" + cmpField +
                ", data=" + data +
                '}';
    }

    public long getId() {
        return id;
    }
}
