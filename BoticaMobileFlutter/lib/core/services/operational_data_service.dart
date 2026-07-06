import '../network/api_client.dart';

class OperationalDataService {
  OperationalDataService(this._apiClient);

  final ApiClient _apiClient;

  Future<ModuleData> latestSales({int limit = 10}) async {
    final result = await _apiClient
        .get('/api/ventas/ultimas', {'limite': limit.toString()});
    if (!result.success) throw Exception(result.message);
    final rows = _list(result.data).map((json) {
      final total = _money(json['total']);
      return ModuleRow(
        title: json['numeroTransaccion']?.toString() ?? 'Venta',
        subtitle: json['nombrePersona']?.toString().isNotEmpty == true
            ? json['nombrePersona'].toString()
            : 'Cliente general',
        value: 'S/ $total',
        footnote:
            '${json['metodoPago'] ?? 'EFECTIVO'} - ${json['estado'] ?? 'COMPLETADA'}',
      );
    }).toList();
    return ModuleData(rows: rows, stats: [
      _stat('Ventas', rows.length.toString()),
      _stat('Total', _sum(rows))
    ]);
  }

  Future<ModuleData> products() async {
    final result = await _apiClient.get('/api/productos', {'limite': '10'});
    if (!result.success) throw Exception(result.message);
    final products = _list(result.data);
    final rows = products.map((json) {
      final name = [
        json['nombreComercial']?.toString(),
        json['concentracion']?.toString(),
      ].where((part) => part != null && part.isNotEmpty).join(' ');
      final stock = json['stockActual']?.toString() ?? '0';
      return ModuleRow(
        title: name.isEmpty ? 'Producto' : name,
        subtitle: '${json['presentacion'] ?? 'Presentacion no registrada'}',
        value: 'S/ ${_money(json['precioVenta'])}',
        footnote: 'Stock $stock - Lote ${json['lote'] ?? '-'}',
      );
    }).toList();
    final totalStock = products.fold<int>(
      0,
      (sum, json) =>
          sum +
          ((json['stockActual'] as num?)?.toInt() ??
              int.tryParse(json['stockActual']?.toString() ?? '') ??
              0),
    );
    return ModuleData(rows: rows, stats: [
      _stat('Productos', rows.length.toString()),
      _stat('Unidades', totalStock.toString())
    ]);
  }

  Future<ModuleData> purchases({int limit = 10}) async {
    final result =
        await _apiClient.get('/api/compras', {'limite': limit.toString()});
    if (!result.success) throw Exception(result.message);
    final rows = _list(result.data).map((json) {
      final proveedor = json['proveedor'];
      return ModuleRow(
        title: json['numeroTransaccion']?.toString() ?? 'Compra',
        subtitle: proveedor is Map
            ? proveedor['razonSocial']?.toString() ?? 'Proveedor'
            : 'Proveedor',
        value: 'S/ ${_money(json['total'])}',
        footnote:
            '${json['metodoPago'] ?? 'EFECTIVO'} - ${json['estado'] ?? 'COMPLETADA'}',
      );
    }).toList();
    return ModuleData(rows: rows, stats: [
      _stat('Compras', rows.length.toString()),
      _stat('Total', _sum(rows))
    ]);
  }

  Future<ModuleData> suppliers() async {
    final result = await _apiClient.get('/api/compras/proveedores');
    if (!result.success) throw Exception(result.message);
    final rows = _list(result.data).map((json) {
      return ModuleRow(
        title: json['razonSocial']?.toString() ?? 'Proveedor',
        subtitle: 'RUC ${json['ruc'] ?? '-'}',
        value: json['activo'] == true ? 'Activo' : 'Inactivo',
        footnote:
            '${json['contacto'] ?? 'Sin contacto'} - ${json['telefono'] ?? 'Sin telefono'}',
      );
    }).toList();
    return ModuleData(rows: rows, stats: [
      _stat('Proveedores', rows.length.toString()),
      _stat('Activos',
          rows.where((row) => row.value == 'Activo').length.toString())
    ]);
  }

  Future<ModuleData> users() async {
    final result = await _apiClient.get('/api/usuarios');
    if (!result.success) throw Exception(result.message);
    final rows = _list(result.data).map((json) {
      final roles = json['roles'] is List
          ? (json['roles'] as List)
              .map((role) => role is Map
                  ? role['nombre']?.toString().replaceFirst('ROLE_', '')
                  : null)
              .whereType<String>()
              .join(', ')
          : 'Sin rol';
      return ModuleRow(
        title: json['nombreCompleto']?.toString() ??
            json['username']?.toString() ??
            'Usuario',
        subtitle:
            json['email']?.toString() ?? json['username']?.toString() ?? '',
        value: json['activo'] == true ? 'Activo' : 'Inactivo',
        footnote: roles,
      );
    }).toList();
    return ModuleData(rows: rows, stats: [
      _stat('Usuarios', rows.length.toString()),
      _stat('Activos',
          rows.where((row) => row.value == 'Activo').length.toString())
    ]);
  }

  Future<ModuleData> topProducts() async {
    final result = await _apiClient
        .get('/api/reportes/productos-mas-vendidos', {'limite': '10'});
    if (!result.success) throw Exception(result.message);
    final rows = _list(result.data).map((json) {
      return ModuleRow(
        title: json['nombreProducto']?.toString() ?? 'Producto',
        subtitle: '${json['cantidadVendida'] ?? 0} unidades vendidas',
        value: 'S/ ${_money(json['totalVendido'])}',
        footnote: 'Ranking de ventas',
      );
    }).toList();
    return ModuleData(rows: rows, stats: [
      _stat('Productos top', rows.length.toString()),
      _stat('Vendido', _sum(rows))
    ]);
  }

  Future<SalesReportData> salesReport(
      {required String desde, required String hasta}) async {
    final result = await _apiClient
        .get('/api/reportes/ventas', {'desde': desde, 'hasta': hasta});
    if (!result.success) throw Exception(result.message);
    final data = result.data is Map
        ? Map<String, dynamic>.from(result.data! as Map)
        : <String, dynamic>{};
    final resumen = data['resumen'] is Map
        ? Map<String, dynamic>.from(data['resumen'] as Map)
        : <String, dynamic>{};
    final ventasPorDia = _list(data['ventasPorDia']).map((json) {
      return ModuleRow(
        title: json['fecha']?.toString() ?? json['dia']?.toString() ?? 'Dia',
        subtitle:
            '${json['cantidadVentas'] ?? json['cantidad'] ?? 0} transacciones',
        value: 'S/ ${_money(json['total'] ?? json['totalVentas'])}',
        footnote: 'Venta diaria',
      );
    }).toList();
    final ventas = _list(data['ventas']).map((json) {
      return ModuleRow(
        title: json['numeroTransaccion']?.toString() ?? 'Venta',
        subtitle: json['nombrePersona']?.toString().isNotEmpty == true
            ? json['nombrePersona'].toString()
            : 'Cliente general',
        value: 'S/ ${_money(json['total'])}',
        footnote:
            '${json['metodoPago'] ?? 'EFECTIVO'} - ${json['estado'] ?? 'COMPLETADA'}',
      );
    }).toList();

    return SalesReportData(
      desde: data['desde']?.toString() ?? desde,
      hasta: data['hasta']?.toString() ?? hasta,
      cantidadVentas: (resumen['cantidadVentas'] as num?)?.toInt() ?? 0,
      totalGeneral: _money(resumen['totalGeneral']),
      totalEfectivo: _money(resumen['totalEfectivo']),
      totalYapePlin: _money(resumen['totalYapePlin']),
      totalTarjeta: _money(resumen['totalTarjeta']),
      totalMixto: _money(resumen['totalMixto']),
      promedioVenta: _money(resumen['promedioVenta']),
      ventasPorDia: ventasPorDia,
      ventas: ventas,
    );
  }

  List<Map<String, dynamic>> _list(Object? data) => data is List
      ? data
          .whereType<Map>()
          .map((json) => Map<String, dynamic>.from(json))
          .toList()
      : const [];
  ModuleStat _stat(String label, String value) =>
      ModuleStat(label: label, value: value);
  String _money(Object? value) => ((value as num?)?.toDouble() ??
          double.tryParse(value?.toString() ?? '') ??
          0)
      .toStringAsFixed(2);
  String _sum(List<ModuleRow> rows) {
    final total = rows.fold<double>(
        0,
        (sum, row) =>
            sum +
            (double.tryParse(row.value.replaceAll('S/', '').trim()) ?? 0));
    return 'S/ ${total.toStringAsFixed(2)}';
  }
}

class ModuleData {
  const ModuleData({required this.rows, required this.stats});
  final List<ModuleRow> rows;
  final List<ModuleStat> stats;
}

class ModuleRow {
  const ModuleRow(
      {required this.title,
      required this.subtitle,
      required this.value,
      required this.footnote});
  final String title;
  final String subtitle;
  final String value;
  final String footnote;
}

class ModuleStat {
  const ModuleStat({required this.label, required this.value});
  final String label;
  final String value;
}

class SalesReportData {
  const SalesReportData({
    required this.desde,
    required this.hasta,
    required this.cantidadVentas,
    required this.totalGeneral,
    required this.totalEfectivo,
    required this.totalYapePlin,
    required this.totalTarjeta,
    required this.totalMixto,
    required this.promedioVenta,
    required this.ventasPorDia,
    required this.ventas,
  });

  final String desde;
  final String hasta;
  final int cantidadVentas;
  final String totalGeneral;
  final String totalEfectivo;
  final String totalYapePlin;
  final String totalTarjeta;
  final String totalMixto;
  final String promedioVenta;
  final List<ModuleRow> ventasPorDia;
  final List<ModuleRow> ventas;
}
