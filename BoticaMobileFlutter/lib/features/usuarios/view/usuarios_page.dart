import 'package:flutter/material.dart';

import '../../../core/widgets/operational_module_page.dart';

class UsuariosPage extends StatelessWidget {
  const UsuariosPage({super.key});

  @override
  Widget build(BuildContext context) {
    return OperationalModulePage(
      title: 'Usuarios',
      description: 'Personal activo, roles asignados y acceso administrativo.',
      icon: Icons.group_rounded,
      color: const Color(0xFF364FC7),
      primaryAction: 'Usuarios registrados',
      loader: (service) => service.users(),
    );
  }
}
