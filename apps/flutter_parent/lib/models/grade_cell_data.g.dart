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
  final String latePenalty;
  @override
  final String finalGrade;

  factory _$GradeCellData([void Function(GradeCellDataBuilder) updates]) =>
      (new GradeCellDataBuilder()..update(updates)).build();

  _$GradeCellData._(
      {this.state,
      this.submissionText,
      this.showCompleteIcon,
      this.showIncompleteIcon,
      this.showPointsLabel,
      this.accentColor,
      this.graphPercent,
      this.score,
      this.grade,
      this.gradeContentDescription,
      this.outOf,
      this.latePenalty,
      this.finalGrade})
      : super._() {
    if (state == null) {
      throw new BuiltValueNullFieldError('GradeCellData', 'state');
    }
    if (submissionText == null) {
      throw new BuiltValueNullFieldError('GradeCellData', 'submissionText');
    }
    if (showCompleteIcon == null) {
      throw new BuiltValueNullFieldError('GradeCellData', 'showCompleteIcon');
    }
    if (showIncompleteIcon == null) {
      throw new BuiltValueNullFieldError('GradeCellData', 'showIncompleteIcon');
    }
    if (showPointsLabel == null) {
      throw new BuiltValueNullFieldError('GradeCellData', 'showPointsLabel');
    }
    if (accentColor == null) {
      throw new BuiltValueNullFieldError('GradeCellData', 'accentColor');
    }
    if (graphPercent == null) {
      throw new BuiltValueNullFieldError('GradeCellData', 'graphPercent');
    }
    if (score == null) {
      throw new BuiltValueNullFieldError('GradeCellData', 'score');
    }
    if (grade == null) {
      throw new BuiltValueNullFieldError('GradeCellData', 'grade');
    }
    if (gradeContentDescription == null) {
      throw new BuiltValueNullFieldError(
          'GradeCellData', 'gradeContentDescription');
    }
    if (outOf == null) {
      throw new BuiltValueNullFieldError('GradeCellData', 'outOf');
    }
    if (latePenalty == null) {
      throw new BuiltValueNullFieldError('GradeCellData', 'latePenalty');
    }
    if (finalGrade == null) {
      throw new BuiltValueNullFieldError('GradeCellData', 'finalGrade');
    }
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
        latePenalty == other.latePenalty &&
        finalGrade == other.finalGrade;
  }

  @override
  int get hashCode {
    return $jf($jc(
        $jc(
            $jc(
                $jc(
                    $jc(
                        $jc(
                            $jc(
                                $jc(
                                    $jc(
                                        $jc(
                                            $jc(
                                                $jc($jc(0, state.hashCode),
                                                    submissionText.hashCode),
                                                showCompleteIcon.hashCode),
                                            showIncompleteIcon.hashCode),
                                        showPointsLabel.hashCode),
                                    accentColor.hashCode),
                                graphPercent.hashCode),
                            score.hashCode),
                        grade.hashCode),
                    gradeContentDescription.hashCode),
                outOf.hashCode),
            latePenalty.hashCode),
        finalGrade.hashCode));
  }

  @override
  String toString() {
    return (newBuiltValueToStringHelper('GradeCellData')
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
          ..add('latePenalty', latePenalty)
          ..add('finalGrade', finalGrade))
        .toString();
  }
}

class GradeCellDataBuilder
    implements Builder<GradeCellData, GradeCellDataBuilder> {
  _$GradeCellData _$v;

  GradeCellState _state;
  GradeCellState get state => _$this._state;
  set state(GradeCellState state) => _$this._state = state;

  String _submissionText;
  String get submissionText => _$this._submissionText;
  set submissionText(String submissionText) =>
      _$this._submissionText = submissionText;

  bool _showCompleteIcon;
  bool get showCompleteIcon => _$this._showCompleteIcon;
  set showCompleteIcon(bool showCompleteIcon) =>
      _$this._showCompleteIcon = showCompleteIcon;

  bool _showIncompleteIcon;
  bool get showIncompleteIcon => _$this._showIncompleteIcon;
  set showIncompleteIcon(bool showIncompleteIcon) =>
      _$this._showIncompleteIcon = showIncompleteIcon;

  bool _showPointsLabel;
  bool get showPointsLabel => _$this._showPointsLabel;
  set showPointsLabel(bool showPointsLabel) =>
      _$this._showPointsLabel = showPointsLabel;

  Color _accentColor;
  Color get accentColor => _$this._accentColor;
  set accentColor(Color accentColor) => _$this._accentColor = accentColor;

  double _graphPercent;
  double get graphPercent => _$this._graphPercent;
  set graphPercent(double graphPercent) => _$this._graphPercent = graphPercent;

  String _score;
  String get score => _$this._score;
  set score(String score) => _$this._score = score;

  String _grade;
  String get grade => _$this._grade;
  set grade(String grade) => _$this._grade = grade;

  String _gradeContentDescription;
  String get gradeContentDescription => _$this._gradeContentDescription;
  set gradeContentDescription(String gradeContentDescription) =>
      _$this._gradeContentDescription = gradeContentDescription;

  String _outOf;
  String get outOf => _$this._outOf;
  set outOf(String outOf) => _$this._outOf = outOf;

  String _latePenalty;
  String get latePenalty => _$this._latePenalty;
  set latePenalty(String latePenalty) => _$this._latePenalty = latePenalty;

  String _finalGrade;
  String get finalGrade => _$this._finalGrade;
  set finalGrade(String finalGrade) => _$this._finalGrade = finalGrade;

  GradeCellDataBuilder() {
    GradeCellData._initializeBuilder(this);
  }

  GradeCellDataBuilder get _$this {
    if (_$v != null) {
      _state = _$v.state;
      _submissionText = _$v.submissionText;
      _showCompleteIcon = _$v.showCompleteIcon;
      _showIncompleteIcon = _$v.showIncompleteIcon;
      _showPointsLabel = _$v.showPointsLabel;
      _accentColor = _$v.accentColor;
      _graphPercent = _$v.graphPercent;
      _score = _$v.score;
      _grade = _$v.grade;
      _gradeContentDescription = _$v.gradeContentDescription;
      _outOf = _$v.outOf;
      _latePenalty = _$v.latePenalty;
      _finalGrade = _$v.finalGrade;
      _$v = null;
    }
    return this;
  }

  @override
  void replace(GradeCellData other) {
    if (other == null) {
      throw new ArgumentError.notNull('other');
    }
    _$v = other as _$GradeCellData;
  }

  @override
  void update(void Function(GradeCellDataBuilder) updates) {
    if (updates != null) updates(this);
  }

  @override
  _$GradeCellData build() {
    final _$result = _$v ??
        new _$GradeCellData._(
            state: state,
            submissionText: submissionText,
            showCompleteIcon: showCompleteIcon,
            showIncompleteIcon: showIncompleteIcon,
            showPointsLabel: showPointsLabel,
            accentColor: accentColor,
            graphPercent: graphPercent,
            score: score,
            grade: grade,
            gradeContentDescription: gradeContentDescription,
            outOf: outOf,
            latePenalty: latePenalty,
            finalGrade: finalGrade);
    replace(_$result);
    return _$result;
  }
}

// ignore_for_file: always_put_control_body_on_new_line,always_specify_types,annotate_overrides,avoid_annotating_with_dynamic,avoid_as,avoid_catches_without_on_clauses,avoid_returning_this,lines_longer_than_80_chars,omit_local_variable_types,prefer_expression_function_bodies,sort_constructors_first,test_types_in_equals,unnecessary_const,unnecessary_new
