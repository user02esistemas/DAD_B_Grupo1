import 'package:flutter/material.dart';

import '../../../core/widgets/operational_module_page.dart';

class ComprasPage extends StatelessWidget {
  const ComprasPage({super.key});

  @override
  Widget build(BuildContext context) {
    return OperationalModulePage(
      title: 'Compras',
      description: 'Compras recientes y abastecimiento registrado en inventario.',
      icon: Icons.inventory_2_rounded,
      color: const Color(0xFF6741D9),
      primaryAction: 'Compras recientes',
      loader: (service) => service.purchases(),
    );
  }
}
