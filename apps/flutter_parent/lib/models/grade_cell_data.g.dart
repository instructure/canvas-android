// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'grade_cell_data.dart';

// **************************************************************************
// BuiltValueGenerator
// **************************************************************************

class _$GradeCellData extends GradeCellData {
  @override
  final GradeCellState state;
  @override
  final String submissionText;
  @override
  final bool showCompleteIcon;
  @override
  final bool showIncompleteIcon;
  @override
  final bool showPointsLabel;
  @override
  final Color accentColor;
  @override
  final double graphPercent;
  @override
  final String score;
  @override
  final String grade;
  @override
  final String gradeContentDescription;
  @override
  final String outOf;
  @override
  final String yourGrade;
  @override
  final String latePenalty;
  @override
  final String finalGrade;

  factory _$GradeCellData([void Function(GradeCellDataBuilder)? updates]) =>
      (new GradeCellDataBuilder()..update(updates))._build();

  _$GradeCellData._(
      {required this.state,
      required this.submissionText,
      required this.showCompleteIcon,
      required this.showIncompleteIcon,
      required this.showPointsLabel,
      required this.accentColor,
      required this.graphPercent,
      required this.score,
      required this.grade,
      required this.gradeContentDescription,
      required this.outOf,
      required this.yourGrade,
      required this.latePenalty,
      required this.finalGrade})
      : super._() {
    BuiltValueNullFieldError.checkNotNull(state, r'GradeCellData', 'state');
    BuiltValueNullFieldError.checkNotNull(
        submissionText, r'GradeCellData', 'submissionText');
    BuiltValueNullFieldError.checkNotNull(
        showCompleteIcon, r'GradeCellData', 'showCompleteIcon');
    BuiltValueNullFieldError.checkNotNull(
        showIncompleteIcon, r'GradeCellData', 'showIncompleteIcon');
    BuiltValueNullFieldError.checkNotNull(
        showPointsLabel, r'GradeCellData', 'showPointsLabel');
    BuiltValueNullFieldError.checkNotNull(
        accentColor, r'GradeCellData', 'accentColor');
    BuiltValueNullFieldError.checkNotNull(
        graphPercent, r'GradeCellData', 'graphPercent');
    BuiltValueNullFieldError.checkNotNull(score, r'GradeCellData', 'score');
    BuiltValueNullFieldError.checkNotNull(grade, r'GradeCellData', 'grade');
    BuiltValueNullFieldError.checkNotNull(
        gradeContentDescription, r'GradeCellData', 'gradeContentDescription');
    BuiltValueNullFieldError.checkNotNull(outOf, r'GradeCellData', 'outOf');
    BuiltValueNullFieldError.checkNotNull(
        yourGrade, r'GradeCellData', 'yourGrade');
    BuiltValueNullFieldError.checkNotNull(
        latePenalty, r'GradeCellData', 'latePenalty');
    BuiltValueNullFieldError.checkNotNull(
        finalGrade, r'GradeCellData', 'finalGrade');
  }

  @override
  GradeCellData rebuild(void Function(GradeCellDataBuilder) updates) =>
      (toBuilder()..update(updates)).build();

  @override
  GradeCellDataBuilder toBuilder() => new GradeCellDataBuilder()..replace(this);

  @override
  bool operator ==(Object other) {
    if (identical(other, this)) return true;
    return other is GradeCellData &&
        state == other.state &&
        submissionText == other.submissionText &&
        showCompleteIcon == other.showCompleteIcon &&
        showIncompleteIcon == other.showIncompleteIcon &&
        showPointsLabel == other.showPointsLabel &&
        accentColor == other.accentColor &&
        graphPercent == other.graphPercent &&
        score == other.score &&
        grade == other.grade &&
        gradeContentDescription == other.gradeContentDescription &&
        outOf == other.outOf &&
        yourGrade == other.yourGrade &&
        latePenalty == other.latePenalty &&
        finalGrade == other.finalGrade;
  }

  @override
  int get hashCode {
    var _$hash = 0;
    _$hash = $jc(_$hash, state.hashCode);
    _$hash = $jc(_$hash, submissionText.hashCode);
    _$hash = $jc(_$hash, showCompleteIcon.hashCode);
    _$hash = $jc(_$hash, showIncompleteIcon.hashCode);
    _$hash = $jc(_$hash, showPointsLabel.hashCode);
    _$hash = $jc(_$hash, accentColor.hashCode);
    _$hash = $jc(_$hash, graphPercent.hashCode);
    _$hash = $jc(_$hash, score.hashCode);
    _$hash = $jc(_$hash, grade.hashCode);
    _$hash = $jc(_$hash, gradeContentDescription.hashCode);
    _$hash = $jc(_$hash, outOf.hashCode);
    _$hash = $jc(_$hash, yourGrade.hashCode);
    _$hash = $jc(_$hash, latePenalty.hashCode);
    _$hash = $jc(_$hash, finalGrade.hashCode);
    _$hash = $jf(_$hash);
    return _$hash;
  }

  @override
  String toString() {
    return (newBuiltValueToStringHelper(r'GradeCellData')
          ..add('state', state)
          ..add('submissionText', submissionText)
          ..add('showCompleteIcon', showCompleteIcon)
          ..add('showIncompleteIcon', showIncompleteIcon)
          ..add('showPointsLabel', showPointsLabel)
          ..add('accentColor', accentColor)
          ..add('graphPercent', graphPercent)
          ..add('score', score)
          ..add('grade', grade)
          ..add('gradeContentDescription', gradeContentDescription)
          ..add('outOf', outOf)
          ..add('yourGrade', yourGrade)
          ..add('latePenalty', latePenalty)
          ..add('finalGrade', finalGrade))
        .toString();
  }
}

class GradeCellDataBuilder
    implements Builder<GradeCellData, GradeCellDataBuilder> {
  _$GradeCellData? _$v;

  GradeCellState? _state;
  GradeCellState? get state => _$this._state;
  set state(GradeCellState? state) => _$this._state = state;

  String? _submissionText;
  String? get submissionText => _$this._submissionText;
  set submissionText(String? submissionText) =>
      _$this._submissionText = submissionText;

  bool? _showCompleteIcon;
  bool? get showCompleteIcon => _$this._showCompleteIcon;
  set showCompleteIcon(bool? showCompleteIcon) =>
      _$this._showCompleteIcon = showCompleteIcon;

  bool? _showIncompleteIcon;
  bool? get showIncompleteIcon => _$this._showIncompleteIcon;
  set showIncompleteIcon(bool? showIncompleteIcon) =>
      _$this._showIncompleteIcon = showIncompleteIcon;

  bool? _showPointsLabel;
  bool? get showPointsLabel => _$this._showPointsLabel;
  set showPointsLabel(bool? showPointsLabel) =>
      _$this._showPointsLabel = showPointsLabel;

  Color? _accentColor;
  Color? get accentColor => _$this._accentColor;
  set accentColor(Color? accentColor) => _$this._accentColor = accentColor;

  double? _graphPercent;
  double? get graphPercent => _$this._graphPercent;
  set graphPercent(double? graphPercent) => _$this._graphPercent = graphPercent;

  String? _score;
  String? get score => _$this._score;
  set score(String? score) => _$this._score = score;

  String? _grade;
  String? get grade => _$this._grade;
  set grade(String? grade) => _$this._grade = grade;

  String? _gradeContentDescription;
  String? get gradeContentDescription => _$this._gradeContentDescription;
  set gradeContentDescription(String? gradeContentDescription) =>
      _$this._gradeContentDescription = gradeContentDescription;

  String? _outOf;
  String? get outOf => _$this._outOf;
  set outOf(String? outOf) => _$this._outOf = outOf;

  String? _yourGrade;
  String? get yourGrade => _$this._yourGrade;
  set yourGrade(String? yourGrade) => _$this._yourGrade = yourGrade;

  String? _latePenalty;
  String? get latePenalty => _$this._latePenalty;
  set latePenalty(String? latePenalty) => _$this._latePenalty = latePenalty;

  String? _finalGrade;
  String? get finalGrade => _$this._finalGrade;
  set finalGrade(String? finalGrade) => _$this._finalGrade = finalGrade;

  GradeCellDataBuilder() {
    GradeCellData._initializeBuilder(this);
  }

  GradeCellDataBuilder get _$this {
    final $v = _$v;
    if ($v != null) {
      _state = $v.state;
      _submissionText = $v.submissionText;
      _showCompleteIcon = $v.showCompleteIcon;
      _showIncompleteIcon = $v.showIncompleteIcon;
      _showPointsLabel = $v.showPointsLabel;
      _accentColor = $v.accentColor;
      _graphPercent = $v.graphPercent;
      _score = $v.score;
      _grade = $v.grade;
      _gradeContentDescription = $v.gradeContentDescription;
      _outOf = $v.outOf;
      _yourGrade = $v.yourGrade;
      _latePenalty = $v.latePenalty;
      _finalGrade = $v.finalGrade;
      _$v = null;
    }
    return this;
  }

  @override
  void replace(GradeCellData other) {
    ArgumentError.checkNotNull(other, 'other');
    _$v = other as _$GradeCellData;
  }

  @override
  void update(void Function(GradeCellDataBuilder)? updates) {
    if (updates != null) updates(this);
  }

  @override
  GradeCellData build() => _build();

  _$GradeCellData _build() {
    final _$result = _$v ??
        new _$GradeCellData._(
            state: BuiltValueNullFieldError.checkNotNull(
                state, r'GradeCellData', 'state'),
            submissionText: BuiltValueNullFieldError.checkNotNull(
                submissionText, r'GradeCellData', 'submissionText'),
            showCompleteIcon: BuiltValueNullFieldError.checkNotNull(
                showCompleteIcon, r'GradeCellData', 'showCompleteIcon'),
            showIncompleteIcon: BuiltValueNullFieldError.checkNotNull(
                showIncompleteIcon, r'GradeCellData', 'showIncompleteIcon'),
            showPointsLabel: BuiltValueNullFieldError.checkNotNull(
                showPointsLabel, r'GradeCellData', 'showPointsLabel'),
            accentColor: BuiltValueNullFieldError.checkNotNull(
                accentColor, r'GradeCellData', 'accentColor'),
            graphPercent: BuiltValueNullFieldError.checkNotNull(
                graphPercent, r'GradeCellData', 'graphPercent'),
            score: BuiltValueNullFieldError.checkNotNull(
                score, r'GradeCellData', 'score'),
            grade: BuiltValueNullFieldError.checkNotNull(grade, r'GradeCellData', 'grade'),
            gradeContentDescription: BuiltValueNullFieldError.checkNotNull(gradeContentDescription, r'GradeCellData', 'gradeContentDescription'),
            outOf: BuiltValueNullFieldError.checkNotNull(outOf, r'GradeCellData', 'outOf'),
            yourGrade: BuiltValueNullFieldError.checkNotNull(yourGrade, r'GradeCellData', 'yourGrade'),
            latePenalty: BuiltValueNullFieldError.checkNotNull(latePenalty, r'GradeCellData', 'latePenalty'),
            finalGrade: BuiltValueNullFieldError.checkNotNull(finalGrade, r'GradeCellData', 'finalGrade'));
    replace(_$result);
    return _$result;
  }
}

// ignore_for_file: deprecated_member_use_from_same_package,type=lint
