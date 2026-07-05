import 'package:flutter_test/flutter_test.dart';

import 'package:botica_mobile_flutter/app.dart';

void main() {
  testWidgets('muestra pantalla de login', (WidgetTester tester) async {
    await tester.pumpWidget(const EconoSaludApp());

    expect(find.text('EconoSalud'), findsOneWidget);
    expect(find.text('Ingresar'), findsOneWidget);
  });
}
