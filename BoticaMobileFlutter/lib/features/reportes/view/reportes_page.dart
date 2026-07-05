import '../../../core/widgets/module_placeholder_page.dart';

class ReportesPage extends ModulePlaceholderPage {
  const ReportesPage({super.key})
      : super(
          title: 'Reportes',
          description: 'Modulo para consultar ventas por rango y productos mas vendidos.',
          nextSteps: const [
            'Consumir GET /api/reportes/ventas',
            'Consumir GET /api/reportes/productos-mas-vendidos',
            'Agregar filtros de fechas',
            'Mostrar resumen y detalle',
          ],
        );
}
