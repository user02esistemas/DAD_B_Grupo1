import '../../../core/network/api_client.dart';
import '../../productos/model/product.dart';

class SalesService {
  SalesService(this._apiClient);

  final ApiClient _apiClient;

  Future<void> registerSale({
    required int userId,
    required Product product,
    required int quantity,
    required String paymentMethod,
    required String customerName,
  }) async {
    final subtotal = product.precioVenta * quantity;
    final total = subtotal * 1.18;
    final isVirtual =
        paymentMethod == 'YAPE_PLIN' || paymentMethod == 'TARJETA';
    final result = await _apiClient.post('/api/ventas', {
      'usuarioId': userId,
      'clienteNombre':
          customerName.trim().isEmpty ? 'CLIENTES VARIOS' : customerName.trim(),
      'metodoPago': paymentMethod,
      'tipoComprobante': 'NOTA_VENTA',
      'montoEfectivo': isVirtual ? 0 : total,
      'montoVirtual': isVirtual ? total : 0,
      'medioPagoVirtual': isVirtual ? paymentMethod : null,
      'vuelto': 0,
      'detalles': [
        {
          'productoId': product.id,
          'cantidad': quantity,
          'precioUnitario': product.precioVenta,
        }
      ],
    });

    if (!result.success) {
      throw Exception(result.message);
    }
  }

  Future<void> registerCartSale({
    required int userId,
    required List<SaleItem> items,
    required String paymentMethod,
    required String customerName,
  }) async {
    if (items.isEmpty) throw Exception('Agregue productos a la venta.');
    final total = items.fold<double>(
      0,
      (sum, item) => sum + (item.product.precioVenta * item.quantity * 1.18),
    );
    final isVirtual =
        paymentMethod == 'YAPE_PLIN' || paymentMethod == 'TARJETA';
    final result = await _apiClient.post('/api/ventas', {
      'usuarioId': userId,
      'clienteNombre':
          customerName.trim().isEmpty ? 'CLIENTES VARIOS' : customerName.trim(),
      'metodoPago': paymentMethod,
      'tipoComprobante': 'NOTA_VENTA',
      'montoEfectivo': isVirtual ? 0 : total,
      'montoVirtual': isVirtual ? total : 0,
      'medioPagoVirtual': isVirtual ? paymentMethod : null,
      'vuelto': 0,
      'detalles': items
          .map((item) => {
                'productoId': item.product.id,
                'cantidad': item.quantity,
                'precioUnitario': item.product.precioVenta,
              })
          .toList(),
    });
    if (!result.success) throw Exception(result.message);
  }
}

class SaleItem {
  const SaleItem({required this.product, required this.quantity});

  final Product product;
  final int quantity;

  double get total => product.precioVenta * quantity * 1.18;

  SaleItem copyWith({int? quantity}) => SaleItem(
        product: product,
        quantity: quantity ?? this.quantity,
      );
}
