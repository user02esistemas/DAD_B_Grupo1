import '../../../core/widgets/module_placeholder_page.dart';

class CajaPage extends ModulePlaceholderPage {
  const CajaPage({super.key})
      : super(
          title: 'Caja',
          description: 'Modulo pendiente para apertura, cierre y control de sesiones de caja.',
          nextSteps: const [
            'Definir endpoints REST para sesion de caja',
            'Consultar sesion abierta por usuario',
            'Abrir caja con monto inicial',
            'Cerrar caja con resumen de ventas',
          ],
        );
}
