class Product {
  const Product({
    required this.id,
    required this.nombre,
    required this.lote,
    required this.stockActual,
    required this.stockMinimo,
    required this.precioVenta,
    required this.fechaVencimiento,
  });

  final int id;
  final String nombre;
  final String lote;
  final int stockActual;
  final int stockMinimo;
  final double precioVenta;
  final String fechaVencimiento;

  bool get stockBajo => stockActual <= stockMinimo;

  factory Product.fromJson(Map<String, dynamic> json) {
    final catalogo = json['catalogoProducto'] is Map<String, dynamic>
        ? json['catalogoProducto'] as Map<String, dynamic>
        : <String, dynamic>{};
    final nombre = [
      (catalogo['nombreComercial'] ?? json['nombreComercial'])?.toString(),
      (catalogo['concentracion'] ?? json['concentracion'])?.toString(),
    ].where((part) => part != null && part.isNotEmpty).join(' ');

    return Product(
      id: _int(json['id']),
      nombre: nombre.isEmpty ? 'Producto #${json['id']}' : nombre,
      lote: json['lote']?.toString() ?? '-',
      stockActual: _int(json['stockActual']),
      stockMinimo: _int(json['stockMinimo']),
      precioVenta: _double(json['precioVenta']),
      fechaVencimiento: json['fechaVencimiento']?.toString() ?? '-',
    );
  }

  static int _int(Object? value) => value is num ? value.toInt() : int.tryParse(value?.toString() ?? '') ?? 0;
  static double _double(Object? value) => value is num ? value.toDouble() : double.tryParse(value?.toString() ?? '') ?? 0;
}
