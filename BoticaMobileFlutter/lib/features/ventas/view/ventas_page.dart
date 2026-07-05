import '../../../core/widgets/module_placeholder_page.dart';

class VentasPage extends ModulePlaceholderPage {
  const VentasPage({super.key})
      : super(
          title: 'Ventas',
          description: 'Modulo operativo para registrar ventas y consultar las ultimas transacciones.',
          nextSteps: const [
            'Consumir GET /api/ventas/ultimas',
            'Crear carrito de productos',
            'Registrar venta con POST /api/ventas',
            'Validar stock antes de confirmar',
          ],
        );
}
