import '../../core/widgets/module_placeholder_page.dart';

class ComprasPage extends ModulePlaceholderPage {
  const ComprasPage({super.key})
      : super(
          title: 'Compras',
          description: 'Modulo para registrar compras y abastecimiento de inventario.',
          nextSteps: const [
            'Consumir GET /api/compras/proveedores',
            'Seleccionar proveedor y productos',
            'Registrar compra con POST /api/compras',
            'Verificar incremento de stock',
          ],
        );
}
