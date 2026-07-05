import '../../../core/widgets/module_placeholder_page.dart';

class UsuariosPage extends ModulePlaceholderPage {
  const UsuariosPage({super.key})
      : super(
          title: 'Usuarios',
          description: 'Modulo administrativo restringido a ROLE_ADMIN.',
          nextSteps: const [
            'Consumir GET /api/usuarios',
            'Consultar detalle con GET /api/usuarios/{id}',
            'Definir endpoints de creacion/edicion si se requieren en movil',
            'Validar permisos en API/RMI',
          ],
        );
}
