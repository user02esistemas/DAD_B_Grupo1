import 'package:flutter/material.dart';

import '../../../core/widgets/operational_module_page.dart';

class VentasPage extends StatelessWidget {
  const VentasPage({super.key});

  @override
  Widget build(BuildContext context) {
    return OperationalModulePage(
      title: 'Ventas',
      description: 'Ultimas ventas registradas, metodo de pago y estado de comprobante.',
      icon: Icons.point_of_sale_rounded,
      color: const Color(0xFF1C7ED6),
      primaryAction: 'Ultimas transacciones',
      loader: (service) => service.latestSales(),
    );
  }
}
