import 'package:firebase_database/firebase_database.dart';

class FoozTable {
  String key;
  String id = "";
  String currentBestOf = "";
  String currentGame = "";
  String currentPointsToWin = "";
  String currentRound = "";
  String currentScoreTeamOne = "";
  String currentScoreTeamTwo = "";
  String name = "";
  String sideOneColor = "";
  String sideOneName = "";
  String sideTwoColor = "";
  String sideTwoName = "";
  String pushId = "";

  // teamOne: TableTeam? = null,
  // teamTwo: TableTeam? = null

  FoozTable(
    this.id,
    this.currentBestOf,
    this.currentGame,
    this.currentPointsToWin,
    this.currentRound,
    this.currentScoreTeamOne,
    this.currentScoreTeamTwo,
    this.name,
    this.sideOneColor,
    this.sideOneName,
    this.sideTwoColor,
    this.sideTwoName,
    this.pushId,
  );

  FoozTable.fromSnapshot(DataSnapshot snapshot)
      : key = snapshot.key,
        id = snapshot.value["id"],
        currentBestOf = snapshot.value["currentBestOf"],
        currentGame = snapshot.value["currentGame"],
        currentPointsToWin = snapshot.value["currentPointsToWin"],
        currentRound = snapshot.value["currentRound"],
        currentScoreTeamOne = snapshot.value["currentScoreTeamOne"],
        currentScoreTeamTwo = snapshot.value["currentScoreTeamTwo"],
        name = snapshot.value["name"],
        sideOneColor = snapshot.value["sideOneColor"],
        sideOneName = snapshot.value["sideOneName"],
        sideTwoColor = snapshot.value["sideTwoColor"],
        sideTwoName = snapshot.value["sideTwoName"],
        pushId = snapshot.value["pushId"];

  toJson() {
    return {
      "id": id,
      "currentBestOf": currentBestOf,
      "currentGame": currentGame,
      "currentPointsToWin": currentPointsToWin,
      "currentRound": currentRound,
      "currentScoreTeamOne": currentScoreTeamOne,
      "currentScoreTeamTwo": currentScoreTeamTwo,
      "name": name,
      "sideOneColor": sideOneColor,
      "sideOneName": sideOneName,
      "sideTwoColor": sideTwoColor,
      "sideTwoName": sideTwoName,
      "pushId": pushId,
    };
  }
}
