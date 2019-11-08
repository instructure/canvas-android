import 'package:flutter/material.dart';
import 'package:flutter_parent/models/mobile_verify_result.dart';
import 'package:flutter_parent/screens/web_login/web_login_interactor.dart';
import 'package:flutter_parent/screens/web_login/web_login_screen.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:get_it/get_it.dart';
import 'package:mockito/mockito.dart';
import 'package:webview_flutter/webview_flutter.dart';

import '../utils/test_app.dart';

void main() {
  _setupLocator({WebLoginInteractor webInteractor}) {
    final _locator = GetIt.instance;
    _locator.reset();

    _locator.registerFactory<WebLoginInteractor>(() => webInteractor ?? _MockWebLoginInteractor());
  }

  testWidgets('Shows the domain in the toolbar', (tester) async {
    final domain = 'domain';
    final interactor = _MockWebLoginInteractor();
    when(interactor.mobileVerify(domain)).thenAnswer((_) => Future.value(MobileVerifyResult()));
    _setupLocator(webInteractor: interactor);

    await tester.pumpWidget(TestApp(WebLoginScreen(domain)));
    await tester.pump();

    expect(find.text(domain), findsOneWidget);
  });

  testWidgets('Shows loading while doing mobile verify', (tester) async {
    final domain = 'domain';
    final interactor = _MockWebLoginInteractor();
    when(interactor.mobileVerify(domain)).thenAnswer((_) => Future.value(MobileVerifyResult()));
    _setupLocator(webInteractor: interactor);

    await tester.pumpWidget(TestApp(WebLoginScreen(domain)));
    await tester.pump();

    expect(find.byType(CircularProgressIndicator), findsOneWidget);
  });

  testWidgets('Does not show loading when mobile verify is finished', (tester) async {
    final domain = 'domain';
    final interactor = _MockWebLoginInteractor();
    when(interactor.mobileVerify(domain)).thenAnswer((_) => Future.value(MobileVerifyResult()));
    _setupLocator(webInteractor: interactor);

    await tester.pumpWidget(TestApp(WebLoginScreen(domain)));
    await tester.pumpAndSettle();

    expect(find.byType(CircularProgressIndicator), findsNothing);
  });

  testWidgets('Shows a web view when mobile verify is finished', (tester) async {
    final domain = 'domain';
    final interactor = _MockWebLoginInteractor();
    when(interactor.mobileVerify(domain)).thenAnswer((_) => Future.value(MobileVerifyResult()));
    _setupLocator(webInteractor: interactor);

    await tester.pumpWidget(TestApp(WebLoginScreen(domain)));
    await tester.pumpAndSettle();

    expect(find.byType(WebView), findsOneWidget);
    expect(find.byType(Dialog), findsNothing);
  });

  testWidgets('Shows a dialog when mobile verify failed with general error', (tester) async {
    final domain = 'domain';
    final interactor = _MockWebLoginInteractor();
    when(interactor.mobileVerify(domain))
        .thenAnswer((_) => Future.value(MobileVerifyResult((b) => b..result = VerifyResultEnum.generalError)));
    _setupLocator(webInteractor: interactor);

    await tester.pumpWidget(TestApp(WebLoginScreen(domain)));
    await tester.pumpAndSettle();

    expect(find.byType(WebView), findsOneWidget);
    expect(find.byType(Dialog), findsOneWidget);
    expect(find.text('This app is not authorized for use.'), findsOneWidget);
  });

  testWidgets('Shows a dialog when mobile verify failed because of domain', (tester) async {
    final domain = 'domain';
    final interactor = _MockWebLoginInteractor();
    when(interactor.mobileVerify(domain))
        .thenAnswer((_) => Future.value(MobileVerifyResult((b) => b..result = VerifyResultEnum.domainNotAuthorized)));
    _setupLocator(webInteractor: interactor);

    await tester.pumpWidget(TestApp(WebLoginScreen(domain)));
    await tester.pumpAndSettle();

    expect(find.byType(WebView), findsOneWidget);
    expect(find.byType(Dialog), findsOneWidget);
    expect(find.text('The server you entered is not authorized for this app.'), findsOneWidget);
  });

  testWidgets('Shows a dialog when mobile verify failed because of user agent', (tester) async {
    final domain = 'domain';
    final interactor = _MockWebLoginInteractor();
    when(interactor.mobileVerify(domain))
        .thenAnswer((_) => Future.value(MobileVerifyResult((b) => b..result = VerifyResultEnum.unknownUserAgent)));
    _setupLocator(webInteractor: interactor);

    await tester.pumpWidget(TestApp(WebLoginScreen(domain)));
    await tester.pumpAndSettle();

    expect(find.byType(WebView), findsOneWidget);
    expect(find.byType(Dialog), findsOneWidget);
    expect(find.text('The user agent for this app is not authorized.'), findsOneWidget);
  });

  testWidgets('Shows a dialog when mobile verify failed when unknown', (tester) async {
    final domain = 'domain';
    final interactor = _MockWebLoginInteractor();
    when(interactor.mobileVerify(domain))
        .thenAnswer((_) => Future.value(MobileVerifyResult((b) => b..result = VerifyResultEnum.unknownError)));
    _setupLocator(webInteractor: interactor);

    await tester.pumpWidget(TestApp(WebLoginScreen(domain)));
    await tester.pumpAndSettle();

    expect(find.byType(WebView), findsOneWidget);
    expect(find.byType(Dialog), findsOneWidget);
    expect(find.text('We were unable to verify the server for use with this app.'), findsOneWidget);
  });

  testWidgets('Shows a dialog when mobile verify failed', (tester) async {
    final domain = 'domain';
    final interactor = _MockWebLoginInteractor();
    when(interactor.mobileVerify(domain)).thenAnswer((_) => Future.error(null));
    _setupLocator(webInteractor: interactor);

    await tester.pumpWidget(TestApp(WebLoginScreen(domain)));
    await tester.pumpAndSettle();

    expect(find.byType(WebView), findsOneWidget);
    expect(find.byType(Dialog), findsOneWidget);
    expect(find.text('We were unable to verify the server for use with this app.'), findsOneWidget);
  });

  testWidgets('Shows a dialog when mobile verify failed', (tester) async {
    final domain = 'domain';
    final interactor = _MockWebLoginInteractor();
    when(interactor.mobileVerify(domain)).thenAnswer((_) => Future.error(null));
    _setupLocator(webInteractor: interactor);

    await tester.pumpWidget(TestApp(WebLoginScreen(domain)));
    await tester.pumpAndSettle();

    expect(find.byType(WebView), findsOneWidget);
    expect(find.byType(Dialog), findsOneWidget);

    final matchedWidget = find.text("OK");
    expect(matchedWidget, findsOneWidget);
    await tester.tap(matchedWidget);
    await tester.pumpAndSettle();

    expect(find.byType(Dialog), findsNothing);
  });
}

class _MockWebLoginInteractor extends Mock implements WebLoginInteractor {}
