import 'package:botica_mobile_flutter/features/auth/model/user.dart';
import 'package:botica_mobile_flutter/features/ventas/view/ventas_page.dart';
import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

void main() {
  testWidgets('NewSalePage renders POS content', (tester) async {
    await tester.binding.setSurfaceSize(const Size(393, 765));
    const user = User(
      id: 1,
      username: 'admin',
      nombreCompleto: 'Administrador General',
      roles: [Role(id: 1, nombre: 'ROLE_ADMIN', descripcion: 'Admin')],
    );

    await tester.pumpWidget(const MaterialApp(home: NewSalePage(user: user)));
    await tester.pump();

    expect(find.text('Punto de venta'), findsOneWidget);
    expect(find.text('Cliente'), findsOneWidget);
    expect(find.text('Buscar por nombre'), findsOneWidget);

    addTearDown(() => tester.binding.setSurfaceSize(null));
  });
}
