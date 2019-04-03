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
package com.instructure.androidfoosball.utils

import android.content.Context
import android.speech.tts.TextToSpeech
import com.instructure.androidfoosball.BuildConfig
import com.instructure.androidfoosball.R
import com.instructure.androidfoosball.ktmodels.*
import java.util.*

class Commentator {

    enum class Sfx(val resId: Int) {
        DING(R.raw.ding),
        REVERSE_DING(R.raw.ding_reverse),
        ROTATE_DING(R.raw.rotate_ding),
        WINNING_GOAL(R.raw.ding_ding),
        FIRST_BLOOD(R.raw.firstblood),
        DOMINATING(R.raw.dominating),
        MASSACRE(R.raw.massacre),
        MURCA(R.raw.murca),
        BAGEL_SONG(R.raw.bagel_song),
        DOUBLE_KILL(R.raw.doublekill),
        MULTI_KILL(R.raw.multikill),
        KILLING_SPREE(R.raw.killingspree),
        UNSTOPPABLE(R.raw.unstoppable),
        LEEROY_JENKINS(R.raw.leeroy_jenkins),
        MOTIVATION(R.raw.motivation),
        ILLUMINATI_CONFIRMED(R.raw.illuminati_confirmed),
        MLG_HORN(R.raw.mlg_horn),
        LEGITNESS(R.raw.legitness),
        PROFAMITY(R.raw.profamity),
        POLICE_SIREN(R.raw.police_siren)
    }

    private val mSfxNames = Sfx.values().map { it.name }
    private val mQueuedText = ArrayList<String>()
    private var mIsReady = false
    private val mRandy = Random()
    lateinit private var mTts: TextToSpeech
    private var rotatingCommentIndices: HashMap<Int, MutableList<Int>> = HashMap()
    private var mInitialized = false

    fun initialize(context: Context) {
        if (mInitialized) return
        mTts = TextToSpeech(context) { mIsReady = true; mQueuedText.forEach { speak(it) } }
        Sfx.values().forEach { mTts.addSpeech(it.name, BuildConfig.APPLICATION_ID, it.resId) }
        mInitialized = true
    }

    fun shutUp() {
        mTts.stop()
        mTts.shutdown()
    }

    @Suppress("DEPRECATION")
    private fun speak(text: String, flush: Boolean = true) {
        when (mIsReady) {
            true -> segmentify(text).forEachIndexed { idx, it ->
                mTts.speak(it, if (idx == 0 && flush) TextToSpeech.QUEUE_FLUSH else TextToSpeech.QUEUE_ADD, null)
            }
            false -> mQueuedText.add(text)
        }
    }

    private fun segmentify(text: String, start: Int = 0, list: ArrayList<String> = ArrayList()): List<String> {
        return when {
            start >= text.length -> list
            else -> {
                val match = text.findAnyOf(mSfxNames, start)
                when (match) {
                    null -> list.add(text.substring(start))
                    else -> {
                        list.addIfValid(text.substring(start, match.first))
                        list.add(match.second)
                        segmentify(text, match.first + match.second.length, list)
                    }
                }
                list
            }
        }
    }
    
    private fun rotate(id: Int, vararg comments: String): String {
        if (comments.isEmpty()) return "No comment"
        val validIndices = rotatingCommentIndices.getOrPut(id) {(0 until comments.size).toMutableList()}
        if (validIndices.isEmpty()) validIndices.addAll(0 until comments.size)
        return comments[validIndices.removeAt(mRandy.nextInt(validIndices.size))]
    }

    fun announce(text: String, flush: Boolean = true) = speak(text, flush)

    fun queueAnnounce(text: String) = announce(text, false)

    fun announceBadTouch() = speak(rotate( 12347,
            Sfx.MLG_HORN.name,
            "You have to start a game first. ${Sfx.MOTIVATION}",
            "Don't touch that",
            "I can't even.",
            Sfx.MURCA.name,
            "Ding. Wait, that's not right.",
            "In soviet foosball, button press you.",
            "I know that you believe you understand what you think I said, but I'm not sure you realize that what you heard is not what I meant.",
            "Gross.",
            "Do you even know what you're doing?",
            "Insert coin",
            "Look at me! I'm so special! I pressed a button. Hahahahaaaayugugudidididididahahahaaaaaaaaaaa.",
            "That's inappropriate",
            "There's no game in progress, dummy.",
            "Look buddy. You can't be doing that.",
            "Get your greasy fingers away from that button",
            "The controls are over here dude",
            "Touch that again and you're grounded",
            "Do you think that's funny? Just going around pressing all the buttons you see?",
            "${Sfx.DING} One point goes to nobody, because nobody is playing right now.",
            "${Sfx.DING} Is this what you wanted? Free points with no effort? Fine, take all the points you want. It doesn't mean anything in the end. ${Sfx.WINNING_GOAL}",
            "Is doesn't work like that. You have to start a game first.",
            "Maybe you're new to this. You see, in foosball you need to score a goal to get a point. And in order to score a point you need to start a game.",
            "Watch out! There's a serial button presser on the loose!",
            "I don't know what you want me to do. You have to tell me.",
            "Congratulations, you pressed a button. Do you want a sticker or something?"
    ))

    fun announcePlayerAssignment(user: User, teamName: String) {
        // Use custom assignment phrase if available
        if (user.customAssignmentPhrase.isNotBlank()) {
            announce(user.customAssignmentPhrase)
            return
        }

        // Otherwise, use the normal assignment set
        val playerName = user.name
        speak(rotate(87394,
                "$playerName is assigned to $teamName",
                "$playerName will be playing for $teamName",
                "$teamName grudgingly accepts $playerName",
                "$teamName pulls $playerName from the bench",
                "$teamName must be desperate if they're putting $playerName into play",
                "$playerName joins $teamName",
                "$playerName is ready to fight for $teamName",
                "$teamName adds $playerName to its ranks",
                "$playerName rips his shirt off before realizing this isn't shirts versus skins.",
                "$teamName takes on $playerName. How charitable.",
                "$playerName risks joining $teamName",
                "$teamName risks bringing on $playerName",
                "$teamName regrets to inform your that it is contractually obligated to allow $playerName on the field.",
                "$playerName takes one for $teamName",
                "$playerName asks $teamName to make his tombstone the biggest.",
                "Grant one ticket to $playerName for a ride on the $teamName train.",
                "$teamName takes $playerName away.",
                "Isn't $playerName a bit too skilled for $teamName?",
                "Where oh where has my little $playerName gone? $teamName, that's where.",
                "$playerName pulls out his com and says Beam me up $teamName",
                "$playerName. $teamName. Now.",
                "In a classic move, $playerName chooses $teamName",
                "Do you, $playerName, take $teamName to be your lawfully wedded team?",
                "$teamName, I'm home. I brought $playerName with me.",
                "One small step for $playerName, one giant mistake for $teamName",
                "$teamName could only afford $playerName",
                "$playerName needs to start somewhere, so $teamName has to do for now."
        ))
    }

    fun announceGameStart(text: String = rotate( 832,
            Sfx.LEEROY_JENKINS.name,
            Sfx.MLG_HORN.name,
            "Let's get this party started",
            "Let's make this a clean fight",
            "Go go go! Move move move move!",
            "Game start. Go!",
            "The goal is to get the thing into the other team's thing",
            "This isn't going to take as long as last time, is it?",
            "Let's make this fast. I totally didn't place any bets. Totally.",
            "If you don't finish this in 5 minutes, I will self destruct.",
            "This again? Do you people never learn?",
            "3. 2. 1. Stop. Go. Wait. Yeah, go. Go now. For reals.",
            "Just do it! Don't let your dreams be dreams!"
    )) = speak("${Sfx.DING.name} $text")

    fun announceGoal(scoringSide: TableSide, round: Round, table: Table) {
        val (scoringTeam, opposingTeam) = when (scoringSide) {
            TableSide.SIDE_1 -> round.sideOneTeam!! to round.sideTwoTeam!!
            TableSide.SIDE_2 -> round.sideTwoTeam!! to round.sideOneTeam!!
        }
        announceGoal(round.getTeamName(scoringTeam, table), round.getTeamName(opposingTeam, table), round.getScore(scoringTeam), round.getScore(opposingTeam))
    }

    fun announceUndoGoal(shameTeam: Team, opposingTeam: Team, round: Round, table: Table) {
        announceUndoGoal(round.getTeamName(shameTeam, table), round.getTeamName(opposingTeam, table), round.getScore(shameTeam), round.getScore(opposingTeam))
    }

    private fun announceUndoGoal(shameTeamName: String, opponentTeamName: String, scoringTeamScore: Int, opponentTeamScore: Int) {
        speak(Sfx.REVERSE_DING.name + rotate(3321,
                "One less point for $shameTeamName. ${Sfx.PROFAMITY}",
                "Shame on you $shameTeamName. You should feel bad. One less point for you.",
                "Maybe we'll take the point away from $shameTeamName and give it to $opponentTeamName",
                "Look at how far you got $shameTeamName. A whole $scoringTeamScore points. Then you went and threw it all away.",
                "First you want the point, then you don't want the point. Make up your mind, will you?",
                "W T F. Moving all of these points around is hard work. I just placed that point there and now you want me to move it again?",
                "It seems $shameTeamName was a bit trigger happy. How embarrassing.",
                "$shameTeamName made an honest mistake. Surely this is cause for ridicule."
        ))
    }

    fun announceGoal(scoringTeamName: String, opponentTeamName: String, scoringTeamScore: Int, opponentTeamScore: Int) {
        speak((if (scoringTeamScore >= 5) Sfx.WINNING_GOAL.name else Sfx.DING.name) + when {

        /* Random chance to spawn patriotism */
            mRandy.nextFloat() < 0.01 -> Sfx.MURCA.name

        /* Winning Goal */
            scoringTeamScore >= 5 -> when (opponentTeamScore) {

            /* BAGEL! */
                0 -> rotate(1,
                        "${Sfx.LEGITNESS} Absolutely beautiful. I couldn't have done it better myself.",
                        "Give me a B. Give me an A. Give me a G. Give me an E. Give me an L. B. A. G. E. L. What does it spell? ${Sfx.MASSACRE}",
                        "Get the cream cheese ready because this bagel is hot hot hot.",
                        "Roll out the bagel carpet because $opponentTeamName is about to take the walk of shame.",
                        Sfx.BAGEL_SONG.name,
                        "$opponentTeamName has bagel on their face. How embarrassing.",
                        "Well, that was easy. Bagels on the house."
                )

            /* Little competition */
                in 1..2 -> rotate(2, 
                        "$opponentTeamName didn't put up much of a fight.",
                        "Was $opponentTeamName even trying?",
                        "$opponentTeamName must be a bunch of interns.",
                        "Piece of cake. Slice of pie. Like taking candy from an intern.",
                        "Let's hope $opponentTeamName is better at their job than at foosball.",
                        "This isn't ping pong $opponentTeamName. Get your head in the game.",
                        "Clearly $opponentTeamName had their mind on other things."
                )

            /* Eh */
                3 -> rotate(3, 
                        "It looks like $opponentTeamName needs a bit more practice.",
                        "$opponentTeamName wins the match! Oh, wait. No they didn't.",
                        "$opponentTeamName forgot to drink their Ovaltine."
                )

            /* A close game */
                4 -> rotate(4,
                        "Good game $opponentTeamName. If only you had sucked a little less.",
                        "It was a close match but the better team came out on top."
                )

                else -> "This shouldn't happen. You shouldn't be here. ${Sfx.MURCA}"

            } + " $scoringTeamName wins the match!"

        /* Opponent hasn't scored */
            opponentTeamScore == 0 -> when (scoringTeamScore) {

            /* First goal of the game */
                1 -> rotate(5, 
                        "$scoringTeamName drew ${Sfx.FIRST_BLOOD}",
                        "$scoringTeamName is off to a good start.",
                        "It's okay $opponentTeamName, there's plenty of time to waste $scoringTeamName",
                        "First point goes to $scoringTeamName",
                        "Don't worry; we're just getting started.",
                        "One down, four to go."
                )

            /* Not first goal */
                2 -> rotate(6, 
                        Sfx.DOUBLE_KILL.name,
                        "Two zero. 1 2 3. Double oh 7. One billion. Numbers are meaningless.",
                        "Wow. Just wow.",
                        "If $scoringTeamName scores and $opponentTeamName isn't there to watch it, does it still count?",
                        "Huh."
                )
                3 -> rotate(7,
                        "Three to zero, illuminati confirmed ${Sfx.ILLUMINATI_CONFIRMED}",
                        Sfx.MULTI_KILL.name,
                        "I'm seeing a trend here",
                        "I see where this is going",
                        "$opponentTeamName is just rolling over and taking it.",
                        "I've got the phone if you want to order some bagels."
                )
                4 -> rotate(8, Sfx.KILLING_SPREE.name, Sfx.UNSTOPPABLE.name)
                else -> "Trlololololololo"
            }

            else -> when (scoringTeamScore - opponentTeamScore) {

                -3 -> rotate(9,
                        "$scoringTeamName is making a comeback. Well, probably not.",
                        "$scoringTeamName might have a chance after all",
                        "Don't get overconfident. $opponentTeamName is still 3 points ahead is thoroughly destroying you.",
                        "$scoringTeamName is making some progress. Finally",
                        "Bagel successfully avoided."
                )

                -2 -> rotate(10,
                        "$scoringTeamName is closing the gap",
                        "Just two more points and you might have a shot at winning this $scoringTeamName",
                        "Look into your collective hearts and find the power to win",
                        "Look at $scoringTeamName trying so hard to win. It would almost be cute if it weren't so pathetic.",
                        "$scoringTeamName is now losing a little less than before."
                )

                -1 -> rotate(11,
                        "$scoringTeamName is only one point behind.",
                        "You win some; you lose some. But you really lose most of them.",
                        "$scoringTeamName is basically winning. Except they're not.",
                        "$scoringTeamName got a goal. Fascinating.",
                        "This game might actually get interesting if $scoringTeamName keeps up the pace."
                )

                0 -> when (scoringTeamScore) {
                    1 -> rotate(12,
                            "Snake eyes! Place your bets now!",
                            "One to one. Brother to brother. Dust to dust. Mano a mano.",
                            "Just keep that up and maybe you'll make it to big league someday $scoringTeamName"
                    )
                    2, 3 -> rotate(13,
                            "Tie game! Only one can be victorious. Will $scoringTeamName take the lead or will $opponentTeamName come out on top?",
                            "$scoringTeamName has leveled the playing field. Not that it was unleveled before. If it was, you should probably get that fixed.",
                            "A tie game only means that $opponentTeamName sucks just as much as $scoringTeamName",
                            "Only one team can win, but everyone gets a participation trophy.",
                            "There are no winners here. I would know. I looked at the roster",
                            "Papa used to tell me that foosball would get me far in life. That was before he died when a foosball table fell on top of him."
                    )
                    4 -> rotate(14,
                            "I don't care who wins, would somebody just hurry up and score?",
                            "Both teams are neck and neck at 4 points. Next point wins the match!",
                            "No matter who scores the next goal, remember that I will love both teams equally. But I will love the winner more.",
                            "Now is a good time for a break, don't you think?"
                    )
                    else -> "WAT"
                }

                1 -> rotate(15,
                        "$scoringTeamName takes the lead. Barely.",
                        "$scoringTeamName is one step ahead, but that's not saying much.",
                        "$scoringTeamName and $opponentTeamName sitting in a tree. F. O. O. S. I. N. G.",
                        "$scoringTeamName got lucky.",
                        "Let's keep this match clean, shall we?",
                        "$opponentTeamName let one get by. Typical.",
                        "Good. Now do it again."
                )

                2 -> rotate(16,
                        "$opponentTeamName just needs some motivation. ${Sfx.MOTIVATION}",
                        "$scoringTeamName is on a roll",
                        "$opponentTeamName is lagging behind.",
                        "I'm pretty sure $scoringTeamName just cheated",
                        "$scoringTeamName should take it easy. They might hurt the feelings of $opponentTeamName",
                        "If I had a dollar for every goal $scoringTeamName made, I would have $scoringTeamScore dollars."
                )

                3 -> rotate(17,
                        "$scoringTeamName is ${Sfx.DOMINATING}",
                        "$scoringTeamName is ${Sfx.UNSTOPPABLE}",
                        "Now I'm confident $scoringTeamName is cheating",
                        "It's not that $scoringTeamName is any good; it's that $opponentTeamName just sucks. A lot.",
                        "Go $opponentTeamName, I believe in you! But I'm still betting everything on $scoringTeamName"
                )

                else -> "Is there a bug in the app? Probably. You shouldn't be hearing this. Did you hear this? You did? Oh great, now I'll have to kill you."
            }
        })
    }
}

fun ArrayList<String>.addIfValid(text: String) {
    if (text.isNotBlank()) add(text)
}
