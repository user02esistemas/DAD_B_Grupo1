import '../../../core/network/api_client.dart';

class PurchaseService {
  PurchaseService(this._apiClient);

  final ApiClient _apiClient;

  Future<List<Supplier>> suppliers() async {
    final result = await _apiClient.get('/api/compras/proveedores');
    if (!result.success || result.data is! List) {
      throw Exception(result.message);
    }
    return (result.data! as List)
        .whereType<Map>()
        .map((json) => Supplier.fromJson(Map<String, dynamic>.from(json)))
        .toList();
  }

  Future<List<CatalogProduct>> searchCatalog(String term) async {
    final result = await _apiClient.get('/api/compras/productos', {
      'q': term,
      'limite': '12',
    });
    if (!result.success || result.data is! List) {
      throw Exception(result.message);
    }
    return (result.data! as List)
        .whereType<Map>()
        .map((json) => CatalogProduct.fromJson(Map<String, dynamic>.from(json)))
        .toList();
  }

  Future<void> registerPurchase({
    required int userId,
    required int supplierId,
    required PurchaseLine line,
    String observations = '',
  }) async {
    final subtotal = line.total / 1.18;
    final igv = line.total - subtotal;
    final result = await _apiClient.post('/api/compras', {
      'usuarioId': userId,
      'proveedorId': supplierId,
      'metodoPago': 'EFECTIVO',
      'tipoComprobante': 'NOTA_VENTA',
      'observaciones': observations,
      'montoEfectivo': line.total,
      'montoVirtual': 0,
      'subtotal': subtotal,
      'igv': igv,
      'total': line.total,
      'estado': 'COMPLETADA',
      'detalles': [line.toJson()],
    });
    if (!result.success) {
      throw Exception(result.message);
    }
  }
}

class Supplier {
  const Supplier({required this.id, required this.name, required this.ruc});

  final int id;
  final String name;
  final String ruc;

  factory Supplier.fromJson(Map<String, dynamic> json) => Supplier(
        id: _int(json['id']),
        name: json['razonSocial']?.toString() ?? 'Proveedor',
        ruc: json['ruc']?.toString() ?? '-',
      );
}

class CatalogProduct {
  const CatalogProduct(
      {required this.id, required this.name, required this.presentation});

  final int id;
  final String name;
  final String presentation;

  factory CatalogProduct.fromJson(Map<String, dynamic> json) {
    final name = [
      json['nombreComercial']?.toString(),
      json['concentracion']?.toString(),
    ].where((part) => part != null && part.isNotEmpty).join(' ');
    return CatalogProduct(
      id: _int(json['id']),
      name: name.isEmpty ? 'Producto #${json['id']}' : name,
      presentation: json['presentacion']?.toString() ?? 'Sin presentacion',
    );
  }
}

class PurchaseLine {
  const PurchaseLine({
    required this.product,
    required this.quantity,
    required this.purchasePrice,
    required this.salePrice,
    required this.lot,
    required this.expirationDate,
  });

  final CatalogProduct product;
  final int quantity;
  final double purchasePrice;
  final double salePrice;
  final String lot;
  final String expirationDate;

  double get total => quantity * purchasePrice;

  Map<String, Object?> toJson() => {
        'catalogoProductoId': product.id,
        'nombreComercial': product.name,
        'cantidad': quantity,
        'precioCompra': purchasePrice,
        'precioVenta': salePrice,
        'lote': lot,
        'fechaVencimiento': expirationDate,
      };
}

int _int(Object? value) =>
    value is num ? value.toInt() : int.tryParse(value?.toString() ?? '') ?? 0;
