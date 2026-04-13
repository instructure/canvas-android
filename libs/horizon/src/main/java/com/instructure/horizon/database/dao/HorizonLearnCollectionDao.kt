/*
 * Copyright (C) 2026 - present Instructure, Inc.
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
 */
package com.instructure.horizon.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.instructure.horizon.database.entity.HorizonLearnCollectionEntity
import com.instructure.horizon.database.entity.HorizonLearnCollectionItemEntity

@Dao
interface HorizonLearnCollectionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCollections(collections: List<HorizonLearnCollectionEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItems(items: List<HorizonLearnCollectionItemEntity>)

    @Query("SELECT * FROM horizon_learn_collections")
    suspend fun getAllCollections(): List<HorizonLearnCollectionEntity>

    @Query("SELECT * FROM horizon_learn_collection_items WHERE collectionId = :collectionId")
    suspend fun getItemsByCollectionId(collectionId: String): List<HorizonLearnCollectionItemEntity>

    @Query("DELETE FROM horizon_learn_collections")
    suspend fun deleteAllCollections()

    @Query("DELETE FROM horizon_learn_collection_items")
    suspend fun deleteAllItems()

    @Transaction
    suspend fun replaceAll(
        collections: List<HorizonLearnCollectionEntity>,
        items: List<HorizonLearnCollectionItemEntity>,
    ) {
        deleteAllCollections()
        deleteAllItems()
        insertCollections(collections)
        insertItems(items)
    }
}
