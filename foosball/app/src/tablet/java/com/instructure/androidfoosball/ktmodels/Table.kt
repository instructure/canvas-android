package com.instructure.androidfoosball.ktmodels

import com.instructure.androidfoosball.App
import com.instructure.androidfoosball.utils.Prefs
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey


@Suppress("unused")
open class Table(
        @PrimaryKey
        var id: String = "",
        var name: String = "",
        var sideOneColor: String = "",
        var sideTwoColor: String = "",
        var sideOneName: String = "",
        var sideTwoName: String = "",
        var currentGame: String = "",
        var currentGameInfo: String = "",
        var pushId: String = ""
) : RealmObject() {
    companion object {

        /**
         * Returns the selected [Table], specified by `tableId` in [Prefs]
         */
        fun getSelectedTable(): Table
                = App.realm.where(Table::class.java).equalTo("id", Prefs.tableId).findFirst()
                ?: throw IllegalStateException("Table cannot be request before initial sync")

        /**
         * Returns `true` if there are any ongoing games
         */
        fun hasOngoingGames() = App.realm.where(Game::class.java).equalTo("status", GameStatus.ONGOING.name).count() > 0
                || App.realm.where(CutThroatGame::class.java).equalTo("status", GameStatus.ONGOING.name).count() > 0
                || App.realm.where(TeamTwisterGame::class.java).equalTo("status", GameStatus.ONGOING.name).count() > 0
                || App.realm.where(TableKingGame::class.java).equalTo("status", GameStatus.ONGOING.name).count() > 0

        /**
         * Returns a [List] of ongoing [Game]s
         */
        fun getOngoingGames(): List<Any>
                = App.realm.where(Game::class.java).equalTo("status", GameStatus.ONGOING.name).findAll().toList() +
                App.realm.where(CutThroatGame::class.java).equalTo("status", GameStatus.ONGOING.name).findAll().toList() +
                App.realm.where(TeamTwisterGame::class.java).equalTo("status", GameStatus.ONGOING.name).findAll().toList() +
                App.realm.where(TableKingGame::class.java).equalTo("status", GameStatus.ONGOING.name).findAll().toList()
    }
}
